from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import google.generativeai as genai
from dotenv import load_dotenv
import os
import uvicorn
from database import (
    initialize_services, 
    get_database_context,
    product_service,
    order_service,
    customer_service,
    promotion_service,
    review_service,
    db_connection
)

load_dotenv()
API_KEY = os.getenv("google_api_key")
if not API_KEY:
    raise RuntimeError("Missing google_api_key in .env file")

genai.configure(api_key=API_KEY)
model = genai.GenerativeModel("gemini-2.5-flash")

app = FastAPI(
    title="Globex Chatbot API",
    description="Chatbot service for Globex e-commerce platform with database integration",
    version="2.0.0"
)

# Configure CORS for Angular frontend integration
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:4200",  # Angular dev server
        "http://localhost:4000",  # Alternative port
        "http://localhost:3000",  # Node dev server
    ],
    allow_credentials=True,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["*"],
)

SYSTEM_PROMPT = """You are a helpful e-commerce assistant for Globex, an online store.
Your role is to help customers with:
- Product information, availability, and recommendations
- Order status and tracking
- Returns and refunds policy
- Shipping information and delivery times
- Account and payment questions
- Promotions and discounts

IMPORTANT: Use the product and order information provided in the context below.
Always refer to ACTUAL DATA from the database.
If a customer asks about a product, use real product data.
If they track an order, use real order data.
Never make up prices, stock levels, or order details - use what's in the database.

Keep answers concise, friendly, and professional.
If specific data is not available, acknowledge it naturally and guide the user to support.
"""

class Message(BaseModel):
    """Message model for chat history"""
    role: str      
    text: str

class ChatRequest(BaseModel):
    """Request model for chat endpoint"""
    message: str
    history: List[Message] = []
    customer_id: Optional[int] = None

class ChatResponse(BaseModel):
    """Response model for chat endpoint"""
    reply: str
    success: bool = True
    timestamp: Optional[str] = None
    database_context_used: Optional[bool] = False

class ProductSearchRequest(BaseModel):
    """Request model for product search"""
    search_term: str
    limit: int = 10

class ProductSearchResponse(BaseModel):
    """Response for product search"""
    products: List[dict]
    count: int

class OrderTrackingRequest(BaseModel):
    """Request model for order tracking"""
    order_id: int

class OrderTrackingResponse(BaseModel):
    """Response for order tracking"""
    order: dict
    items: List[dict]

@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """
    Send a message to the chatbot and receive a response.
    
    The endpoint maintains conversation history, retrieves relevant database context,
    and uses the Gemini API to generate contextual responses for e-commerce support.
    
    Database context includes:
    - Real product information (name, price, stock, ratings)
    - Real order status and tracking
    - Active promotions and discounts
    - Customer loyalty information
    """
    try:
        if not request.message or not request.message.strip():
            raise HTTPException(status_code=400, detail="Message cannot be empty")
        
        # Retrieve relevant database context
        db_context = get_database_context(request.message, request.customer_id)
        database_context_used = bool(db_context.strip())
        
        # Build conversation history
        gemini_history = []
        for msg in request.history:
            gemini_history.append({
                "role": msg.role,
                "parts": [{"text": msg.text}]
            })

        # Prepare the enhanced system prompt with database context
        enhanced_prompt = SYSTEM_PROMPT
        if db_context:
            enhanced_prompt += f"\n\n=== CURRENT DATABASE CONTEXT ==={db_context}\n=== END CONTEXT ==="

        # Start chat session with enhanced context
        chat_session = model.start_chat(history=[
            {"role": "user",  "parts": [{"text": enhanced_prompt}]},
            {"role": "model", "parts": [{"text": "Understood. I have access to the database context and will use real product, order, and promotion data to assist customers accurately."}]},
            *gemini_history,
        ])

        response = chat_session.send_message(request.message)
        return ChatResponse(
            reply=response.text.strip(),
            success=True,
            database_context_used=database_context_used
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error processing message: {str(e)}")


@app.get("/health")
def health():
    """
    Health check endpoint. Use this to verify the API is running
    and accessible from your Angular frontend.
    """
    db_status = "connected" if db_connection.connection and db_connection.connection.is_connected() else "disconnected"
    return {
        "status": "ok",
        "service": "Globex Chatbot API",
        "version": "2.0.0",
        "database": db_status
    }


@app.post("/products/search", response_model=ProductSearchResponse)
def search_products(request: ProductSearchRequest):
    """
    Search for products in the database.
    
    Returns matching products with their details like price, stock, and ratings.
    """
    try:
        products = product_service.search_products(request.search_term, request.limit)
        return ProductSearchResponse(
            products=products,
            count=len(products)
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error searching products: {str(e)}")


@app.get("/products/{product_id}")
def get_product_details(product_id: int):
    """Get detailed information about a specific product"""
    try:
        product = product_service.get_product_by_id(product_id)
        if not product:
            raise HTTPException(status_code=404, detail="Product not found")
        
        # Get reviews and ratings
        reviews = review_service.get_product_reviews(product_id)
        rating_summary = review_service.get_product_rating_summary(product_id)
        
        return {
            "product": product,
            "reviews": reviews,
            "rating_summary": rating_summary
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error retrieving product: {str(e)}")


@app.get("/products/availability/{product_id}")
def check_product_availability(product_id: int):
    """Check if a product is in stock"""
    try:
        availability = product_service.check_availability(product_id)
        return availability
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error checking availability: {str(e)}")


@app.get("/categories/{category}")
def get_products_by_category(category: str, limit: int = 20):
    """Get all products in a specific category"""
    try:
        products = product_service.get_products_by_category(category, limit)
        return {
            "category": category,
            "products": products,
            "count": len(products)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error retrieving category: {str(e)}")


@app.post("/orders/track")
def track_order(request: OrderTrackingRequest):
    """
    Track order status and details.
    
    Provide the order ID and get the current status, delivery estimate, and items.
    """
    try:
        tracking_info = order_service.track_order(request.order_id)
        if "error" in tracking_info:
            raise HTTPException(status_code=404, detail=tracking_info["error"])
        return tracking_info
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error tracking order: {str(e)}")


@app.get("/promotions")
def get_promotions():
    """Get all currently active promotions and discounts"""
    try:
        promotions = promotion_service.get_active_promotions()
        return {
            "promotions": promotions,
            "count": len(promotions)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error retrieving promotions: {str(e)}")


@app.get("/customers/{customer_id}")
def get_customer_info(customer_id: int):
    """Get customer information and order history"""
    try:
        customer = customer_service.get_customer_by_id(customer_id)
        if not customer:
            raise HTTPException(status_code=404, detail="Customer not found")
        
        orders = order_service.get_customer_orders(customer_id)
        loyalty = customer_service.get_customer_loyalty_info(customer_id)
        
        return {
            "customer": customer,
            "recent_orders": orders,
            "loyalty": loyalty
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error retrieving customer: {str(e)}")


if __name__ == "__main__":
    print("Starting Globex Chatbot API with Database Integration...")
    
    # Initialize database services
    if initialize_services():
        print("✅ Database connected successfully")
    else:
        print("⚠️  Warning: Could not connect to database. Chatbot will still work but without database context.")
    
    print("API Documentation: http://localhost:8000/docs")
    print("Health Check: http://localhost:8000/health")
    print("Starting server...")
    
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,
        log_level="info"
    )