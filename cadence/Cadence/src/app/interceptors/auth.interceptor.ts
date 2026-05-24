import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = localStorage.getItem('token');

  console.log('=== INTERCEPTOR DEBUG ===');
  console.log('Request URL:', req.url);
  console.log('Request method:', req.method);
  console.log('Token exists:', !!token);
  console.log('Token value:', token ? token.substring(0, 30) + '...' : 'null');
  console.log('Current headers:', req.headers.keys());

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    console.log('Authorization header added');
    console.log('New headers:', req.headers.keys());
  } else {
    console.log('WARNING: No token found in localStorage');
  }

  console.log('=== END INTERCEPTOR DEBUG ===');

  return next(req).pipe(
    // Catch errors and suppress stock-related alerts
    catchError((error: any) => {
      console.error('HTTP Error:', error);
      // Don't show alerts for stock errors
      if (error.error?.message && error.error.message.includes('stock')) {
        console.log('Stock error suppressed:', error.error.message);
        return throwError(() => error);
      }
      return throwError(() => error);
    })
  );
};

