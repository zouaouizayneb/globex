import { Component, OnInit } from '@angular/core';
import { ShopService } from '../../pages/shared/shop.services';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router'; 


import { AppCurrencyPipe } from '../../pipes/currency.pipe';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule, RouterModule, AppCurrencyPipe],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css'
})
export class WishlistComponent implements OnInit {

  wishlist: any[] = []; // will be replaced by subscription

  constructor(private shopService: ShopService) {}

  ngOnInit(): void {
    this.shopService.wishlist$.subscribe(items => this.wishlist = items);
  }

  // loadWishlist method removed; using observable subscription

  removeFromWishlist(itemId: number) {
    this.shopService.removeFromWishlist(itemId);
    // no need to reload manually; observable updates
  }

  addToCart(productId: number) {
    const product = this.wishlist.find(item => item.id === productId);
    if (product) {
      // Stock validation is handled by the backend
      // Add using variantId if available, otherwise fallback to id
      const variantId = product.variantId ?? product.id;
      this.shopService.addToCart({ ...product, variantId: variantId });
      // Optionally remove from wishlist after adding to cart
      this.shopService.removeFromWishlist(product.id);
    }
  }

  getImage(path: string) {
    return path || 'assets/images/no-product-image.jpg';
  }
}
