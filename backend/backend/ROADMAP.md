# Backend Roadmap – ERP + E-Commerce (from guide)

This roadmap follows the **ERP-Ecommerce-Complete-Guide** phases and maps them to this backend.

---

## PHASE 1: Foundation ✅ COMPLETED

| Item | Status |
|------|--------|
| Database schema | ✅ |
| Basic entities (User, Product, Order, Client, etc.) | ✅ |
| Simple CRUD operations | ✅ |

---

## PHASE 2: Authentication & Security ✅ MOSTLY DONE

| Item | Status |
|------|--------|
| User registration/login (JWT) | ✅ |
| Customer accounts (Client linked to User) | ✅ |
| Admin accounts | ✅ |
| Role-based access (Client, Admin) | ✅ |
| Password reset | ✅ |
| Email verification | ✅ |

**Guide suggestion:** Separate **User** (employees/admins) from **Customer** (buyers). You already have **Client** as the buyer profile linked to User; next step is to add **Address** and **Cart** and wire them to Client.

---

## PHASE 3: E-Commerce Core → DO NEXT

### 3.1 Product management (enhanced)

- [ ] Product categories (you have Category)
- [ ] Product variants (e.g. size, color)
- [ ] Multiple product images
- [ ] Product reviews & ratings
- [ ] Search & filtering

### 3.2 Shopping cart & wishlist

- [ ] Cart entity (customer, items, quantities)
- [ ] Add/remove/update cart items API
- [ ] Cart persistence (e.g. by customer/session)
- [ ] Wishlist (optional)
- [ ] Cart expiry (optional)

### 3.3 Checkout & payment

- [ ] Shipping address management
- [ ] Multiple addresses per customer (Address entity)
- [ ] Payment gateway (Stripe or PayPal)
- [ ] Order summary, tax, shipping cost
- [ ] Discount codes / coupons

### 3.4 Order management

- [x] Order placement workflow (you have OrderProcessService)
- [x] Order status (Pending, Processing, Shipped, etc.)
- [ ] Order history for customers
- [ ] Cancel/return and refund handling

### 3.5 Shipping integration

- [ ] Carrier integration (DHL, FedEx, UPS, etc.)
- [ ] Shipping rate calculation
- [ ] Tracking number and status updates
- [ ] International shipping rules

---

## PHASE 4: ERP integration

### 4.1 Inventory

- [ ] Stock levels and low-stock alerts
- [ ] Reorder points
- [ ] Stock adjustments and movement history
- [ ] Multi-warehouse (optional)

### 4.2 Supplier management

- [ ] Supplier entity and CRUD
- [ ] Purchase orders
- [ ] Supplier performance and cost tracking

### 4.3 Invoice & finance

- [ ] Automated invoice generation (PDF)
- [ ] Invoice numbering
- [ ] Tax and payment reconciliation
- [ ] Basic financial reports

### 4.4 Analytics & reporting

- [ ] Sales reports (daily/monthly/yearly)
- [ ] Top products, customer analytics
- [ ] Inventory and revenue dashboards
- [ ] Export (Excel/PDF)

---

## PHASE 5: Notifications

- [ ] Email: order confirmation, shipping, password reset (partially done)
- [ ] WhatsApp Business API
- [ ] Telegram integration

---

## PHASE 6: AI chatbot

- [ ] Chat API and history (you have ChatbotMessage)
- [ ] AI integration (OpenAI/Claude)
- [ ] Context (orders, products)
- [ ] Fallback to human support

---

## Suggested order (from guide)

| When | Focus |
|------|--------|
| **Done** | Phase 2 – Auth & security |
| **Next** | **Week 3 – DB enhancement:** Customer/Client, **Cart**, **Address**; Order references Customer |
| Then | **Week 4–5 – Cart API**, product search/filter, variants |
| Then | **Week 6–7 – Checkout:** payment (Stripe), full checkout flow, invoices |
| Then | **Week 8 – Notifications:** email, WhatsApp, Telegram |
| Then | **Week 9–10 – ERP:** admin APIs, reports, inventory |
| Then | **Week 11–12 – AI chatbot** |

---

## Immediate next steps (recommended)

1. **Database enhancement (Week 3)**  
   - Add **Address** entity (customer, street, city, country, postalCode, isDefault).  
   - Add **Cart** and **CartItem** (customer, product, quantity).  
   - Ensure **Order** and **Client** use these (e.g. shipping address, cart → order).

2. **Cart API (Week 4–5)**  
   - `POST/GET/DELETE /api/cart` (or per customer).  
   - Add/remove/update line items, compute totals.

3. **Checkout (Week 6–7)**  
   - Use Cart + Address + Payment to create Order and invoice (align with your existing OrderProcessService).

If you tell me which you want to do first (e.g. “Address + Cart entities and one Cart endpoint”), I can outline the exact entities, fields, and API contracts for this backend.
