import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ShopService } from '../shared/shop.services';

import { AppCurrencyPipe } from '../../pipes/currency.pipe';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, AppCurrencyPipe],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {
  isOpen = false;
  cartItems: any[] = [];

  constructor(public shop: ShopService, private router: Router) {}

  ngOnInit(): void {
    this.shop.cart$.subscribe(items => this.cartItems = items);
  }

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


  get totalItems(): number {
    return this.shop.cartCount;
  }

  get subtotal(): number {
    return this.shop.subtotal;
  }
}