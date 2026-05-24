import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.css']
})
export class VerifyEmailComponent implements OnInit {
  loading = true;
  success = false;
  error = false;
  message = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    console.log('VerifyEmailComponent initialized');
    const token = this.route.snapshot.queryParamMap.get('token');
    console.log('Token from URL:', token);
    
    if (!token) {
      this.error = true;
      this.message = 'Invalid verification link. No token provided.';
      this.loading = false;
      return;
    }

    this.verifyEmail(token);
  }

  verifyEmail(token: string): void {
    console.log('Calling verifyEmail with token:', token);
    this.http.post('http://localhost:8080/api/auth/verify-email', { token })
      .subscribe({
        next: (response: any) => {
          console.log('Verification successful:', response);
          this.success = true;
          this.message = response.message || 'Email verified successfully!';
          this.loading = false;
          
          // Redirect to login after 3 seconds
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 3000);
        },
        error: (err) => {
          console.error('Verification failed:', err);
          console.error('Error status:', err.status);
          console.error('Error message:', err.error?.message);
          this.error = true;
          this.message = err.error?.message || 'Verification failed. The link may be invalid or expired.';
          this.loading = false;
        }
      });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
