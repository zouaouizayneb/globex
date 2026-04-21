import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-chatbot-icon',
  standalone: true,
  templateUrl: './chatbot-icon.component.html',
  styleUrls: ['./chatbot-icon.component.css']
})
export class ChatbotIconComponent {
  constructor(private router: Router) {}

  openChatbot(): void {
    this.router.navigate(['/chatbot']);
  }
}
