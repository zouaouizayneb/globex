import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ServicesService } from '../../services/services.service';
import { AuthService } from '../../services/auth.service';

type ViewType = 'login' | 'register' | 'forgot';

interface LoginPayload {
  username: string;
  password: string;
}

interface RegisterPayload {
  username: string;
  fullname: string;
  email: string;
  phoneNumber: string;
  password: string;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  // ─── View State ───────────────────────────────────────────────
  currentView: ViewType = 'login';
  isLoading = false;

  // ─── Login Fields ─────────────────────────────────────────────
  loginIdentifier = '';
  loginPassword = '';

  // ─── Register Fields ──────────────────────────────────────────
  regFullname = '';
  regEmail = '';
  regUsername = '';
  regPhone = '';
  regAddress = '';    // kept for HTML binding (not sent to backend)
  regCountry = '';    // kept for HTML binding (not sent to backend)
  regPassword = '';
  regConfirmPassword = '';

  // ─── Forgot Password Field ────────────────────────────────────
  forgotEmail = '';

  constructor(
    private services: ServicesService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    if (localStorage.getItem('token')) {
      this.authService.redirectByRole();
    }
  }

  // ─── Validators ───────────────────────────────────────────────

  private isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
  }

  private isValidPhone(phone: string): boolean {
    return /^[+]?[\d\s\-().]{6,20}$/.test(phone.trim());
  }

  private isStrongPassword(password: string): boolean {
    return password.length >= 6;
  }

  // ─── Login ────────────────────────────────────────────────────

  onLogin(): void {
    const identifier = this.loginIdentifier.trim();
    const password = this.loginPassword;

    if (!identifier) {
      alert('Please enter your email or username.');
      return;
    }
    if (!password) {
      alert('Please enter your password.');
      return;
    }

    const payload: LoginPayload = {
      username: identifier,
      password: password
    };

    this.isLoading = true;
    this.services.login(payload).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        this.handleLoginSuccess(res);
      },
      error: (err: any) => {
        this.isLoading = false;
        this.handleLoginError(err);
      }
    });
  }

  private handleLoginSuccess(res: any): void {
    if (res?.token) {
      localStorage.setItem('token', res.token);
    }
    const user = res?.user ?? (res?.username || res?.email ? res : null);
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
    }
    this.authService.redirectByRole();
  }

  private handleLoginError(err: any): void {
    console.error('Full login error:', JSON.stringify(err.error));

    if (err.status === 0) {
      alert('Cannot connect to the server. Please check your connection.');
    } else if (err.status === 400) {
      const errors = err.error?.errors || err.error?.fieldErrors;
      if (errors && Array.isArray(errors)) {
        const messages = errors.map((e: any) => e.defaultMessage || e.message).join(', ');
        alert('Validation error: ' + messages);
      } else {
        alert(err.error?.message || 'Bad request: ' + JSON.stringify(err.error));
      }
    } else if (err.status === 401) {
      alert('Invalid credentials. Please check your username and password.');
    } else if (err.status === 403) {
      alert('Your account is inactive or not verified. Please check your email.');
    } else {
      alert(err.error?.message || 'Login failed. Please try again.');
    }
  }

  // ─── Register ─────────────────────────────────────────────────

  onRegister(): void {
    if (!this.validateRegisterForm()) return;

    // Only send fields the backend RegisterRequest expects
    const payload: RegisterPayload = {
      username: this.regUsername.trim() || this.regEmail.trim(),
      fullname: this.regFullname.trim(),
      email: this.regEmail.trim().toLowerCase(),
      phoneNumber: this.regPhone.trim(),
      password: this.regPassword
    };

    this.isLoading = true;
    this.services.register(payload).subscribe({
      next: () => {
        this.isLoading = false;
        alert('Account created successfully! Please log in.');
        this.switchView('login');
      },
      error: (err: any) => {
        this.isLoading = false;
        this.handleRegisterError(err);
      }
    });
  }

  private validateRegisterForm(): boolean {
    const checks: { condition: boolean; message: string }[] = [
      { condition: !this.regFullname.trim(),                          message: 'Please enter your full name.' },
      { condition: !this.regUsername.trim(),                          message: 'Please enter a username.' },
      { condition: this.regUsername.trim().length < 3,                message: 'Username must be at least 3 characters.' },
      { condition: this.regUsername.trim().length > 20,               message: 'Username must be less than 20 characters.' },
      { condition: !this.regEmail.trim(),                             message: 'Please enter your email address.' },
      { condition: !this.isValidEmail(this.regEmail),                 message: 'Please enter a valid email address.' },
      { condition: !this.regPhone.trim(),                             message: 'Please enter your phone number.' },
      { condition: !this.isValidPhone(this.regPhone),                 message: 'Please enter a valid phone number.' },
      { condition: !this.regAddress.trim(),                           message: 'Please enter your address.' },
      { condition: !this.regCountry.trim(),                           message: 'Please enter your country.' },
      { condition: !this.regPassword,                                 message: 'Please enter your password.' },
      { condition: !this.isStrongPassword(this.regPassword),          message: 'Password must be at least 6 characters.' },
      { condition: !this.regConfirmPassword,                          message: 'Please confirm your password.' },
      { condition: this.regPassword !== this.regConfirmPassword,      message: 'Passwords do not match.' },
    ];

    for (const check of checks) {
      if (check.condition) {
        alert(check.message);
        return false;
      }
    }
    return true;
  }

  private handleRegisterError(err: any): void {
    console.error('Full register error:', JSON.stringify(err.error));

    if (err.status === 0) {
      alert('Cannot connect to the server. Please check your connection.');
      return;
    }
    if (err.status === 409) {
      alert('An account with this email or username already exists.');
      return;
    }
    if (err.status === 400) {
      const errors = err.error?.errors || err.error?.fieldErrors;
      if (errors && Array.isArray(errors)) {
        const messages = errors.map((e: any) => e.defaultMessage || e.message).join(', ');
        alert('Validation error: ' + messages);
      } else {
        alert(err.error?.message || 'Bad request: ' + JSON.stringify(err.error));
      }
      return;
    }

    const errBody = err.error;
    let msg = 'Registration failed. ';
    if (typeof errBody === 'string') {
      msg += errBody;
    } else if (errBody?.message) {
      msg += errBody.message;
    } else if (errBody?.errors) {
      msg += Object.values(errBody.errors).join(' ');
    } else {
      msg += err.message || 'Unknown error.';
    }
    alert(msg);
  }

  // ─── Forgot Password ──────────────────────────────────────────

  onForgotPassword(): void {
    const email = this.forgotEmail.trim();
    if (!email) {
      alert('Please enter your email address.');
      return;
    }
    if (!this.isValidEmail(email)) {
      alert('Please enter a valid email address.');
      return;
    }
    this.isLoading = true;
    this.services.forgotPassword(email).subscribe({
      next: () => {
        this.isLoading = false;
        alert('If an account exists with this email, a password reset link has been sent.');
        this.switchView('login');
      },
      error: () => {
        this.isLoading = false;
        alert('If an account exists with this email, a password reset link has been sent.');
        this.switchView('login');
      }
    });
  }

  // ─── View Switching ───────────────────────────────────────────

  switchView(view: ViewType): void {
    this.currentView = view;
    this.resetAllForms();
  }

  private resetAllForms(): void {
    this.loginIdentifier = '';
    this.loginPassword = '';
    this.regFullname = '';
    this.regEmail = '';
    this.regUsername = '';
    this.regPhone = '';
    this.regAddress = '';
    this.regCountry = '';
    this.regPassword = '';
    this.regConfirmPassword = '';
    this.forgotEmail = '';
  }
}