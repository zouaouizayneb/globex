import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product, ShopService } from '../../pages/shared/shop.services';
import { ServicesService } from '../../services/services.service';
import { CurrencyService } from '../../services/currency.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-product',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.css']
})
export class ProductComponent implements OnInit, OnDestroy {

  products: Product[] = [];
  fullProducts: any[] = []; // Store full DB product data for quick view
  loading: boolean = true;
  error: string = '';
  debugInfo: string = '';
  showQuickView: boolean = false;
  selectedProduct: any = null;
  currentImageIndex: number = 0;
  quantity: number = 1;
  selectedVariant: any = null;
  selectedColor: string = '';
  selectedSize: string = '';

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

      // Create pairs of (mapped, full) products
      const paired = mappedProducts.map((mapped, index) => ({ mapped, full: dbProducts[index] }));

      // Shuffle the pairs together
      const shuffled = paired.sort(() => Math.random() - 0.5);

      // Take only 8 products
      const selected = shuffled.slice(0, 8);

      // Separate back into mapped and full arrays
      this.products = selected.map(p => p.mapped);
      this.fullProducts = selected.map(p => p.full);

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
        colors: this.getColors(dbProduct.variants, dbProduct.color)
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

  getColors(variants: any[], productColor?: string): string[] {
    console.log('getColors called with productColor:', productColor, 'and variants:', variants);

    const colors: string[] = [];

    // Add product color if it exists
    if (productColor) {
      const hexColor = this.colorNameToHex(productColor);
      console.log('Adding product color:', productColor, '-> hex:', hexColor);
      colors.push(hexColor);
    }

    // Add variant colors
    if (variants && variants.length > 0) {
      const variantColors = [...new Set(
        variants.map(v => v.color).filter(c => !!c)
      )];

      console.log('Unique colors from variants:', variantColors);

      variantColors.forEach(color => {
        const hexColor = this.colorNameToHex(color);
        if (!colors.includes(hexColor)) {
          colors.push(hexColor);
        }
      });
    }

    // If no colors found, return default gray
    const result = colors.length > 0 ? colors : ['#CCCCCC'];

    console.log('Final colors:', result);
    return result;
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

  toggleQuickView(product?: Product): void {
    // If no product passed, close the modal
    if (!product) {
      this.showQuickView = false;
      this.selectedProduct = null;
      return;
    }

    // Find the index of the product in the products array
    const index = this.products.findIndex(p => p.id === product.id);

    if (index === -1 || !this.fullProducts[index]) {
      console.error('Full product not found for:', product);
      return;
    }

    // Use the same index to get the full product data
    this.selectedProduct = this.fullProducts[index];
    this.showQuickView = !this.showQuickView;
    this.currentImageIndex = 0;
    this.quantity = 1;
    this.selectedVariant = null;
    this.selectedColor = '';
    this.selectedSize = '';

    // Set default variant if available
    if (this.selectedProduct.variants && this.selectedProduct.variants.length > 0) {
      this.selectedVariant = this.selectedProduct.variants[0];
      this.selectedColor = this.selectedProduct.variants[0].color || '';
      this.selectedSize = this.selectedProduct.variants[0].size || '';
    }
  }

  selectColor(color: string): void {
    this.selectedColor = color;
    const variant = this.selectedProduct?.variants?.find((v: any) => v.color === color);
    if (variant) {
      this.selectedVariant = variant;
      this.selectedSize = variant.size || '';
    }
    this.currentImageIndex = 0;
  }

  selectSize(size: string): void {
    this.selectedSize = size;
    const variant = this.selectedProduct?.variants?.find((v: any) => v.size === size);
    if (variant) {
      this.selectedVariant = variant;
      this.selectedColor = variant.color || '';
    }
    this.currentImageIndex = 0;
  }

  getAvailableColors(): string[] {
    if (!this.selectedProduct?.variants) return [];
    const colors = this.selectedProduct.variants
      .map((v: any) => v.color)
      .filter((c: any): c is string => typeof c === 'string' && c.length > 0) as string[];
    return [...new Set(colors)];
  }

  getAvailableSizes(): string[] {
    if (!this.selectedProduct?.variants) return [];
    const sizes = this.selectedProduct.variants
      .map((v: any) => v.size)
      .filter((s: any): s is string => typeof s === 'string' && s.length > 0) as string[];
    return [...new Set(sizes)];
  }

  getCurrentPrice(): number {
    if (this.selectedVariant) {
      return parseFloat(this.selectedVariant.totalPrice) || parseFloat(this.selectedVariant.price) || this.selectedProduct.price;
    }
    return this.selectedProduct.price;
  }

  getCurrentImages(): any[] {
    console.log('getCurrentImages - selectedProduct:', this.selectedProduct);
    console.log('getCurrentImages - selectedProduct.images:', this.selectedProduct?.images);
    console.log('getCurrentImages - selectedProduct.image:', this.selectedProduct?.image);

    // Always show main product images first
    // Backend images have imageUrl property, so we need to map them
    const mainImages = this.selectedProduct.images?.map((img: any) => ({ url: img.imageUrl || img.url })) || [{ url: this.selectedProduct.image || this.selectedProduct.imageUrl }];

    console.log('getCurrentImages - mainImages:', mainImages);

    // If variant has a specific image, add it at the end
    if (this.selectedVariant?.imageUrl) {
      const variantImage = { url: this.selectedVariant.imageUrl };
      // Only add if it's not already in the main images
      const alreadyExists = mainImages.some((img: any) => img.url === this.selectedVariant.imageUrl);
      if (!alreadyExists) {
        return [...mainImages, variantImage];
      }
    }

    return mainImages;
  }

  nextImage(): void {
    const images = this.getCurrentImages();
    if (images?.length) {
      this.currentImageIndex = (this.currentImageIndex + 1) % images.length;
    }
  }

  prevImage(): void {
    const images = this.getCurrentImages();
    if (images?.length) {
      this.currentImageIndex =
        (this.currentImageIndex - 1 + images.length) % images.length;
    }
  }

  increaseQuantity(): void { this.quantity++; }
  decreaseQuantity(): void { if (this.quantity > 1) this.quantity--; }

  // Currency conversion methods
  formatPrice(priceInUSD: number): string {
    return this.currencyService.formatPrice(priceInUSD);
  }

  formatOldPrice(oldPriceInUSD: number): string {
    return this.currencyService.formatPrice(oldPriceInUSD);
  }
}


