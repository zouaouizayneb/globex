import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

interface DecodedToken {
  role?: string;
  email?: string;
  sub?: string;
  [key: string]: any;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private router: Router) { }

  // Decode JWT token to extract role
  decodeToken(token: string): DecodedToken | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        return null;
      }

      // Decode the payload 
      const decoded = JSON.parse(atob(parts[1]));
      return decoded;
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  // Get user role from stored token
  getUserRole(): string | null {
    const token = localStorage.getItem('token');
    if (!token) {
      return null;
    }

    const decoded = this.decodeToken(token);
    return decoded?.role || null;
  }

  isAdmin(): boolean {
    return this.getUserRole() === 'ADMIN';
  }

  isClient(): boolean {
    return this.getUserRole() === 'CLIENT';
  }

  redirectByRole(): void {
    const role = this.getUserRole();
    if (role === 'ADMIN') {
      this.router.navigate(['/admin/dashboard']);
    } else if (role === 'CLIENT') {
      this.router.navigate(['/home']);
    } else {
      this.router.navigate(['/login']);
    }
  }

  logout(): void {
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
}
