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

  // ── Change this to your production URL when you deploy ────────────────────
  private readonly API_URL = 'http://localhost:8000';

  constructor(
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.messages.push({
      role: 'bot',
      text: "Hello! I'm your Globex AI assistant. How can I help you today?",
      timestamp: new Date()
    });
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
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

    this.http
      .post<ChatResponse>(`${this.API_URL}/chat`, { message: text, history })
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
          this.messages.push({
            role: 'bot',
            text: 'Sorry, I encountered an error. Please try again in a moment.',
            timestamp: new Date()
          });
          this.isLoading = false;
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