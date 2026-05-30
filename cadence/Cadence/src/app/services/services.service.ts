import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ServicesService {

  private prefix = 'http://localhost:8080'; 

  constructor(private http: HttpClient) {}

  getImageUrl(relativePath: string): string {
    return `${this.prefix}${relativePath}`;
  }

  login(data: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/auth/login`, data);
  }

  register(data: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/auth/register`, data);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.prefix}/api/auth/forgot-password`, { email });
  }

  getUsers(): Observable<any> {
    return this.http.get(`${this.prefix}/api/users`);
  }

  getClients(): Observable<any> {
    return this.http.get(`${this.prefix}/api/clients`);
  }

  getAddresses(): Observable<any> {
    return this.http.get(`${this.prefix}/api/addresses`);
  }

  addAddress(data: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/addresses`, data);
  }

  getAllCategories(): Observable<any[]> {
    return this.http.get<any[]>(`${this.prefix}/api/categories`);
  }

  getCategoryById(id: number): Observable<any> {
    return this.http.get<any>(`${this.prefix}/api/categories/${id}`);
  }

  getAllProducts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.prefix}/api/products`);
  }

  getProductById(id: number): Observable<any> {
    return this.http.get<any>(`${this.prefix}/api/products/${id}`);
  }

  getProductsByCategory(categoryId: any): Observable<any[]> {
    console.log('🔍 Fetching products for category ID:', categoryId);
    return this.http.get<any[]>(`${this.prefix}/api/products/category/${categoryId}`);
  }

  searchProducts(keyword: string): Observable<any[]> {
    const params = new HttpParams().set('keyword', keyword);
    return this.http.get<any[]>(`${this.prefix}/api/products/search`, { params });
  }

  search(keyword: string): Observable<any[]> {
    return this.searchProducts(keyword);
  }

  getFeaturedProducts(limit: number = 8): Observable<any[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<any[]>(`${this.prefix}/api/products/featured`, { params });
  }

  getBestSellers(limit: number = 8): Observable<any[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<any[]>(`${this.prefix}/api/products/best-sellers`, { params });
  }

  // ── Cart ─────────────────────────────────────────────────────────────
  getCart(): Observable<any> {
    return this.http.get<any>(`${this.prefix}/api/cart`);
  }

  addToCart(variantId: number, quantity: number = 1): Observable<any> {
    return this.http.post<any>(`${this.prefix}/api/cart/items`, { variantId, quantity });
  }

  updateCartItemQuantity(cartItemId: number, quantity: number): Observable<any> {
    return this.http.put<any>(`${this.prefix}/api/cart/items/${cartItemId}`, { quantity });
  }

  removeFromCart(cartItemId: number): Observable<any> {
    return this.http.delete<any>(`${this.prefix}/api/cart/items/${cartItemId}`);
  }

  clearCart(): Observable<any> {
    return this.http.delete<any>(`${this.prefix}/api/cart`);
  }

  moveCartItemToWishlist(cartItemId: number): Observable<any> {
    return this.http.post<any>(`${this.prefix}/api/cart/items/${cartItemId}/move-to-wishlist`, {});
  }

  // ── Wishlist ─────────────────────────────────────────────────────────
  getWishlist(): Observable<any> {
    return this.http.get<any>(`${this.prefix}/api/wishlist`);
  }

  addToWishlist(variantId: number): Observable<any> {
    return this.http.post<any>(`${this.prefix}/api/wishlist/items/${variantId}`, {});
  }

  removeFromWishlist(wishlistItemId: number): Observable<any> {
    return this.http.delete<any>(`${this.prefix}/api/wishlist/items/${wishlistItemId}`);
  }

  clearWishlist(): Observable<any> {
    return this.http.delete<any>(`${this.prefix}/api/wishlist`);
  }

  moveWishlistItemToCart(wishlistItemId: number): Observable<any> {
    return this.http.post<any>(`${this.prefix}/api/wishlist/items/${wishlistItemId}/move-to-cart`, {});
  }

  getOrders(): Observable<any> {
    return this.http.get(`${this.prefix}/api/orders`);
  }

  createOrder(data: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/orders/create`, data);
  }

  getPayments(): Observable<any> {
    return this.http.get(`${this.prefix}/api/payments`);
  }

  createPayment(data: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/payments`, data);
  }

  checkout(data: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/checkout`, data);
  }

  getInvoices(): Observable<any> {
    return this.http.get(`${this.prefix}/api/invoices`);
  }

  getShipments(): Observable<any> {
    return this.http.get(`${this.prefix}/api/shipments`);
  }

  getStocks(): Observable<any> {
    return this.http.get(`${this.prefix}/api/stocks`);
  }

  getInventory(): Observable<any> {
    return this.http.get(`${this.prefix}/api/inventory`);
  }

  getSuppliers(): Observable<any> {
    return this.http.get(`${this.prefix}/api/suppliers`);
  }

  getPurchaseOrders(): Observable<any> {
    return this.http.get(`${this.prefix}/api/purchase-orders`);
  }

  getAnalytics(): Observable<any> {
    return this.http.get(`${this.prefix}/api/analytics`);
  }

  getNotifications(): Observable<any> {
    return this.http.get(`${this.prefix}/api/notifications`);
  }

  getChatbotMessages(): Observable<any> {
    return this.http.get(`${this.prefix}/api/chatbot-messages`);
  }

  initiatePayment(payload: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/payment/initiate`, payload);
  }

  // ── Comments ─────────────────────────────────────────────────────────
  getComments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.prefix}/api/comments`);
  }

  submitComment(data: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/comments`, data);
  }

  updateComment(id: number, data: any): Observable<any> {
    return this.http.put(`${this.prefix}/api/comments/${id}`, data);
  }

  deleteComment(id: number): Observable<any> {
    return this.http.delete(`${this.prefix}/api/comments/${id}`);
  }
}