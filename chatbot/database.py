
import mysql.connector
from mysql.connector import Error
from typing import List, Dict, Optional
import os
from dotenv import load_dotenv

load_dotenv()

class DatabaseConnection:
    
    def __init__(self):
        self.host = os.getenv("DB_HOST", "localhost")
        self.user = os.getenv("DB_USER", "root")
        self.password = os.getenv("DB_PASSWORD", "")
        self.database = os.getenv("DB_NAME", "database")
        self.connection = None
    
    def connect(self):
        try:
            self.connection = mysql.connector.connect(
                host=self.host,
                user=self.user,
                password=self.password,
                database=self.database
            )
            if self.connection.is_connected():
                print(f"Connected to MySQL database: {self.database}")
                return True
        except Error as e:
            print(f"Error connecting to MySQL: {e}")
            return False
    
    def disconnect(self):
        if self.connection and self.connection.is_connected():
            self.connection.close()
            print("Disconnected from MySQL database")
    
    def execute_query(self, query: str, params: tuple = None) -> List[Dict]:
        try:
            cursor = self.connection.cursor(dictionary=True)
            cursor.execute(query, params or ())
            results = cursor.fetchall()
            cursor.close()
            return results
        except Error as e:
            print(f"Error executing query: {e}")
            return []
    
    def get_connection(self):
        if not self.connection or not self.connection.is_connected():
            self.connect()
        return self.connection


class ProductService:
    
    def __init__(self, db: DatabaseConnection):
        self.db = db
    
    def search_products(self, search_term: str, limit: int = 10) -> List[Dict]:
        query = """
            SELECT id, name, description, price, stock, category
            FROM products
            WHERE LOWER(name) LIKE %s OR LOWER(description) LIKE %s
            LIMIT %s
        """
        search_pattern = f"%{search_term.lower()}%"
        return self.db.execute_query(query, (search_pattern, search_pattern, limit))
    
    def get_product_by_id(self, product_id: int) -> Optional[Dict]:
        """Get detailed product information"""
        query = """
            SELECT id, name, description, price, stock, category, 
                   specifications, rating, reviews_count
            FROM products
            WHERE id = %s
        """
        results = self.db.execute_query(query, (product_id,))
        return results[0] if results else None
    
    def get_products_by_category(self, category: str, limit: int = 20) -> List[Dict]:
        """Get products by category"""
        query = """
            SELECT id, name, price, stock, rating
            FROM products
            WHERE LOWER(category) = LOWER(%s)
            LIMIT %s
        """
        return self.db.execute_query(query, (category, limit))
    
    def check_availability(self, product_id: int) -> Dict:
        """Check product availability and stock"""
        query = """
            SELECT id, name, stock, price
            FROM products
            WHERE id = %s
        """
        results = self.db.execute_query(query, (product_id,))
        if results:
            product = results[0]
            return {
                "product_id": product["id"],
                "name": product["name"],
                "in_stock": product["stock"] > 0,
                "quantity_available": product["stock"],
                "price": product["price"]
            }
        return {"in_stock": False, "error": "Product not found"}
    
    def get_top_products(self, limit: int = 5) -> List[Dict]:
        """Get top rated or trending products"""
        query = """
            SELECT id, name, price, rating, reviews_count, stock
            FROM products
            WHERE stock > 0
            ORDER BY rating DESC, reviews_count DESC
            LIMIT %s
        """
        return self.db.execute_query(query, (limit,))


