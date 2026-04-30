import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = localStorage.getItem('token');

  console.log('Interceptor - Request URL:', req.url);
  console.log('Interceptor - Token exists:', !!token);
  console.log('Interceptor - Token value:', token ? token.substring(0, 20) + '...' : 'null');

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    console.log('Interceptor - Authorization header added');
  }

  return next(req);
};

