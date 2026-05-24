import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ShopService } from '../shared/shop.services';
import { ServicesService } from '../../services/services.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {

  // ── Contact ──────────────────────────────────────────────────────────
  contact = {
    email: '',
    emailOffers: false
  };

  // ── Delivery ─────────────────────────────────────────────────────────
  delivery = {
    country: 'TN',
    firstName: '',
    lastName: '',
    address: '',
    apartment: '',
    city: '',
    state: '',
    postcode: ''
  };

  // ── Shipping ─────────────────────────────────────────────────────────
  selectedShipping: 'standard' | 'express' = 'standard';
  shippingOptions = [
    { id: 'standard', label: 'Standard Shipping', price: 7.99, eta: '5–7 business days' },
    { id: 'express', label: 'Express Shipping',   price: 14.99, eta: '1–3 business days' }
  ];

  // ── Payment ───────────────────────────────────────────────────────────
  // Payment method selection
  selectedPaymentMethod: 'PAYPAL' | 'CASH_ON_DELIVERY' = 'PAYPAL';

  // Non-Tunisian card info (for future use)
  card = {
    number: '',
    expiry: '',
    cvv: '',
    name: '',
    useBillingAddress: true
  };

  // ── State ─────────────────────────────────────────────────────────────
  isPlacing = false;
  orderPlaced = false;
  placedOrderId = '';
  errorMsg = '';

  // ── Countries list ────────────────────────────────────────────────────
  countries = [
    { code: 'TN', name: 'Tunisia' },
    { code: 'US', name: 'United States' },
    { code: 'GB', name: 'United Kingdom' },
    { code: 'FR', name: 'France' },
    { code: 'DE', name: 'Germany' },
    { code: 'DZ', name: 'Algeria' },
    { code: 'MA', name: 'Morocco' },
    { code: 'LY', name: 'Libya' },
    { code: 'EG', name: 'Egypt' },
    { code: 'SA', name: 'Saudi Arabia' },
    { code: 'AE', name: 'United Arab Emirates' },
    { code: 'IT', name: 'Italy' },
    { code: 'ES', name: 'Spain' },
    { code: 'CA', name: 'Canada' },
    { code: 'AU', name: 'Australia' },
    { code: 'JP', name: 'Japan' },
    { code: 'CN', name: 'China' },
    { code: 'IN', name: 'India' },
    { code: 'BR', name: 'Brazil' },
    { code: 'MX', name: 'Mexico' }
  ];

  constructor(
    public shop: ShopService,
    private api: ServicesService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Pre-fill email from stored user
    try {
      const raw = localStorage.getItem('user');
      if (raw) {
        const u = JSON.parse(raw);
        this.contact.email = u.email || u.username || '';
        this.delivery.firstName = u.firstName || u.prenom || '';
        this.delivery.lastName  = u.lastName  || u.nom   || '';
      }
    } catch { /* ignore */ }
  }

  // ── Computed ──────────────────────────────────────────────────────────
  get isTunisian(): boolean {
    return this.delivery.country === 'TN';
  }

  get cartItems() { return this.shop.getCart(); }
  get subtotal(): number { return this.shop.subtotal; }

  get shippingCost(): number {
    if (this.subtotal >= 99) return 0;
    return this.selectedShipping === 'express' ? 14.99 : 7.99;
  }

  get taxes(): number {
    return parseFloat(((this.subtotal + this.shippingCost) * 0.09).toFixed(2));
  }

  get total(): number {
    return parseFloat((this.subtotal + this.shippingCost + this.taxes).toFixed(2));
  }

  get itemCount(): number { return this.shop.cartCount; }

  // ── Payment method helper ───────────────────────────────────────────────
  setPaymentMethod(method: 'PAYPAL' | 'CASH_ON_DELIVERY') {
    this.selectedPaymentMethod = method;
  }

  get availablePaymentMethods() {
    const methods = [{ id: 'PAYPAL', label: 'PayPal (Online)', icon: '💳' }];
    if (this.delivery.country === 'TN') {
      methods.push({ id: 'CASH_ON_DELIVERY', label: 'Cash on Delivery', icon: '💵' });
    }
    return methods;
  }

  // ── Format helpers ────────────────────────────────────────────────────
  formatCardNumber(val: string) {
    this.card.number = val.replace(/\D/g, '').replace(/(.{4})/g, '$1 ').trim().slice(0, 19);
  }

  formatExpiry(val: string) {
    const clean = val.replace(/\D/g, '');
    if (clean.length >= 3) {
      this.card.expiry = clean.slice(0, 2) + ' / ' + clean.slice(2, 4);
    } else {
      this.card.expiry = clean;
    }
  }

  // ── Place order ───────────────────────────────────────────────────────
  placeOrder() {
    console.log('=== placeOrder called ===');

    // Simple token presence check — the backend JWT filter is the authority.
    // If the token is expired/invalid, the backend returns 401 and the
    // error handler below redirects to /login automatically.
    const token = localStorage.getItem('token');
    console.log('Token exists:', !!token);

    if (!token) {
      console.log('No token found — redirecting to login');
      this.router.navigate(['/login']);
      return;
    }

    if (this.isPlacing) {
      console.log('Already placing order, returning');
      return;
    }

    // Validate required fields
    if (!this.contact.email || !this.delivery.firstName || !this.delivery.lastName ||
        !this.delivery.address || !this.delivery.city || !this.delivery.postcode) {
      this.errorMsg = 'Please fill in all required delivery fields.';
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    this.errorMsg = '';
    this.isPlacing = true;
    console.log('Setting isPlacing to true');

    // Determine payment method label
    let paymentMethod = this.selectedPaymentMethod;
    console.log('Payment method:', paymentMethod);

    // Build order payload
    const orderPayload = {
      email: this.contact.email,
      shippingAddress: [
        this.delivery.firstName + ' ' + this.delivery.lastName,
        this.delivery.address,
        this.delivery.apartment,
        this.delivery.city,
        this.delivery.state,
        this.delivery.postcode,
        this.countries.find(c => c.code === this.delivery.country)?.name
      ].filter(Boolean).join(', '),
      country: this.delivery.country,
      shippingMethod: this.selectedShipping,
      paymentMethod,
      subtotal: this.subtotal,
      shippingCost: this.shippingCost,
      taxes: this.taxes,
      total: this.total,
      items: this.cartItems.map(item => ({
        productId: item.id,
        productName: item.name,
        quantity: item.quantity ?? 1,
        unitPrice: item.price
      })),
      status: 'PENDING'
    };

    console.log('Order payload:', orderPayload);
    console.log('Token being used:', token ? token.substring(0, 30) + '...' : 'none');

    this.api.createOrder(orderPayload).subscribe({
      next: (res: any) => {
        console.log('Order created successfully:', res);
        const orderId = res?.id || res?.orderId || res?.orderNumber;
        console.log('Order ID:', orderId);
        
        // Decide if we need to initiate a payment gateway redirect
        if (paymentMethod !== 'CASH_ON_DELIVERY') {
          console.log('Initiating payment for PayPal');
          // It's an API payment (PayPal)
          this.api.initiatePayment({
            orderId: String(orderId),
            amount: this.total,
            currency: this.delivery.country === 'TN' ? 'TND' : 'USD', // Simple currency map
            country: this.delivery.country,
            paymentMethod: paymentMethod,
            description: `Order ${orderId}`,
            returnUrl: window.location.origin + '/payment-result',
            cancelUrl: window.location.origin + '/payment-result'
          }).subscribe({
            next: (paymentRes: any) => {
              console.log('Payment initiated:', paymentRes);
              if (paymentRes.paymentUrl) {
                // Clear cart before redirecting
                this.shop.cart = [];
                localStorage.removeItem('cart');
                
                // Redirect user to the gateway (PayPal)
                console.log('Redirecting to:', paymentRes.paymentUrl);
                window.location.href = paymentRes.paymentUrl;
              } else {
                console.error('No payment URL returned');
                this.isPlacing = false;
                this.errorMsg = 'Payment initiation failed: no URL returned.';
              }
            },
            error: (payErr) => {
              console.error('Payment initiation error:', payErr);
              this.isPlacing = false;
              this.errorMsg = 'Failed to connect to the payment provider. ' + (payErr.error?.message || '');
            }
          });
        } else {
          console.log('Cash on delivery - completing order');
          // Standard offline order
          this.placedOrderId = orderId || ('ORD-' + Date.now());
          this.shop.cart = [];
          localStorage.removeItem('cart');
          this.orderPlaced = true;
          this.isPlacing = false;
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
      },
      error: (err) => {
        console.error('Order creation error full object:', err);
        console.error('Status:', err.status);
        console.error('Message:', err.message);
        console.error('Error Body:', err.error);
        
        this.isPlacing = false;
        
        // Temporarily, let's show an alert so the user can tell us what the error is
        if (err.status === 401 || err.status === 403) {
          alert('HTTP ' + err.status + ' Error. Check console. Server rejected token or CORS blocked it. Not redirecting so you can read this.');
          // Temporarily disable redirecting to login to prevent the loop
          // console.log('Server rejected token (401/403) — clearing session and redirecting');
          // localStorage.removeItem('token');
          // localStorage.removeItem('user');
          // this.router.navigate(['/login']);
        } else if (err.status === 400) {
          this.errorMsg = err.error?.message || 'Invalid order information. Please review your details and try again.';
          window.scrollTo({ top: 0, behavior: 'smooth' });
        } else if (err.status === 404) {
          this.errorMsg = 'One or more items in your cart are no longer available.';
          window.scrollTo({ top: 0, behavior: 'smooth' });
        } else {
          this.errorMsg = 'Unable to place your order right now. Please try again in a moment.';
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
      }
    });
  }

  continueShopping() {
    this.router.navigate(['/home']);
  }
}
