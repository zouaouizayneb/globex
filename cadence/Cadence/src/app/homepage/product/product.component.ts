import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Product, ShopService } from '../../pages/shared/shop.services';
import { ServicesService } from '../../services/services.service';
import { CurrencyService } from '../../services/currency.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-product',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './product.component.html',
  styleUrl: './product.component.css'
})
export class ProductComponent implements OnInit, OnDestroy {

  products: Product[] = [];
  loading: boolean = true;
  error: string = '';
  debugInfo: string = '';

  private subscriptions: Subscription = new Subscription();

  constructor(
    private shopService: ShopService,
    private servicesService: ServicesService,
    private router: Router,
    private currencyService: CurrencyService
  ) {}

  ngOnInit(): void {
    this.loadProductsFromDatabase();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

 loadProductsFromDatabase(): void {
  this.loading = true;
  this.error = '';

  const sub = this.servicesService.getAllProducts().subscribe({
    next: (dbProducts) => {
      console.log('✅ Products received from DB:', dbProducts);
      
      // Debug: Check the price field
      if (dbProducts && dbProducts.length > 0) {
        console.log('First product price:', dbProducts[0].price, 'type:', typeof dbProducts[0].price);
      }

      // Map DB products
      const mappedProducts = this.mapDatabaseProducts(dbProducts);

      // Shuffle the array randomly
      const shuffled = mappedProducts.sort(() => Math.random() - 0.5);

      // Take only 8 products
      this.products = shuffled.slice(0, 8);

      console.log('✅ Mapped products:', this.products);
      this.loading = false;
    },
    error: (error) => {
      console.error('❌ Error loading products:', error);
      this.error = 'Unable to load products. Please make sure the backend is running.';
      this.loading = false;
    }
  });

  this.subscriptions.add(sub);
}

  mapDatabaseProducts(dbProducts: any[]): Product[] {
    if (!dbProducts || dbProducts.length === 0) {
      return [];
    }
    
    console.log('✅ HOMEPAGE RAW DATA:', dbProducts);

    return dbProducts.map((dbProduct: any, index: number) => {
      const price = this.extractPrice(dbProduct);

      const product: Product = {
        id: dbProduct.idProduct || dbProduct.id_product || dbProduct.id,
        name: dbProduct.name,
        price: price || 0,
        image: this.getPrimaryImage(dbProduct.images),
        rating: Math.floor(dbProduct.rating || 4),
        colors: this.getColors(dbProduct.variants)
      };

      const oldPrice = this.getOldPrice(dbProduct.variants);
      if (oldPrice !== undefined) {
        product.oldPrice = oldPrice;
      }

      const discount = this.calculateDiscount(dbProduct.variants);
      if (discount !== undefined) {
        product.discount = discount;
      }

      if (this.isNewProduct(dbProduct.createdAt || dbProduct.created_at || dbProduct.date_add)) {
        product.isNew = true;
      }

      return product;
    });
  }

  extractPrice(p: any): number {
    // Check all possible field names for price
    let raw = p.price ?? p.productPrice ?? p.product_price ?? p.unitPrice ?? p.unit_price ?? p.Price ?? p.Prix ?? p.prix ?? p.amount ?? p.label;
    
    if (typeof raw === 'string') {
      raw = raw.replace(',', '.'); // Handle European format
    }
    
    let parsed = parseFloat(raw);
    
    // If the label was used, ensure it was purely a number (not something like 'test 1')
    if (raw === p.label && isNaN(Number(p.label?.toString().replace(',', '.')))) {
       parsed = NaN;
    }

    if (!isNaN(parsed) && parsed > 0) return parsed;
    
    const variantPrice = this.getMinPrice(p.variants);
    if (variantPrice > 0) return variantPrice;

    // Log the actual raw object if still 0
    console.warn(`Price check failed for ${p.name || p.title}. Raw data:`, p);
    return 0;
  }

  getPrimaryImage(images: any[]): string {
    if (!images || images.length === 0) {
      return 'assets/images/no-product-image.jpg';
    }

    const primaryImage = images.find(img => img.isPrimary === true);
    if (primaryImage?.imageUrl) {
      return primaryImage.imageUrl;
    }

    if (images[0]?.imageUrl) {
      return images[0].imageUrl;
    }

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

    const v = variants.find(v =>
      v.oldPrice && parseFloat(v.oldPrice) > parseFloat(v.price)
    );

    return v ? parseFloat(v.oldPrice) : undefined;
  }

  getColors(variants: any[]): string[] {
    if (!variants || variants.length === 0) return ['#CCCCCC'];

    const uniqueColors = [...new Set(
      variants.map(v => v.color).filter(c => !!c)
    )];

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

    const v = variants.find(v =>
      v.oldPrice && parseFloat(v.oldPrice) > parseFloat(v.price)
    );

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

  addToCart(product: Product): void {
    console.log('Adding to cart:', product);
    const cartItem = { ...product, quantity: 1 };
    this.shopService.addToCart(cartItem);
    this.shopService.cartOpen$.next(true);
  }

  addToWishlist(product: Product): void {
    this.shopService.addToWishlist(product);
  }

  isInCart(product: Product): boolean {
    return this.shopService.getCart().some(item => item.id === product.id);
  }

  isInWishlist(product: Product): boolean {
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

  retry(): void {
    this.loadProductsFromDatabase();
  }

  // Currency conversion methods
  formatPrice(priceInUSD: number): string {
    return this.currencyService.formatPrice(priceInUSD);
  }

  formatOldPrice(oldPriceInUSD: number): string {
    return this.currencyService.formatPrice(oldPriceInUSD);
  }
}