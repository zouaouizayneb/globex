# Globex Chatbot API v2.0

A FastAPI-based chatbot service for the Globex e-commerce platform with **full database integration**. The chatbot responds based on real product, order, and customer data from your MySQL database.

## Features

✅ **Database-Driven Responses** - Uses real product, order, and customer data  
✅ **Product Search** - Search products by name or category  
✅ **Order Tracking** - Real-time order status and tracking  
✅ **Promotions & Discounts** - Active promotions retrieval  
✅ **Customer Profiles** - Access customer loyalty and order history  
✅ **Angular Integration** - Built-in CORS for Angular frontends  
✅ **AI-Powered** - Google Gemini API for intelligent responses  

## Setup Instructions

### 1. Prerequisites

- Python 3.8+
- XAMPP (or any MySQL server)
- Virtual environment setup (venv or conda)

### 2. Installation

```bash
# Create virtual environment
python -m venv venv

# Activate virtual environment
# Windows:
venv\Scripts\activate
# macOS/Linux:
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

### 3. Database Configuration

Edit `.env` file with your MySQL credentials:

```
google_api_key=your_google_api_key_here

# MySQL Database Configuration (XAMPP defaults)
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=
DB_NAME=database
```

**For XAMPP users:**
- Host: `localhost`
- User: `root`
- Password: (leave empty)
- Database: Your database name from phpMyAdmin

### 4. Database Schema

The chatbot expects the following database tables:

```sql
-- Products table
CREATE TABLE products (
  id INT PRIMARY KEY,
  name VARCHAR(255),
  description TEXT,
  price DECIMAL(10,2),
  stock INT,
  category VARCHAR(100),
  specifications TEXT,
  rating FLOAT,
  reviews_count INT
);

-- Orders table
CREATE TABLE orders (
  id INT PRIMARY KEY,
  customer_id INT,
  order_date DATETIME,
  status VARCHAR(50),
  total_amount DECIMAL(10,2),
  shipping_address TEXT,
  estimated_delivery DATE
);

