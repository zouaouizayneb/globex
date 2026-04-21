import { Component, OnInit } from '@angular/core';
import { ShopService } from '../../pages/shared/shop.services';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router'; 


@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule , RouterModule],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css'
})
export class WishlistComponent implements OnInit {

  wishlist: any[] = [];

  constructor(private shopService: ShopService) {}

  ngOnInit(): void {
    this.loadWishlist();
  }

  loadWishlist() {
    this.wishlist = this.shopService.getWishlist();
  }

  removeFromWishlist(itemId: number) {
    this.shopService.removeFromWishlist(itemId);
    this.loadWishlist();
  }

  addToCart(productId: number) {
    const product = this.wishlist.find(item => item.id === productId);
    if (product) {
      this.shopService.addToCart(product);
    }
  }

  getImage(path: string) {
    return path || 'assets/images/no-product-image.jpg';
  }
}