class OrderService:
    """Handle order-related database operations"""
    
    def __init__(self, db: DatabaseConnection):
        self.db = db
    
    def get_order_by_id(self, order_id: int) -> Optional[Dict]:
        """Get detailed order information"""
        query = """
            SELECT id, customer_id, order_date, status, total_amount, 
                   shipping_address, estimated_delivery
            FROM orders
            WHERE id = %s
        """
        results = self.db.execute_query(query, (order_id,))
        return results[0] if results else None
    
    def get_order_items(self, order_id: int) -> List[Dict]:
        """Get items in a specific order"""
        query = """
            SELECT oi.product_id, p.name, oi.quantity, oi.price, 
                   (oi.quantity * oi.price) as subtotal
            FROM order_items oi
            JOIN products p ON oi.product_id = p.id
            WHERE oi.order_id = %s
        """
        return self.db.execute_query(query, (order_id,))
    
    def get_customer_orders(self, customer_id: int, limit: int = 10) -> List[Dict]:
        """Get orders for a specific customer"""
        query = """
            SELECT id, order_date, status, total_amount
            FROM orders
            WHERE customer_id = %s
            ORDER BY order_date DESC
            LIMIT %s
        """
        return self.db.execute_query(query, (customer_id, limit))
    
    def track_order(self, order_id: int) -> Dict:
        """Get order tracking information"""
        order = self.get_order_by_id(order_id)
        if not order:
            return {"error": "Order not found"}
        
        items = self.get_order_items(order_id)
        return {
            "order_id": order["id"],
            "status": order["status"],
            "order_date": str(order["order_date"]),
            "estimated_delivery": str(order["estimated_delivery"]),
            "total_amount": order["total_amount"],
            "items": items
        }


class CustomerService:
    """Handle customer-related database operations"""
    
    def __init__(self, db: DatabaseConnection):
        self.db = db
    
    def get_customer_by_id(self, customer_id: int) -> Optional[Dict]:
        """Get customer information"""
        query = """
            SELECT id, name, email, phone, registration_date, loyalty_points
            FROM customers
            WHERE id = %s
        """
        results = self.db.execute_query(query, (customer_id,))
        return results[0] if results else None
    
    def get_customer_by_email(self, email: str) -> Optional[Dict]:
        """Find customer by email"""
        query = """
            SELECT id, name, email, phone, registration_date, loyalty_points
            FROM customers
            WHERE LOWER(email) = LOWER(%s)
        """
        results = self.db.execute_query(query, (email,))
        return results[0] if results else None
    
    def get_customer_loyalty_info(self, customer_id: int) -> Dict:
        """Get customer loyalty program information"""
        query = """
            SELECT id, loyalty_points, total_purchases, member_since
            FROM customers
            WHERE id = %s
        """
        results = self.db.execute_query(query, (customer_id,))
        if results:
            customer = results[0]
            return {
                "loyalty_points": customer["loyalty_points"],
                "total_purchases": customer["total_purchases"],
                "member_since": str(customer["member_since"])
            }
        return {"error": "Customer not found"}


class PromotionService:
    """Handle promotions and discounts"""
    
    def __init__(self, db: DatabaseConnection):
        self.db = db
    
    def get_active_promotions(self) -> List[Dict]:
        """Get currently active promotions"""
        query = """
            SELECT id, name, description, discount_percent, 
                   applicable_categories, expiry_date
            FROM promotions
            WHERE status = 'active' AND expiry_date >= NOW()
            ORDER BY discount_percent DESC
        """
        return self.db.execute_query(query)
    
    def get_applicable_discounts(self, product_id: int) -> List[Dict]:
        """Get discounts applicable to a product"""
        query = """
            SELECT p.id, p.name, p.discount_percent, p.description
            FROM promotions p
            WHERE status = 'active' 
            AND expiry_date >= NOW()
            AND (p.applicable_categories LIKE (
                SELECT category FROM products WHERE id = %s
            ) OR p.applicable_categories = 'all')
        """
        return self.db.execute_query(query, (product_id,))