-- Order items table
CREATE TABLE order_items (
  id INT PRIMARY KEY,
  order_id INT,
  product_id INT,
  quantity INT,
  price DECIMAL(10,2),
  FOREIGN KEY (order_id) REFERENCES orders(id),
  FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Customers table
CREATE TABLE customers (
  id INT PRIMARY KEY,
  name VARCHAR(255),
  email VARCHAR(255),
  phone VARCHAR(20),
  registration_date DATETIME,
  loyalty_points INT,
  total_purchases DECIMAL(10,2),
  member_since DATE
);

-- Reviews table
CREATE TABLE reviews (
  id INT PRIMARY KEY,
  product_id INT,
  customer_id INT,
  rating INT,
  comment TEXT,
  review_date DATETIME,
  approved BOOLEAN,
  FOREIGN KEY (product_id) REFERENCES products(id),
  FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Promotions table
CREATE TABLE promotions (
  id INT PRIMARY KEY,
  name VARCHAR(255),
  description TEXT,
  discount_percent FLOAT,
  applicable_categories VARCHAR(255),
  status VARCHAR(50),
  expiry_date DATETIME
);
```

### 5. Running the Server

```bash
python chatbot_api.py
```

The API will be available at `http://localhost:8000`

**Features:**
- **API Documentation**: `http://localhost:8000/docs` (Swagger UI)
- **Health Check**: `http://localhost:8000/health`

## API Endpoints

### Chat Endpoints

#### POST `/chat`
Send a message and get intelligent response based on database context.

**Request Body:**
```json
{
  "message": "Do you have any laptops in stock?",
  "history": [],
  "customer_id": 1
}
```

**Response:**
```json
{
  "reply": "Yes! We have several laptops in stock...",
  "success": true,
  "database_context_used": true
}
```

### Product Endpoints

#### POST `/products/search`
Search for products.

**Request:**
```json
{
  "search_term": "laptop",
  "limit": 10
}
```

**Response:**
```json
{
  "products": [
    {
      "id": 1,
      "name": "Dell XPS 13",
      "price": 999.99,
      "stock": 5,
      "rating": 4.8
    }
  ],
  "count": 1
}
```

#### GET `/products/{product_id}`
Get detailed product information with reviews.

**Response:**
```json
{
  "product": {
    "id": 1,
    "name": "Dell XPS 13",
    "price": 999.99,
    "stock": 5,
    "rating": 4.8
  },
  "reviews": [...],
  "rating_summary": {...}
}
```

#### GET `/products/availability/{product_id}`
Check product availability and stock level.

#### GET `/categories/{category}`
Get all products in a specific category.

### Order Endpoints

#### POST `/orders/track`
Track order status and details.

**Request:**
```json
{
  "order_id": 12345
}
```

**Response:**
```json
{
  "order_id": 12345,
  "status": "shipped",
  "order_date": "2026-04-20",
  "estimated_delivery": "2026-04-25",
  "total_amount": 1299.99,
  "items": [...]
}
```

### Customer Endpoints

#### GET `/customers/{customer_id}`
Get customer profile, order history, and loyalty information.

### Promotion Endpoints

#### GET `/promotions`
Get all active promotions and discounts.

### Health Check

#### GET `/health`
Check API and database status.

## Angular Integration

### 1. Import HttpClientModule

```typescript
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  imports: [HttpClientModule, ...other imports],
  ...
})
export class AppModule { }
```

### 2. Create ChatbotService

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private apiUrl = 'http://localhost:8000';

  constructor(private http: HttpClient) {}

  sendMessage(message: string, history: any[] = [], customerId?: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/chat`, {
      message,
      history,
      customer_id: customerId
    });
  }

  searchProducts(searchTerm: string, limit: number = 10): Observable<any> {
    return this.http.post(`${this.apiUrl}/products/search`, {
      search_term: searchTerm,
      limit
    });
  }

  getProductDetails(productId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/products/${productId}`);
  }

  trackOrder(orderId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/orders/track`, {
      order_id: orderId
    });
  }

  getPromotions(): Observable<any> {
    return this.http.get(`${this.apiUrl}/promotions`);
  }

  getCustomerInfo(customerId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/customers/${customerId}`);
  }

  healthCheck(): Observable<any> {
    return this.http.get(`${this.apiUrl}/health`);
  }
}
```

### 3. Use in Component

```typescript
import { Component, OnInit } from '@angular/core';
import { ChatbotService } from './services/chatbot.service';

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html'
})
export class ChatbotComponent implements OnInit {
  messages: any[] = [];
  userInput = '';

  constructor(private chatbotService: ChatbotService) {}

  ngOnInit() {
    // Verify API is running
    this.chatbotService.healthCheck().subscribe(
      health => console.log('Chatbot API Status:', health)
    );
  }

  sendMessage() {
    if (!this.userInput.trim()) return;

    this.messages.push({ role: 'user', text: this.userInput });

    this.chatbotService.sendMessage(this.userInput, this.messages).subscribe(
      response => {
        this.messages.push({ role: 'model', text: response.reply });
        this.userInput = '';
      },
      error => console.error('Chat error:', error)
    );
  }
}
```

## Production Deployment

### Update CORS

In `chatbot_api.py`, update allowed origins:

```python
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "https://yourdomain.com",
        "https://www.yourdomain.com",
    ],
    allow_credentials=True,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["*"],
)
```

### Environment Variables

Update `.env` for production:

```
google_api_key=your_production_key
DB_HOST=your_db_host
DB_USER=your_db_user
DB_PASSWORD=your_secure_password
DB_NAME=your_production_db
```

### Run with Production Server

```bash
# Using Gunicorn
pip install gunicorn
gunicorn -w 4 -b 0.0.0.0:8000 chatbot_api:app
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| **CORS Error** | Ensure Angular runs on allowed origin (default: localhost:4200) |
| **Database Connection Failed** | Check MySQL is running, verify credentials in `.env` |
| **API Key Error** | Verify `google_api_key` in `.env` |
| **Connection Refused** | Ensure chatbot API runs on port 8000 |
| **Module not found** | Run `pip install -r requirements.txt` |

## Project Structure

```
chatbot/
├── chatbot_api.py          # Main API application
├── database.py             # Database integration module
├── requirements.txt        # Python dependencies
├── .env                    # Configuration (API key, DB credentials)
├── README.md               # This file
└── venv/                   # Virtual environment
```

## License

Internal use only - Globex

