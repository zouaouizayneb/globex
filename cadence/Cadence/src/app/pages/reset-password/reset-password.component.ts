import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  token: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';
  showPassword: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    console.log('Reset password token:', this.token);
    
    if (!this.token) {
      this.errorMessage = 'Invalid or missing reset token. Please request a new password reset.';
    }
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  resetPassword() {
    console.log('=== Reset Password ===');
    console.log('Token:', this.token);
    
    if (!this.token) {
      this.errorMessage = 'Invalid or missing reset token. Please request a new password reset.';
      return;
    }

    if (!this.newPassword || this.newPassword.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters long.';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.isLoading = true;

    const payload = {
      token: this.token,
      newPassword: this.newPassword
    };

    console.log('Sending reset password request:', { token: this.token, newPassword: '***' });

    this.http.post('http://localhost:8080/api/auth/reset-password', payload).subscribe({
      next: (response: any) => {
        console.log('Password reset successful:', response);
        this.isLoading = false;
        this.successMessage = 'Password has been reset successfully. You can now login with your new password.';
        
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 3000);
      },
      error: (error) => {
        console.error('Password reset error:', error);
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Failed to reset password. The token may be invalid or expired.';
      }
    });
  }
}
