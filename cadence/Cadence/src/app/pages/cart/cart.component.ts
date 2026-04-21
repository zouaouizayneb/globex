import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ShopService } from '../shared/shop.services';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent {
  isOpen = false;

  constructor(public shop: ShopService, private router: Router) {}

  open() {
    this.isOpen = true;
    document.body.style.overflow = 'hidden';
  }

  close() {
    this.isOpen = false;
    document.body.style.overflow = '';
  }

  toggle() {
    this.isOpen ? this.close() : this.open();
  }

  increment(item: any) {
    this.shop.updateQuantity(item.id, 1);
  }

  decrement(item: any) {
    this.shop.updateQuantity(item.id, -1);
  }

  removeItem(productId: number) {
    this.shop.removeFromCart(productId);
  }

  goToCheckout() {
    this.close();
    this.router.navigate(['/checkout']);
  }

  get cartItems() {
    return this.shop.getCart();
  }

  get totalItems(): number {
    return this.shop.cartCount;
  }

  get subtotal(): number {
    return this.shop.subtotal;
  }
}