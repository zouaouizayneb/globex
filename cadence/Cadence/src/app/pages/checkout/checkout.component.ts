import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ShopService } from '../shared/shop.services';
import { ServicesService } from '../../services/services.service';

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
  // Non-Tunisian
  card = {
    number: '',
    expiry: '',
    cvv: '',
    name: '',
    useBillingAddress: true
  };
  paypalSelected = true;  // default for non-TN

  // Tunisian tabs
  tunisianTab: 'D17' | 'Flouci' | 'Virement' = 'D17';

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
    private router: Router
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

  // ── Tunisian tab helper ───────────────────────────────────────────────
  setTunisianTab(tab: 'D17' | 'Flouci' | 'Virement') {
    this.tunisianTab = tab;
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
    if (this.isPlacing) return;

    // Validate required fields
    if (!this.contact.email || !this.delivery.firstName || !this.delivery.lastName ||
        !this.delivery.address || !this.delivery.city || !this.delivery.postcode) {
      this.errorMsg = 'Please fill in all required delivery fields.';
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    this.errorMsg = '';
    this.isPlacing = true;

    // Determine payment method label
    let paymentMethod = 'PAYPAL';
    if (this.isTunisian) {
      paymentMethod = this.tunisianTab === 'D17' ? 'D17' :
                      this.tunisianTab === 'Flouci' ? 'FLOUCI' : 'BANK_TRANSFER';
    }

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

    this.api.createOrder(orderPayload).subscribe({
      next: (res: any) => {
        const orderId = res?.id || res?.orderId || res?.orderNumber;
        
        // Decide if we need to initiate a payment gateway redirect
        if (paymentMethod !== 'CASH_ON_DELIVERY' && paymentMethod !== 'BANK_TRANSFER') {
          // It's an API payment (PayPal, Flouci, D17, etc.)
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
              if (paymentRes.paymentUrl) {
                // Clear cart before redirecting
                this.shop.cart = [];
                localStorage.removeItem('cart');
                
                // Redirect user to the gateway (Flouci, PayPal, etc.) or our simulated D17 page
                window.location.href = paymentRes.paymentUrl;
              } else {
                this.isPlacing = false;
                this.errorMsg = 'Payment initiation failed: no URL returned.';
              }
            },
            error: (payErr) => {
              this.isPlacing = false;
              this.errorMsg = 'Failed to connect to the payment provider. ' + (payErr.error?.message || '');
            }
          });
        } else {
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
        this.isPlacing = false;
        if (err.status === 401 || err.status === 403) {
          this.errorMsg = 'You must be logged in to place an order. Please sign in and try again.';
        } else if (err.status === 400) {
          this.errorMsg = err.error?.message || 'Invalid order information. Please review your details and try again.';
        } else if (err.status === 404) {
          this.errorMsg = 'One or more items in your cart are no longer available.';
        } else {
          this.errorMsg = 'Unable to place your order right now. Please try again in a moment.';
        }
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
  }

  continueShopping() {
    this.router.navigate(['/home']);
  }
}
