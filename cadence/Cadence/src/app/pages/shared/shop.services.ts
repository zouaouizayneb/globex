import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
import { ServicesService } from '../../services/services.service';

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
  variantId?: number; // Added to distinguish between Product ID and Variant ID
  cartItemId?: number; // Added for DB tracking
  wishlistItemId?: number; // Added for DB tracking
  stock?: number; // Added for stock tracking
  // Variant-specific details
  variantColor?: string;
  variantSize?: string;
  variantImage?: string;
  variants?: any[];
}

@Injectable({
  providedIn: 'root'
})
export class ShopService {
  cart: Product[] = [];
  wishlist: Product[] = [];

  // Global cart UI state
  public cartOpen$ = new BehaviorSubject<boolean>(false);
  
  // Observables for state tracking
  private cartSubject = new BehaviorSubject<Product[]>([]);
  public cart$ = this.cartSubject.asObservable();
  
  private wishlistSubject = new BehaviorSubject<Product[]>([]);
  public wishlist$ = this.wishlistSubject.asObservable();

  private api = inject(ServicesService);

  constructor() {
    this.init();
  }

  private async init() {
    const token = localStorage.getItem('token');
    if (token) {
      await this.syncWithBackend();
    } else {
      this.loadFromLocalStorage();
    }
  }

  private loadFromLocalStorage() {
    // Restore cart from localStorage
    const saved = localStorage.getItem('cart');
    if (saved) {
      try {
        this.cart = JSON.parse(saved);
        this.cartSubject.next(this.cart);
      } catch {
        this.cart = [];
      }
    }

    // Restore wishlist from localStorage
    const savedWishlist = localStorage.getItem('wishlist');
    if (savedWishlist) {
      try {
        this.wishlist = JSON.parse(savedWishlist);
        this.wishlistSubject.next(this.wishlist);
      } catch {
        this.wishlist = [];
      }
    }
  }

  public async syncWithBackend() {
    try {
      const dbCart = await firstValueFrom(this.api.getCart());
      if (dbCart && dbCart.items) {
        this.cart = dbCart.items.map((item: any) => ({
          id: item.variantId,
          variantId: item.variantId,
          cartItemId: item.cartItemId,
          name: item.productName,
          price: item.pricePerUnit,
          image: item.imageUrl,
          variantImage: item.imageUrl,
          quantity: item.quantity,
          colors: [],
          rating: 5,
          variantColor: item.color,
          variantSize: item.size
        }));
        this.cartSubject.next(this.cart);
        this.saveCartLocal();
      }

      const dbWishlist = await firstValueFrom(this.api.getWishlist());
      if (dbWishlist && dbWishlist.items) {
        this.wishlist = dbWishlist.items.map((item: any) => ({
          id: item.variantId,
          variantId: item.variantId,
          wishlistItemId: item.wishlistItemId,
          name: item.productName,
          price: item.currentPrice,
          image: item.imageUrl,
          variantImage: item.imageUrl,
          colors: [],
          rating: 5,
          variantColor: item.color,
          variantSize: item.size
        }));
        this.wishlistSubject.next(this.wishlist);
        this.saveWishlistLocal();
      }
    } catch (error) {
      console.error('Failed to sync with backend:', error);
      this.loadFromLocalStorage();
    }
  }

  private saveCartLocal() {
    localStorage.setItem('cart', JSON.stringify(this.cart));
    this.cartSubject.next(this.cart);
  }

  private saveWishlistLocal() {
    localStorage.setItem('wishlist', JSON.stringify(this.wishlist));
    this.wishlistSubject.next(this.wishlist);
  }

  async addToCart(product: Product) {
    // Stock validation is handled by the backend
    const token = localStorage.getItem('token');
    // Use variantId if provided, fall back to id
    const vid = product.variantId || product.id;
    const qty = product.quantity || 1;
    if (token) {
      try {
        await firstValueFrom(this.api.addToCart(vid, qty));
        // Update local cart immediately for UI responsiveness
        const found = this.cart.find(item => item.id === vid);
        if (found) {
          found.quantity! += qty;
        } else {
          this.cart.push({ ...product, id: vid, variantId: vid, quantity: qty });
        }
        this.saveCartLocal();
        await this.syncWithBackend();
      } catch (error: any) {
        console.error('Error adding to cart (DB):', error);
        // Don't show alerts for any errors - let backend validation handle it silently
        // Still add to local cart for guest users
        const found = this.cart.find(item => item.id === vid);
        if (found) {
          found.quantity! += qty;
        } else {
          this.cart.push({ ...product, id: vid, variantId: vid, quantity: qty });
        }
        this.saveCartLocal();
      }
    } else {
      // Guest (no token) – use variantId if present, otherwise fallback to product.id
      const found = this.cart.find(item => item.id === vid);
      if (found) {
        found.quantity! += qty;
      } else {
        // Ensure the stored item uses the variant identifier as its id
        this.cart.push({ ...product, id: vid, variantId: vid, quantity: qty });
      }
      this.saveCartLocal();
    }
  }

  async addToWishlist(product: Product) {
    const token = localStorage.getItem('token');
    // Use variantId if provided, fall back to id
    const vid = product.variantId || product.id;
    if (token) {
      try {
        await firstValueFrom(this.api.addToWishlist(vid));
        await this.syncWithBackend();
      } catch (error) {
        console.error('Error adding to wishlist (DB):', error);
      }
    } else {
      if (!this.wishlist.find(item => item.id === vid)) {
        this.wishlist.push({ ...product, id: vid });
        this.saveWishlistLocal();
      }
    }
  }

  getCart() {
    return this.cart;
  }

  getWishlist() {
    return this.wishlist;
  }

  async removeFromCart(productId: number) {
    const token = localStorage.getItem('token');
    const item = this.cart.find(i => i.id === productId);
    
    if (token && item?.cartItemId) {
      try {
        await firstValueFrom(this.api.removeFromCart(item.cartItemId));
        await this.syncWithBackend();
      } catch (error) {
        console.error('Error removing from cart (DB):', error);
      }
    } else {
      this.cart = this.cart.filter(item => item.id !== productId);
      this.saveCartLocal();
    }
  }

  async removeFromWishlist(productId: number) {
    const token = localStorage.getItem('token');
    const item = this.wishlist.find(i => i.id === productId);

    if (token && item?.wishlistItemId) {
      try {
        await firstValueFrom(this.api.removeFromWishlist(item.wishlistItemId));
        await this.syncWithBackend();
      } catch (error) {
        console.error('Error removing from wishlist (DB):', error);
      }
    } else {
      this.wishlist = this.wishlist.filter(item => item.id !== productId);
      this.saveWishlistLocal();
    }
  }

  async updateQuantity(productId: number, delta: number) {
    const item = this.cart.find(i => i.id === productId);
    if (!item) return;

    const newQty = Math.max(1, (item.quantity ?? 1) + delta);
    const token = localStorage.getItem('token');

    if (token && item.cartItemId) {
      try {
        await firstValueFrom(this.api.updateCartItemQuantity(item.cartItemId, newQty));
        await this.syncWithBackend();
      } catch (error) {
        console.error('Error updating quantity (DB):', error);
      }
    } else {
      item.quantity = newQty;
      this.saveCartLocal();
    }
  }

  get cartCount(): number {
    return this.cart.reduce((sum, item) => sum + (item.quantity ?? 0), 0);
  }

  get subtotal(): number {
    return this.cart.reduce((sum, item) => sum + item.price * (item.quantity ?? 1), 0);
  }
}