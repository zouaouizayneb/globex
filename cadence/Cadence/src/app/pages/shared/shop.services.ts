import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Product {
  id: number;
  name: string;
  price: number;
  oldPrice?: number;
  image: string;
  quantity?: number;
  rating: number;
  colors: string[];
  isNew?: boolean;
  discount?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ShopService {
  cart: Product[] = [];
  wishlist: Product[] = [];

  // Global cart UI state
  public cartOpen$ = new BehaviorSubject<boolean>(false);

  constructor() {
    // Restore cart from localStorage on startup
    const saved = localStorage.getItem('cart');
    if (saved) {
      try {
        this.cart = JSON.parse(saved);
      } catch {
        this.cart = [];
      }
    }

    // Restore wishlist from localStorage on startup
    const savedWishlist = localStorage.getItem('wishlist');
    if (savedWishlist) {
      try {
        this.wishlist = JSON.parse(savedWishlist);
      } catch {
        this.wishlist = [];
      }
    }
  }

  private saveCart() {
    localStorage.setItem('cart', JSON.stringify(this.cart));
  }

  private saveWishlist() {
    localStorage.setItem('wishlist', JSON.stringify(this.wishlist));
  }

  addToCart(product: Product) {
    const found = this.cart.find(item => item.id === product.id);
    if (found) {
      found.quantity! += 1;
    } else {
      this.cart.push({ ...product, quantity: 1 });
    }
    this.saveCart();
  }

  addToWishlist(product: Product) {
    if (!this.wishlist.find(item => item.id === product.id)) {
      this.wishlist.push({ ...product });
      this.saveWishlist();
    }
  }

  getCart() {
    return this.cart;
  }

  getWishlist() {
    return this.wishlist;
  }

  removeFromCart(productId: number) {
    this.cart = this.cart.filter(item => item.id !== productId);
    this.saveCart();
  }

  removeFromWishlist(productId: number) {
    this.wishlist = this.wishlist.filter(item => item.id !== productId);
    this.saveWishlist();
  }

  updateQuantity(productId: number, delta: number) {
    const item = this.cart.find(i => i.id === productId);
    if (item) {
      item.quantity = Math.max(1, (item.quantity ?? 1) + delta);
      this.saveCart();
    }
  }

  get cartCount(): number {
    return this.cart.reduce((sum, item) => sum + (item.quantity ?? 0), 0);
  }

  get subtotal(): number {
    return this.cart.reduce((sum, item) => sum + item.price * (item.quantity ?? 1), 0);
  }
}