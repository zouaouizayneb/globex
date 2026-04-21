import { Component } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { NavBarComponent } from './nav-bar/nav-bar.component';
import { FooterComponent } from './footer/footer.component';
import { CommonModule } from '@angular/common';
import { ChatbotIconComponent } from './chatbot-icon/chatbot-icon.component';

@Component({
  selector: 'app-root',
  imports: [RouterModule, NavBarComponent, FooterComponent, CommonModule, ChatbotIconComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'your-app-name';

  constructor(private router: Router) { }

  isAdminRoute(): boolean {
    return this.router.url.startsWith('/admin');
  }
}
