import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-payment-result',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './payment-result.component.html',
  styleUrls: ['./payment-result.component.css']
})
export class PaymentResultComponent implements OnInit {

  status: 'loading' | 'success' | 'error' = 'loading';
  message: string = 'Verifying your payment...';
  orderId: string = '';
  private apiUrl = 'http://localhost:8080/api';

  constructor(
    private route: ActivatedRoute, 
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    // Collect specific parameters from multiple gateways
    this.route.queryParams.subscribe(params => {
      const method = params['method']; // We passed this in our initiate request
      
      const paymentId = params['paymentId']; // PayPal
      const payerId = params['PayerID']; // PayPal
      
      const flouciPaymentId = params['payment_id']; // Flouci / D17

      if (paymentId && payerId) {
        // PayPal Verification
        this.verifyPayPal(paymentId, payerId);
      } else if (flouciPaymentId) {
        // Flouci / D17 Verification
        const resolvedMethod = method || 'FLOUCI';
        this.verifyFlouciOrD17(flouciPaymentId, resolvedMethod);
      } else {
        this.status = 'error';
        this.message = 'No payment reference found. Please check your order history.';
      }
    });
  }

  private verifyPayPal(paymentId: string, payerId: string): void {
    this.http.post(`${this.apiUrl}/payment/paypal/execute`, { paymentId, payerId }).subscribe({
      next: (res: any) => {
        this.status = 'success';
        this.message = 'Payment successfully processed via PayPal!';
        // PayPal response includes order info usually
      },
      error: (err) => {
        console.error('PayPal verification failed', err);
        this.status = 'error';
        this.message = 'Verification failed. Please contact support if you were charged.';
      }
    });
  }

  private verifyFlouciOrD17(paymentId: string, method: string): void {
    // If it's D17, we hit our D17 verify endpoint, otherwise Flouci
    const endpoint = method === 'D17' ? 'd17' : 'flouci';
    
    this.http.get(`${this.apiUrl}/payment/${endpoint}/verify/${paymentId}`, { responseType: 'text' }).subscribe({
      next: (res: any) => {
        this.status = 'success';
        this.message = `Payment successfully processed via ${method}!`;
        this.orderId = paymentId;
      },
      error: (err) => {
        console.error(`${method} verification failed`, err);
        this.status = 'error';
        this.message = 'Verification failed. Please contact support if you were charged.';
      }
    });
  }

  goToHome() {
    this.router.navigate(['/home']);
  }
}
