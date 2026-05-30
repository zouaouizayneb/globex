import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product, ShopService } from '../../pages/shared/shop.services';
import { ServicesService } from '../../services/services.service';
import { CurrencyService } from '../../services/currency.service';
import { Subscription } from 'rxjs';

import { AppCurrencyPipe } from '../../pipes/currency.pipe';

@Component({
  selector: 'app-product',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule, AppCurrencyPipe],
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
        variantId: (dbProduct.variants && dbProduct.variants.length > 0) ? dbProduct.variants[0].idVariant : null,
        name: dbProduct.name,
        price: price || 0,
        image: this.getPrimaryImage(dbProduct.images),
        rating: Math.floor(dbProduct.rating || 4),
        colors: this.getColors(dbProduct.variants, dbProduct.color),
        stock: (dbProduct.variants && dbProduct.variants.length > 0) 
          ? (dbProduct.variants[0].stockQuantity ?? dbProduct.variants[0].stock ?? dbProduct.stock ?? dbProduct.quantity ?? dbProduct.qty ?? dbProduct.inventory ?? 10)
          : (dbProduct.stock ?? dbProduct.quantity ?? dbProduct.qty ?? dbProduct.inventory ?? 10)
      };

      const oldPrice = this.getOldPrice(dbProduct.variants);
      if (oldPrice !== undefined) {
        product.oldPrice = oldPrice;
      }

      const discount = this.calculateDiscount(dbProduct.variants);
      if (discount !== undefined) {
        product.discount = discount;
      }

      // Fix: Only show "New" badge if explicitly marked or if created within the last 7 days
      if (dbProduct.isNew === true || dbProduct.is_new === true) {
        product.isNew = true;
      } else if (this.isNewProduct(dbProduct.createdAt || dbProduct.created_at || dbProduct.date_add)) {
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
      const sevenDaysAgo = new Date();
      sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
      return productDate > sevenDaysAgo;
    } catch {
      return false;
    }
  }

  addToCart(product: any): void {
    console.log('Adding to cart:', product);
    console.log('🔍 QuickView:', this.showQuickView, 'selectedProduct:', this.selectedProduct?.name, 'selectedVariant:', this.selectedVariant);

    // Stock validation
    let availableStock = 0;
    if (this.showQuickView && this.selectedVariant) {
      availableStock = this.selectedVariant.stockQuantity ?? this.selectedVariant.stock ?? 0;
    } else if (product.stock) {
      availableStock = product.stock;
    } else if (product.variants && product.variants.length > 0) {
      availableStock = product.variants[0].stockQuantity ?? product.variants[0].stock ?? 0;
    }

    if (availableStock <= 0) {
      alert('This product is out of stock and cannot be added to cart.');
      return;
    }

    if (this.quantity > availableStock) {
      alert(`Only ${availableStock} item(s) available in stock.`);
      return;
    }

    // If NOT in QuickView and product has variants, open QuickView first
    if (!this.showQuickView && product.variants && product.variants.length > 0) {
      console.log('🔍 Product has variants, opening QuickView for selection');
      this.toggleQuickView(product);
      return;
    }

    // Determine the variant ID to add
    let targetVariantId = product.variantId;
    let targetVariant: any = null;

    // If we're in Quick View and a variant is selected, use that
    if (this.showQuickView && this.selectedProduct && this.selectedVariant) {
      targetVariantId = this.selectedVariant.idVariant || this.selectedVariant.id;
      targetVariant = this.selectedVariant;
      console.log('✅ Using variant from QuickView. targetVariantId:', targetVariantId);
    }
    // If adding from grid, find the variant in fullProducts
    else if (product.variantId) {
      const index = this.products.findIndex(p => p.id === product.id);
      if (index !== -1 && this.fullProducts[index]) {
        const fullProduct = this.fullProducts[index];
        targetVariant = fullProduct.variants?.find((v: any) =>
          (v.idVariant || v.id) === product.variantId
        );
      }
    }
    // Fallback if not set but variants available
    else if (!targetVariantId && product.variants && product.variants.length > 0) {
      targetVariantId = product.variants[0].idVariant || product.variants[0].id;
      targetVariant = product.variants[0];
    }

    const productId = product.idProduct || product.id_product || product.id;
    let name = product.name || product.title || 'Unknown Product';
    let color = '';
    let size = '';

    // Add variant details to name for cart display
    if (targetVariant) {
      color = targetVariant.color || '';
      size = targetVariant.size || '';
      const variantDetails = [];
      if (color && color !== 'undefined') {
        variantDetails.push(`Color: ${color}`);
      }
      if (size && size !== 'undefined') {
        variantDetails.push(`Size: ${size}`);
      }
      if (variantDetails.length > 0) {
        name = `${name} (${variantDetails.join(', ')})`;
      }
    } else if (this.showQuickView) {
      // Fallback to selected color/size if variant not found
      if (this.selectedColor && this.selectedColor !== 'undefined') {
        color = this.selectedColor;
      }
      if (this.selectedSize && this.selectedSize !== 'undefined') {
        size = this.selectedSize;
      }
      const variantDetails = [];
      if (color) {
        variantDetails.push(`Color: ${color}`);
      }
      if (size) {
        variantDetails.push(`Size: ${size}`);
      }
      if (variantDetails.length > 0) {
        name = `${name} (${variantDetails.join(', ')})`;
      }
    }

    // Determine price
    let price = product.price;
    if (targetVariant) {
      price = parseFloat(targetVariant.totalPrice) || parseFloat(targetVariant.price) || product.price;
    } else if (this.showQuickView && this.selectedProduct) {
      price = this.getCurrentPrice();
    }

    // Apply discount if any
    const discount = this.selectedProduct?.discount || this.selectedProduct?.remise || product.discount || product.remise || 0;
    if (discount > 0) {
      price = price - (price * discount / 100);
    }

    // Determine image - prioritize variant image when variant selected
    let image = 'assets/images/no-product-image.jpg';
    if (targetVariant?.imageUrl) {
      image = targetVariant.imageUrl;
    } else if (this.showQuickView && this.selectedVariant?.imageUrl) {
      image = this.selectedVariant.imageUrl;
    } else if (product.image) {
      image = product.image;
    } else if (product.images && product.images.length > 0) {
      image = product.images[0].imageUrl || product.images[0].url;
    }

    const cartItem: Product = {
      id: targetVariantId || productId, // Use variantId as primary key in cart
      variantId: targetVariantId, // Explicitly set variantId for backend
      name: name,
      price: price,
      image: image,
      variantImage: image, // Store variant-specific image
      variantColor: color, // Store variant color
      variantSize: size, // Store variant size
      quantity: this.quantity || 1,
      rating: product.rating || 4,
      colors: product.colors || []
    };

    this.shopService.addToCart(cartItem);
    this.shopService.cartOpen$.next(true);
  }

  addToWishlist(product: any): void {
    const productId = product.idProduct || product.id_product || product.id;
    const name = product.name || product.title || 'Unknown Product';

    // Determine variant ID
    let targetVariantId = product.variantId;
    let targetVariant: any = null;

    // If we're in Quick View and a variant is selected, use that
    if (this.showQuickView && this.selectedProduct && this.selectedVariant) {
      targetVariantId = this.selectedVariant.idVariant || this.selectedVariant.id;
      targetVariant = this.selectedVariant;
    }
    // If adding from grid, find the variant in fullProducts
    else if (product.variantId) {
      const index = this.products.findIndex(p => p.id === product.id);
      if (index !== -1 && this.fullProducts[index]) {
        const fullProduct = this.fullProducts[index];
        targetVariant = fullProduct.variants?.find((v: any) =>
          (v.idVariant || v.id) === product.variantId
        );
      }
    }
    // Fallback if not set but variants available
    else if (!targetVariantId && product.variants && product.variants.length > 0) {
      targetVariantId = product.variants[0].idVariant || product.variants[0].id;
      targetVariant = product.variants[0];
    }

    let color = '';
    let size = '';

    // Add variant details
    if (targetVariant) {
      color = targetVariant.color || '';
      size = targetVariant.size || '';
    }

    // Determine image - prioritize variant image when variant selected
    let image = product.image;
    if (targetVariant?.imageUrl) {
      image = targetVariant.imageUrl;
    } else if (this.showQuickView && this.selectedVariant?.imageUrl) {
      image = this.selectedVariant.imageUrl;
    } else if (!image && product.images && product.images.length > 0) {
      image = product.images[0].imageUrl || product.images[0].url;
    } else if (!image) {
      image = 'assets/images/no-product-image.jpg';
    }

    const wishItem: Product = {
      id: targetVariantId || productId,
      variantId: targetVariantId,
      name: name,
      price: product.price,
      image: image,
      variantImage: image,
      variantColor: color,
      variantSize: size,
      rating: product.rating || 4,
      colors: product.colors || []
    };

    this.shopService.addToWishlist(wishItem);
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
    // Try to find variant with matching color AND current selected size
    let variant = this.selectedProduct?.variants?.find((v: any) => 
      v.color === color && (this.selectedSize ? v.size === this.selectedSize : true)
    );
    // If not found, just find by color
    if (!variant) {
      variant = this.selectedProduct?.variants?.find((v: any) => v.color === color);
    }
    console.log('✅ selectColor called:', color, 'Found variant:', variant);
    if (variant) {
      this.selectedVariant = variant;
      this.selectedSize = variant.size || '';
      console.log('✅ selectedVariant set to:', this.selectedVariant);
      // Set image index to 0 to show the variant's image (which is first in getCurrentImages)
      this.currentImageIndex = 0;
    } else {
      this.currentImageIndex = 0;
    }
  }

  selectSize(size: string): void {
    this.selectedSize = size;
    // Try to find variant with matching size AND current selected color
    let variant = this.selectedProduct?.variants?.find((v: any) => 
      v.size === size && (this.selectedColor ? v.color === this.selectedColor : true)
    );
    // If not found, just find by size
    if (!variant) {
      variant = this.selectedProduct?.variants?.find((v: any) => v.size === size);
    }
    if (variant) {
      this.selectedVariant = variant;
      this.selectedColor = variant.color || '';
    }
    this.currentImageIndex = 0;
  }

  getAvailableColors(): string[] {
    const colors: string[] = [];
    
    // Add base product color if it exists
    if (this.selectedProduct?.color) {
      colors.push(this.selectedProduct.color);
    }
    
    // Add variant colors
    if (this.selectedProduct?.variants) {
      this.selectedProduct.variants.forEach((v: any) => {
        if (v.color && !colors.includes(v.color)) {
          colors.push(v.color);
        }
      });
    }
    
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
    console.log('getCurrentImages - selectedVariant:', this.selectedVariant);

    // If a variant is selected, show its image FIRST
    if (this.selectedVariant?.imageUrl) {
      const variantImage = { url: this.selectedVariant.imageUrl };
      // Get main product images
      const mainImages = this.selectedProduct.images?.map((img: any) => ({ url: img.imageUrl || img.url })) || [{ url: this.selectedProduct.image || this.selectedProduct.imageUrl }];
      
      // Return variant image first, then all main images
      return [variantImage, ...mainImages];
    }

    // If no variant selected, show main product images
    const mainImages = this.selectedProduct.images?.map((img: any) => ({ url: img.imageUrl || img.url })) || [{ url: this.selectedProduct.image || this.selectedProduct.imageUrl }];
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

  increaseQuantity(): void {
    if (this.quantity < 99) {
      this.quantity++;
    }
  }

  decreaseQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  onQuantityChange(): void {
    const newQuantity = Number(this.quantity);
    if (isNaN(newQuantity) || newQuantity < 1) {
      this.quantity = 1;
    } else if (newQuantity > 99) {
      this.quantity = 99;
    } else {
      this.quantity = newQuantity;
    }
  }

  // Currency conversion methods
  formatPrice(priceInUSD: number): string {
    return this.currencyService.formatPrice(priceInUSD);
  }

  formatOldPrice(oldPriceInUSD: number): string {
    return this.currencyService.formatPrice(oldPriceInUSD);
  }

  isOutOfStock(product: any): boolean {
    let availableStock = 0;
    if (this.showQuickView && this.selectedVariant) {
      availableStock = this.selectedVariant.stockQuantity ?? this.selectedVariant.stock ?? 0;
    } else if (product.stock) {
      availableStock = product.stock;
    } else if (product.variants && product.variants.length > 0) {
      availableStock = product.variants[0].stockQuantity ?? product.variants[0].stock ?? 0;
    }
    return availableStock <= 0;
  }

  getAvailableStock(product: any): number {
    if (this.showQuickView && this.selectedVariant) {
      return this.selectedVariant.stockQuantity ?? this.selectedVariant.stock ?? 0;
    } else if (product.stock) {
      return product.stock;
    } else if (product.variants && product.variants.length > 0) {
      return product.variants[0].stockQuantity ?? product.variants[0].stock ?? 0;
    }
    return 0;
  }
}


