import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-account',
  imports: [FormsModule , RouterModule,CommonModule], 
  templateUrl: './accounte.component.html',
  styleUrl: './accounte.component.css'
})
export class AccounteComponent {
  name: string = '';
  email: string = '';

  constructor(private router: Router) {
    const userStr = localStorage.getItem('user');
    let foundName = '';
    let foundEmail = '';

    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        foundName = user.fullname || user.name || (user.firstName ? (user.firstName + ' ' + (user.lastName || '')).trim() : '') || user.username || '';
        foundEmail = user.email || '';
      } catch (e) {
        console.error('Error parsing user data', e);
      }
    }

    // Fallback: try to decode token
    const token = localStorage.getItem('token');
    if (token && (!foundName || !foundEmail)) {
      try {
        const payloadStr = atob(token.split('.')[1]);
        const payload = JSON.parse(payloadStr);
        if (!foundName) foundName = payload.fullname || payload.name || payload.username || '';
        if (!foundEmail) foundEmail = payload.email || '';
        
        let sub = payload.sub || '';
        if (sub) {
          if (sub.includes('@')) {
            if (!foundEmail) foundEmail = sub;
            if (!foundName) foundName = sub.split('@')[0];
          } else {
            if (!foundName) foundName = sub;
          }
        }
      } catch (e) {
        // Not a standard JWT or failed to parse
      }
    }

    if (foundName && foundEmail && foundName === foundEmail) {
      foundName = foundName.split('@')[0];
    }

    this.name = foundName || 'User';
    this.email = foundEmail || 'No email provided';
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }
}
