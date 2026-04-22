/**
 * Globex Chatbot Service
 * 
 * This service handles all communication with the Globex Chatbot API.
 * Copy this file to your Angular project: src/app/services/chatbot.service.ts
 * 
 * Usage:
 * 1. Import HttpClientModule in app.module.ts
 * 2. Inject this service in your component
 * 3. Call methods as shown in component example below
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';

export interface ChatMessage {
  role: 'user' | 'model';
  text: string;
}

export interface ChatRequest {
  message: string;
  history: ChatMessage[];
  customer_id?: number;
}

export interface ChatResponse {
  reply: string;
  success: boolean;
  timestamp?: string;
  database_context_used?: boolean;
}

export interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  stock: number;
  category?: string;
  rating?: number;
  reviews_count?: number;
}

export interface Order {
  id: number;
  customer_id?: number;
  order_date: string;
  status: string;
  total_amount: number;
  shipping_address?: string;
  estimated_delivery?: string;
}

export interface Customer {
  id: number;
  name: string;
  email: string;
  phone?: string;
  loyalty_points?: number;
  total_purchases?: number;
  member_since?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private apiUrl = 'http://localhost:8000';
  private messageHistory: ChatMessage[] = [];

  constructor(private http: HttpClient) {
    this.initializeService();
  }

  /**
   * Initialize service and check API health
   */
  private initializeService(): void {
    this.healthCheck().subscribe(
      (health) => {
        console.log('✅ Chatbot API Status:', health);
      },
      (error) => {
        console.error('❌ Chatbot API is not responding:', error);
      }
    );
  }

  /**
   * Check if API and database are running
   */
  healthCheck(): Observable<any> {
    return this.http.get(`${this.apiUrl}/health`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Send a message to the chatbot and get a response
   * Automatically maintains conversation history
   */
  sendMessage(message: string, customerId?: number): Observable<ChatResponse> {
    const request: ChatRequest = {
      message,
      history: this.messageHistory,
      customer_id: customerId
    };

    return this.http.post<ChatResponse>(`${this.apiUrl}/chat`, request).pipe(
      retry(1),
      catchError(this.handleError)
    );
  }

  /**
   * Add a message to local history for context
   * Call this after receiving a response
   */
  addMessageToHistory(role: 'user' | 'model', text: string): void {
    this.messageHistory.push({ role, text });
  }

  /**
   * Clear conversation history
   */
  clearHistory(): void {
    this.messageHistory = [];
  }

  /**
   * Get current message history
   */
  getHistory(): ChatMessage[] {
    return this.messageHistory;
  }

  /**
   * Search products by keyword
   */
  searchProducts(searchTerm: string, limit: number = 10): Observable<any> {
    return this.http.post(`${this.apiUrl}/products/search`, {
      search_term: searchTerm,
      limit
    }).pipe(
      retry(1),
      catchError(this.handleError)
    );
  }

  /**
   * Get detailed product information with reviews
   */
  getProductDetails(productId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/products/${productId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Check if a product is in stock
   */
  checkAvailability(productId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/products/availability/${productId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get products from a specific category
   */
  getProductsByCategory(category: string, limit: number = 20): Observable<any> {
    return this.http.get(`${this.apiUrl}/categories/${category}?limit=${limit}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Track order status and details
   */
  trackOrder(orderId: number): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/orders/track`, {
      order_id: orderId
    }).pipe(
      retry(1),
      catchError(this.handleError)
    );
  }

  /**
   * Get all active promotions and discounts
   */
  getPromotions(): Observable<any> {
    return this.http.get(`${this.apiUrl}/promotions`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get customer profile and order history
   */
  getCustomerInfo(customerId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/customers/${customerId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Handle HTTP errors
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      if (error.error && error.error.detail) {
        errorMessage = error.error.detail;
      }
    }

    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}

/**
 * ============================================================================
 * EXAMPLE COMPONENT USAGE
 * ============================================================================
 * 
 * In your ChatbotComponent:
 * 
 * import { Component, OnInit } from '@angular/core';
 * import { ChatbotService, ChatMessage } from './services/chatbot.service';
 * 
 * @Component({
 *   selector: 'app-chatbot',
 *   templateUrl: './chatbot.component.html',
 *   styleUrls: ['./chatbot.component.css']
 * })
 * export class ChatbotComponent implements OnInit {
 *   messages: ChatMessage[] = [];
 *   userInput = '';
 *   isLoading = false;
 *   customerId: number | null = null;
 * 
 *   constructor(private chatbotService: ChatbotService) {}
 * 
 *   ngOnInit(): void {
 *     // Optional: Load customer ID from authentication service
 *     // this.customerId = this.authService.getCurrentUserId();
 *   }
 * 
 *   sendMessage(): void {
 *     if (!this.userInput.trim()) return;
 * 
 *     // Add user message to display
 *     this.messages.push({
 *       role: 'user',
 *       text: this.userInput
 *     });
 *     this.chatbotService.addMessageToHistory('user', this.userInput);
 * 
 *     // Send to API
 *     this.isLoading = true;
 *     this.chatbotService.sendMessage(this.userInput, this.customerId).subscribe(
 *       (response) => {
 *         this.messages.push({
 *           role: 'model',
 *           text: response.reply
 *         });
 *         this.chatbotService.addMessageToHistory('model', response.reply);
 *         this.userInput = '';
 *         this.isLoading = false;
 *       },
 *       (error) => {
 *         this.messages.push({
 *           role: 'model',
 *           text: 'Sorry, I encountered an error. Please try again.'
 *         });
 *         this.isLoading = false;
 *       }
 *     );
 *   }
 * 
 *   searchProducts(): void {
 *     this.chatbotService.searchProducts('laptop').subscribe(
 *       (response) => {
 *         console.log('Products:', response.products);
 *       }
 *     );
 *   }
 * 
 *   trackMyOrder(orderId: number): void {
 *     this.chatbotService.trackOrder(orderId).subscribe(
 *       (order) => {
 *         console.log('Order status:', order.status);
 *       }
 *     );
 *   }
 * }
 * 
 * ============================================================================
 * TEMPLATE EXAMPLE (chatbot.component.html)
 * ============================================================================
 * 
 * <div class="chatbot-container">
 *   <div class="messages-container">
 *     <div *ngFor="let msg of messages" [ngClass]="msg.role">
 *       <p>{{ msg.text }}</p>
 *     </div>
 *   </div>
 * 
 *   <div class="input-container">
 *     <input
 *       [(ngModel)]="userInput"
 *       (keyup.enter)="sendMessage()"
 *       [disabled]="isLoading"
 *       placeholder="Ask me about products, orders, or promotions..."
 *     />
 *     <button (click)="sendMessage()" [disabled]="isLoading || !userInput.trim()">
 *       {{ isLoading ? 'Sending...' : 'Send' }}
 *     </button>
 *   </div>
 * </div>
 */
