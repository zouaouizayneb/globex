import { Component, OnInit, AfterViewChecked, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Router } from '@angular/router';

interface ChatMessage {
  role: 'user' | 'bot';
  text: string;
  timestamp: Date;
}

interface ApiMessage {
  role: 'user' | 'model';
  text: string;
}

interface ChatResponse {
  reply: string;
  database_context_used?: boolean;
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent implements OnInit, AfterViewChecked {

  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  messages: ChatMessage[] = [];
  userInput: string = '';
  isLoading: boolean = false;
  customerName: string = 'Guest';
  customerId: number | null = null;

  // ── Change this to your production URL when you deploy ────────────────────
  private readonly API_URL = 'http://localhost:8000';

  constructor(
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    // Check if user is logged in and get their info
    this.loadCustomerInfo();
    
    // Add personalized greeting
    const greeting = this.customerId 
      ? `Hello ${this.customerName}! I'm your Globex AI assistant. How can I help you today?`
      : "Hello! I'm your Globex AI assistant. How can I help you today?";
    
    this.messages.push({
      role: 'bot',
      text: greeting,
      timestamp: new Date()
    });

    // Optional: Check API health
    this.checkApiHealth();
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  /**
   * Load customer info if logged in (integrate with your auth service)
   */
  private loadCustomerInfo(): void {
    // TODO: Get from your authentication service
    // Example: this.customerId = this.authService.getCurrentUserId();
    // Example: this.customerName = this.authService.getCurrentUserName();
  }

  /**
   * Optional: Verify API is running
   */
  private checkApiHealth(): void {
    this.http.get(`${this.API_URL}/health`).subscribe({
      next: (response: any) => {
        if (response.database === 'connected') {
          console.log('✅ Chatbot API healthy and database connected');
        }
      },
      error: (err) => {
        console.warn('⚠️ Chatbot API unavailable:', err.message);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/home']);
  }

  sendMessage(): void {
    const text = this.userInput.trim();
    if (!text || this.isLoading) return;

    // Add the user's message to the visible chat
    this.messages.push({ role: 'user', text, timestamp: new Date() });
    this.userInput = '';
    this.isLoading = true;

    // Build the conversation history to send (all previous turns, excluding the
    // initial bot greeting which is handled by the system prompt server-side)
    const history: ApiMessage[] = this.messages
      .slice(1, -1) // skip greeting + the message we just pushed
      .map(m => ({
        role: m.role === 'user' ? 'user' : 'model',
        text: m.text
      }));

    // Send request with optional customer ID for personalization
    this.http
      .post<ChatResponse>(`${this.API_URL}/chat`, {
        message: text,
        history,
        customer_id: this.customerId // Enables personalized responses
      })
      .subscribe({
        next: (res) => {
          this.messages.push({
            role: 'bot',
            text: res.reply,
            timestamp: new Date()
          });
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Chatbot API error:', err);
          const errorMessage = err.error?.detail 
            ? `Error: ${err.error.detail}`
            : 'Sorry, I encountered an error. Please try again in a moment.';
          
          this.messages.push({
            role: 'bot',
            text: errorMessage,
            timestamp: new Date()
          });
          this.isLoading = false;
        }
      });
  }

  /**
   * Optional: Search products directly
   */
  searchProducts(searchTerm: string): void {
    this.http
      .post<any>(`${this.API_URL}/products/search`, {
        search_term: searchTerm,
        limit: 5
      })
      .subscribe({
        next: (res) => {
          console.log('Products found:', res.products);
        },
        error: (err) => {
          console.error('Search error:', err);
        }
      });
  }

  /**
   * Optional: Track order directly
   */
  trackOrder(orderId: number): void {
    this.http
      .post<any>(`${this.API_URL}/orders/track`, {
        order_id: orderId
      })
      .subscribe({
        next: (res) => {
          console.log('Order status:', res.status);
        },
        error: (err) => {
          console.error('Tracking error:', err);
        }
      });
  }

  handleKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  getTimeString(date: Date): string {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  private scrollToBottom(): void {
    try {
      const el = this.messagesContainer.nativeElement;
      el.scrollTop = el.scrollHeight;
    } catch (_) {}
  }
}