class ReviewService:
    """Handle product reviews and ratings"""
    
    def __init__(self, db: DatabaseConnection):
        self.db = db
    
    def get_product_reviews(self, product_id: int, limit: int = 5) -> List[Dict]:
        """Get reviews for a product"""
        query = """
            SELECT id, customer_id, rating, comment, review_date
            FROM reviews
            WHERE product_id = %s AND approved = 1
            ORDER BY review_date DESC
            LIMIT %s
        """
        return self.db.execute_query(query, (product_id, limit))
    
    def get_product_rating_summary(self, product_id: int) -> Dict:
        """Get rating summary for a product"""
        query = """
            SELECT 
                AVG(rating) as average_rating,
                COUNT(*) as total_reviews,
                SUM(CASE WHEN rating = 5 THEN 1 ELSE 0 END) as five_star,
                SUM(CASE WHEN rating = 4 THEN 1 ELSE 0 END) as four_star,
                SUM(CASE WHEN rating = 3 THEN 1 ELSE 0 END) as three_star,
                SUM(CASE WHEN rating = 2 THEN 1 ELSE 0 END) as two_star,
                SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) as one_star
            FROM reviews
            WHERE product_id = %s AND approved = 1
        """
        results = self.db.execute_query(query, (product_id,))
        return results[0] if results else {}


# Initialize global database connection
db_connection = DatabaseConnection()

# Initialize service instances
product_service = None
order_service = None
customer_service = None
promotion_service = None
review_service = None


def initialize_services():
    """Initialize all database services"""
    global product_service, order_service, customer_service, promotion_service, review_service
    
    if db_connection.connect():
        product_service = ProductService(db_connection)
        order_service = OrderService(db_connection)
        customer_service = CustomerService(db_connection)
        promotion_service = PromotionService(db_connection)
        review_service = ReviewService(db_connection)
        return True
    return False


def get_database_context(query_text: str, customer_id: Optional[int] = None) -> str:
    """
    Retrieve relevant database context based on the query
    This context will be added to the AI prompt
    """
    context = ""
    
    # Search for products
    if any(keyword in query_text.lower() for keyword in ["product", "item", "find", "looking for", "what do you have"]):
        search_terms = query_text.lower().replace("product", "").replace("item", "").strip()
        if search_terms and len(search_terms) > 2:
            products = product_service.search_products(search_terms, limit=5)
            if products:
                context += "\n\n📦 MATCHING PRODUCTS:\n"
                for p in products:
                    context += f"- {p['name']}: ${p['price']} (Stock: {p['stock']}, Rating: {p.get('rating', 'N/A')})\n"
    
    # Check availability
    if any(keyword in query_text.lower() for keyword in ["available", "stock", "in stock", "have you got"]):
        products = product_service.get_top_products(5)
        if products:
            context += "\n\n✅ AVAILABLE PRODUCTS:\n"
            for p in products:
                context += f"- {p['name']}: ${p['price']} ({p['stock']} in stock)\n"
    
    if any(keyword in query_text.lower() for keyword in ["order", "track", "where is", "status", "delivery"]):
        try:
            words = query_text.split()
            for i, word in enumerate(words):
                if word.isdigit():
                    order_id = int(word)
                    tracking = order_service.track_order(order_id)
                    if "error" not in tracking:
                        context += f"\n\n📋 ORDER TRACKING:\n"
                        context += f"Order #{tracking['order_id']} - Status: {tracking['status']}\n"
                        context += f"Estimated Delivery: {tracking['estimated_delivery']}\n"
                        context += f"Total: ${tracking['total_amount']}\n"
                    break
        except:
            pass
    
    # Get promotions
    if any(keyword in query_text.lower() for keyword in ["discount", "offer", "promotion", "sale", "deal"]):
        promotions = promotion_service.get_active_promotions()
        if promotions:
            context += "\n\n🎉 ACTIVE PROMOTIONS:\n"
            for promo in promotions[:3]:
                context += f"- {promo['name']}: {promo['discount_percent']}% off\n"
                context += f"  {promo['description']}\n"
    
    # Customer information
    if customer_id:
        customer = customer_service.get_customer_by_id(customer_id)
        if customer:
            context += f"\n\n👤 CUSTOMER INFO:\n"
            context += f"Name: {customer['name']}\n"
            context += f"Loyalty Points: {customer.get('loyalty_points', 0)}\n"
            
            # Get recent orders
            orders = order_service.get_customer_orders(customer_id, 3)
            if orders:
                context += "Recent Orders:\n"
                for order in orders:
                    context += f"- Order #{order['id']}: {order['status']} (${order['total_amount']})\n"
    
    return context
