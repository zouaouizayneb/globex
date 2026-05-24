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
  // JWT uses Base64URL (no padding), but atob() requires standard Base64 with padding.
  // We must add padding before calling atob(), otherwise valid tokens fail to parse.
  decodeToken(token: string): DecodedToken | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        return null;
      }
      // Convert Base64URL → Base64 by replacing URL-safe chars and adding padding
      let base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      while (base64.length % 4 !== 0) { base64 += '='; }
      const decoded = JSON.parse(atob(base64));
      return decoded;
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  // Check if the stored token exists and is not expired
  isLoggedIn(): boolean {
    const token = localStorage.getItem('token');
    if (!token) return false;
    return !this.isTokenExpired(token);
  }

  isTokenExpired(token: string): boolean {
    try {
      const decoded = this.decodeToken(token);
      if (!decoded || !decoded['exp']) return false; // no expiry claim → treat as valid
      return Date.now() >= decoded['exp'] * 1000;
    } catch {
      return true;
    }
  }

  // Get user role from stored token
  getUserRole(): string | null {
    const token = localStorage.getItem('token');
    if (!token) {
      return null;
    }

    const decoded = this.decodeToken(token);
    console.log('Decoded token:', decoded);
    console.log('Role from token:', decoded?.role);
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
