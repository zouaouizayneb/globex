import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
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


  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }


  getUsers(): Observable<any> {
    return this.http.get(`${this.prefix}/api/users`, { headers: this.getAuthHeaders() });
  }

  getClients(): Observable<any> {
    return this.http.get(`${this.prefix}/api/clients`, { headers: this.getAuthHeaders() });
  }


  getAddresses(): Observable<any> {
    return this.http.get(`${this.prefix}/api/addresses`, { headers: this.getAuthHeaders() });
  }

  addAddress(data: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/addresses`, data, { headers: this.getAuthHeaders() });
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
    return this.getAllProducts().pipe(
      map((products: any[]) =>
        products.filter((p: any) => {
          const pCategoryId = p.category?.idCategory || p.category?.id || p.category_id || p.category;
          return pCategoryId == categoryId;
        })
      )
    );
  }

  searchProducts(keyword: string): Observable<any[]> {
    const params = new HttpParams().set('keyword', keyword);
    return this.http.get<any[]>(`${this.prefix}/api/products/search`, { params });
  }

  getFeaturedProducts(limit: number = 8): Observable<any[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<any[]>(`${this.prefix}/api/products/featured`, { params });
  }


  addToCart(cartData: any): Observable<any> {
    return this.http.post<any>(`${this.prefix}/api/cart/add`, cartData, { headers: this.getAuthHeaders() });
  }

  getCart(): Observable<any> {
    return this.http.get<any>(`${this.prefix}/api/cart`, { headers: this.getAuthHeaders() });
  }

  removeFromCart(itemId: number): Observable<any> {
    return this.http.delete<any>(`${this.prefix}/api/cart/${itemId}`, { headers: this.getAuthHeaders() });
  }


  getWishlist(): Observable<any> {
    return this.http.get(`${this.prefix}/api/wishlist`, { headers: this.getAuthHeaders() });
  }

  addToWishlist(data: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/wishlist`, data, { headers: this.getAuthHeaders() });
  }

  removeFromWishlist(itemId: number): Observable<any> {
    return this.http.delete(`${this.prefix}/api/wishlist/${itemId}`, { headers: this.getAuthHeaders() });
  }


  getOrders(): Observable<any> {
    return this.http.get(`${this.prefix}/api/orders`, { headers: this.getAuthHeaders() });
  }

  createOrder(data: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/orders`, data, { headers: this.getAuthHeaders() });
  }


  getPayments(): Observable<any> {
    return this.http.get(`${this.prefix}/api/payments`, { headers: this.getAuthHeaders() });
  }

  createPayment(data: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/payments`, data, { headers: this.getAuthHeaders() });
  }

  checkout(data: any): Observable<any> {
    return this.http.post(`${this.prefix}/api/checkout`, data, { headers: this.getAuthHeaders() });
  }


  getInvoices(): Observable<any> {
    return this.http.get(`${this.prefix}/api/invoices`, { headers: this.getAuthHeaders() });
  }

  getShipments(): Observable<any> {
    return this.http.get(`${this.prefix}/api/shipments`, { headers: this.getAuthHeaders() });
  }

  getStocks(): Observable<any> {
    return this.http.get(`${this.prefix}/api/stocks`, { headers: this.getAuthHeaders() });
  }

  getInventory(): Observable<any> {
    return this.http.get(`${this.prefix}/api/inventory`, { headers: this.getAuthHeaders() });
  }

  getSuppliers(): Observable<any> {
    return this.http.get(`${this.prefix}/api/suppliers`, { headers: this.getAuthHeaders() });
  }

  getPurchaseOrders(): Observable<any> {
    return this.http.get(`${this.prefix}/api/purchase-orders`, { headers: this.getAuthHeaders() });
  }

  getAnalytics(): Observable<any> {
    return this.http.get(`${this.prefix}/api/analytics`, { headers: this.getAuthHeaders() });
  }

  getNotifications(): Observable<any> {
    return this.http.get(`${this.prefix}/api/notifications`, { headers: this.getAuthHeaders() });
  }

  getChatbotMessages(): Observable<any> {
    return this.http.get(`${this.prefix}/api/chatbot-messages`, { headers: this.getAuthHeaders() });
  }
}