import { Component, OnInit, OnDestroy, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { Product, ShopService } from '../../pages/shared/shop.services';
import { ServicesService } from '../../services/services.service';
import { CurrencyService } from '../../services/currency.service';
import { Subscription } from 'rxjs';

interface BestSellerProduct extends Product {
  description?: string;
  category: string;
  badge: 'trend' | 'hot' | 'sale';
  soldCount: number;
  soldPercentage: number;
  reviews: number;
  isHero?: boolean;
}

@Component({
  selector: 'app-best-selling',
  standalone: true,
  imports: [RouterModule, CommonModule, TitleCasePipe],
  templateUrl: './best-selling.component.html',
  styleUrl: './best-selling.component.css'
})
export class BestSellingComponent implements OnInit, OnDestroy, AfterViewInit {

  products: BestSellerProduct[] = [];
  loading: boolean = true;
  error: string = '';

  activeFilter: string = 'all';
  toastVisible: boolean = false;
  toastMessage: string = '';

  private subscriptions: Subscription = new Subscription();
  private soldCountIntervals: any[] = [];

  @ViewChild('productGrid') productGrid!: ElementRef;

  constructor(
    private shopService: ShopService,
    private servicesService: ServicesService,
    private router: Router,
    private currencyService: CurrencyService
  ) {}

  ngOnInit(): void {
    this.loadBestSellingProducts();
  }

  ngAfterViewInit(): void {
    // Initial animation runs after products load
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    this.soldCountIntervals.forEach(interval => clearInterval(interval));
  }

  loadBestSellingProducts(): void {
    this.loading = true;
    this.error = '';

    const sub = this.servicesService.getAllProducts().subscribe({
      next: (dbProducts) => {
        if (!dbProducts || dbProducts.length === 0) {
          this.error = 'No products available at the moment.';
          this.loading = false;
          return;
        }

        // Sort by rating as fallback to determine "best sellers"
        const sorted = [...dbProducts].sort((a, b) => (b.rating || 0) - (a.rating || 0));
        const top = sorted.slice(0, 8);

        this.products = this.mapDatabaseProducts(top);

        if (this.products.length > 0) {
          this.products[0].isHero = true;
        }

        this.loading = false;
        setTimeout(() => {
          this.animateEntrance();
          this.animateSoldCounts();
        }, 100);
      },
      error: (err) => {
        console.error('Error loading products:', err);
        this.error = 'Unable to load products. Please make sure the backend is running.';
        this.loading = false;
      }
    });

    this.subscriptions.add(sub);
  }

  /** Fallback mapper for plain products without sold data */
  mapDatabaseProducts(dbProducts: any[]): BestSellerProduct[] {
    if (!dbProducts || dbProducts.length === 0) return [];

    const badgeTypes: ('trend' | 'hot' | 'sale')[] = ['trend', 'hot', 'sale'];
    const maxRating = Math.max(...dbProducts.map(p => p.rating || 0), 1);

    return dbProducts.map((dbProduct: any, index: number) => {
      const price = this.extractPrice(dbProduct);
      const rating = dbProduct.rating || 4;
      const soldCount = Math.round(rating * 25);
      const soldPercentage = Math.round((rating / maxRating) * 100);

      const product: BestSellerProduct = {
        id: dbProduct.idProduct,
        name: dbProduct.name,
        description: dbProduct.description,
        price: price || 0,
        image: this.getPrimaryImage(dbProduct.images, dbProduct.imageUrl),
        rating: Math.floor(rating),
        colors: this.getColors(dbProduct.variants),
        category: dbProduct.category?.name || 'Collection',
        badge: badgeTypes[index % badgeTypes.length],
        soldCount,
        soldPercentage,
        reviews: Math.floor(rating * 18)
      };

      const oldPrice = this.getOldPrice(dbProduct.variants);
      if (oldPrice !== undefined) product.oldPrice = oldPrice;

      const discount = this.calculateDiscount(dbProduct.variants);
      if (discount !== undefined) product.discount = discount;

      if (this.isNewProduct(dbProduct.createdAt)) product.isNew = true;

      return product;
    });
  }

  extractPrice(p: any): number {
    if (p.price !== undefined && p.price !== null) {
      if (typeof p.price === 'number') return p.price;
      if (typeof p.price === 'object' && p.price.toNumber) return p.price.toNumber();
      if (typeof p.price === 'string') return parseFloat(p.price.replace(',', '.'));
    }
    const variantPrice = this.getMinPrice(p.variants);
    if (variantPrice > 0) return variantPrice;
    return 0;
  }

  getPrimaryImage(images: any[], fallbackUrl?: string): string {
    if (images && images.length > 0) {
      const primary = images.find(img => img.isPrimary === true || img.is_primary === true);
      if (primary?.imageUrl) return primary.imageUrl;
      if (images[0]?.imageUrl) return images[0].imageUrl;
    }
    if (fallbackUrl) return fallbackUrl;
    return 'assets/images/no-product-image.jpg';
  }

  getMinPrice(variants: any[]): number {
    if (!variants || variants.length === 0) return 0;
    const prices = variants
      .map(v => parseFloat(v.totalPrice) || parseFloat(v.price))
      .filter(p => !isNaN(p) && p > 0);
    return prices.length > 0 ? Math.min(...prices) : 0;
  }

  getOldPrice(variants: any[]): number | undefined {
    if (!variants || variants.length === 0) return undefined;
    const v = variants.find(v => v.oldPrice && parseFloat(v.oldPrice) > parseFloat(v.price));
    return v ? parseFloat(v.oldPrice) : undefined;
  }

  getColors(variants: any[]): string[] {
    if (!variants || variants.length === 0) return ['#CCCCCC'];
    const uniqueColors = [...new Set(variants.map(v => v.color).filter(c => !!c))];
    return uniqueColors.length > 0
      ? uniqueColors.map(color => this.colorNameToHex(color))
      : ['#CCCCCC'];
  }

  colorNameToHex(colorName: string): string {
    const colorMap: { [key: string]: string } = {
      'Black': '#000000', 'White': '#FFFFFF', 'Red': '#FF0000',
      'Blue': '#0000FF', 'Navy': '#000080', 'Green': '#00FF00',
      'Yellow': '#FFFF00', 'Orange': '#FFA500', 'Pink': '#FFC0CB',
      'Purple': '#800080', 'Gray': '#808080', 'Grey': '#808080',
      'Brown': '#8B4513', 'Beige': '#F5F5DC', 'Cream': '#FFFDD0',
      'Gold': '#FFD700', 'Silver': '#C0C0C0', 'Maroon': '#800000',
      'Olive': '#808000', 'Lime': '#00FF00', 'Aqua': '#00FFFF',
      'Teal': '#008080', 'Fuchsia': '#FF00FF', 'Khaki': '#C3B091'
    };
    return colorMap[colorName]
      || colorMap[colorName.charAt(0).toUpperCase() + colorName.slice(1).toLowerCase()]
      || colorName
      || '#CCCCCC';
  }

  calculateDiscount(variants: any[]): number | undefined {
    if (!variants || variants.length === 0) return undefined;
    const v = variants.find(v => v.oldPrice && parseFloat(v.oldPrice) > parseFloat(v.price));
    if (v) {
      const oldPrice = parseFloat(v.oldPrice);
      const currentPrice = parseFloat(v.price);
      return Math.round(((oldPrice - currentPrice) / oldPrice) * 100);
    }
    return undefined;
  }

  isNewProduct(createdAt: string | Date): boolean {
    if (!createdAt) return false;
    try {
      const productDate = new Date(createdAt);
      const thirtyDaysAgo = new Date();
      thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
      return productDate > thirtyDaysAgo;
    } catch {
      return false;
    }
  }

  addToCart(product: BestSellerProduct): void {
    const cartItem = { ...product, quantity: 1 };
    this.shopService.addToCart(cartItem);
    this.shopService.cartOpen$.next(true);
    this.showToast(`"${product.name}" added to cart!`);
  }

  addToWishlist(product: BestSellerProduct): void {
    this.shopService.addToWishlist(product);
  }

  isInCart(product: BestSellerProduct): boolean {
    return this.shopService.getCart().some(item => item.id === product.id);
  }

  isInWishlist(product: BestSellerProduct): boolean {
    return this.shopService.getWishlist().some(item => item.id === product.id);
  }

  getStars(rating: number): string {
    const fullStars = Math.floor(rating);
    const emptyStars = 5 - fullStars;
    return '★'.repeat(fullStars) + '☆'.repeat(emptyStars);
  }

  onImageError(event: any): void {
    event.target.src = 'assets/images/no-product-image.jpg';
  }

  formatPrice(priceInUSD: number): string {
    return this.currencyService.formatPrice(priceInUSD);
  }

  formatOldPrice(oldPriceInUSD: number): string {
    return this.currencyService.formatPrice(oldPriceInUSD);
  }

  setFilter(filter: string): void {
    this.activeFilter = filter;
  }

  getFilteredProducts(): BestSellerProduct[] {
    if (this.activeFilter === 'all') return this.products;
    return this.products.filter(p => p.badge === this.activeFilter);
  }

  isProductVisible(product: BestSellerProduct): boolean {
    return this.activeFilter === 'all' || product.badge === this.activeFilter;
  }

  showToast(message: string): void {
    this.toastMessage = message;
    this.toastVisible = true;
    setTimeout(() => { this.toastVisible = false; }, 2800);
  }

  animateEntrance(): void {
    const cards = document.querySelectorAll('.product-card');
    cards.forEach((card, index) => {
      (card as HTMLElement).style.setProperty('--stagger', `${index * 0.1}s`);
      setTimeout(() => {
        card.classList.add('is-visible');
      }, 50 + index * 80);
    });
  }

  animateSoldCounts(): void {
    this.products.forEach((product) => {
      const interval = setInterval(() => {
        // Gently tick up sold counts every 10–15s for engagement
        if (Math.random() < 0.12) {
          product.soldCount += 1;
        }
      }, 10000 + Math.random() * 5000);
      this.soldCountIntervals.push(interval);
    });
  }

  viewAllProducts(): void {
    this.router.navigate(['/list-article', 'all']);
  }

  viewProduct(product: BestSellerProduct): void {
    this.router.navigate(['/article', product.id]);
  }
}