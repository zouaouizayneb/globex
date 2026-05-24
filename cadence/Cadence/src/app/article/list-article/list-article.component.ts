import { Component, OnInit } from '@angular/core';
import { ServicesService } from '../../services/services.service';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Product, ShopService } from '../../pages/shared/shop.services';
import { CurrencyService } from '../../services/currency.service';

import { AppCurrencyPipe } from '../../pipes/currency.pipe';

@Component({
  selector: 'app-list-article',
  imports: [CommonModule, FormsModule, RouterModule, AppCurrencyPipe],
  templateUrl: './list-article.component.html',
  styleUrl: './list-article.component.css',
  standalone: true
})
export class ListArticleComponent implements OnInit {

  products: any[] = [];
  productsFiltred: any[] = [];
  category!: any;
  minPrice: any = null;
  maxPrice: any = null;
  categoryLabel: string = '';
  inStockCount: number = 0;
  outOfStockCount: number = 0;
  inStockSelected: boolean = false;
  outOfStockSelected: boolean = false;
  brands: string[] = [];
  selectedBrands: { [brand: string]: boolean } = {};
  colors: string[] = [];
  selectedColors: { [color: string]: boolean } = {};
  cartVisible: boolean = false;
  showQuickView: boolean = false;
  selectedProduct: any = null;
  currentImageIndex: number = 0;
  quantity: number = 1;
  loading: boolean = true;
  selectedVariant: any = null;
  selectedColor: string = '';
  selectedSize: string = '';

  currentCurrencyRate: number = 1;
  currentCurrencySymbol: string = '$';

  constructor(
    public services: ServicesService,
    public route: ActivatedRoute,
    private router: Router,
    private shopService: ShopService,
    private currencyService: CurrencyService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.category = params['category'];
      console.log('✅ ListArticleComponent category:', this.category);
      this.setCategoryLabel();
      this.getArticlesByCategory();
    });

    // Subscribe to currency changes
    this.currencyService.getSelectedCurrencyRate().subscribe(rate => {
      this.currentCurrencyRate = rate;
    });
    this.currencyService.getSelectedCurrencySymbol().subscribe(symbol => {
      this.currentCurrencySymbol = symbol;
    });
  }

  setCategoryLabel(): void {
    if (this.category === 'all') {
      this.categoryLabel = 'All Products';
      return;
    }
    this.services.getCategoryById(Number(this.category)).subscribe({
      next: (cat: any) => {
        this.categoryLabel = cat.name || cat.label || 'Collections';
      },
      error: () => {
        this.categoryLabel = 'Collections';
      }
    });
  }

  getArticlesByCategory(): void {
    this.loading = true;

    const categoryId = this.category === 'all' ? 'all' : Number(this.category);
    console.log('Fetching articles for category:', categoryId);

    const request$ = categoryId === 'all' || isNaN(categoryId as number)
      ? this.services.getAllProducts()
      : this.services.getProductsByCategory(categoryId);

    request$.subscribe({
      next: (data: any[]) => {
        console.log('✅ RAW DATA FROM BACKEND:', data);
        if (data.length > 0) {
          console.log('DEBUG: First item raw:', JSON.stringify(data[0], null, 2));
          console.table(data.slice(0, 10));
        }
        this.products = data.map((p, index) => {
          return this.mapProduct(p);
        });
        console.log('✅ Mapped products:', this.products);
        this.productsFiltred = [...this.products];

        this.brands = [...new Set(
          this.products.map(p => p.brand).filter(b => b)
        )] as string[];

        this.colors = [...new Set(
          this.products.flatMap(p => p.colors)
        )] as string[];

        this.selectedBrands = {};
        this.brands.forEach(b => this.selectedBrands[b] = false);

        this.selectedColors = {};
        this.colors.forEach(c => this.selectedColors[c] = false);

        this.updateProducts(this.products);
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Error loading products:', err);
        this.loading = false;
      }
    });
  }

  mapProduct(p: any): any {
    const images = (p.images || []).map((img: any) => ({
      url: img.imageUrl || img.image_url || img.url || img.src || 'assets/images/no-product-image.jpg'
    }));

    if (images.length === 0) {
      images.push({ url: 'assets/images/no-product-image.jpg' });
    }

    const price = this.extractPrice(p);
    const oldPrice = this.getOldPrice(p.variants);
    const remise = oldPrice ? Math.round(((oldPrice - price) / oldPrice) * 100) : 0;
    const colors = this.getColors(p.variants, p.color);

    return {
      id: p.idProduct || p.id_product || p.id,
      variantId: (p.variants && p.variants.length > 0) ? p.variants[0].idVariant : null,
      title: p.name || p.title || 'Unknown Product',
      description: p.description || '',
      price: price,
      oldPrice: oldPrice,
      remise: remise,
      rating: Math.floor(p.rating || p.review || 4),
      images: images,
      colors: colors,
      color: colors[0] || '',
      brand: p.brand || p.brandName || (p.variants?.[0]?.brand) || '',
      stock: (p.variants && p.variants.length > 0)
        ? (p.variants[0].stockQuantity ?? p.variants[0].stock ?? p.stock ?? p.availabilities ?? 10)
        : (p.stock ?? p.availabilities ?? 10),
      isNew: (p.isNew === true || p.is_new === true) || this.isNewProduct(p.createdAt || p.created_at || p.date_add),
      variants: p.variants || []
    };
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

  getMinPrice(variants: any[]): number {
    if (!variants || variants.length === 0) return 0;
    const prices = variants.map(v => parseFloat(v.totalPrice) || parseFloat(v.price)).filter(p => !isNaN(p) && p > 0);
    return prices.length > 0 ? Math.min(...prices) : 0;
  }

  getOldPrice(variants: any[]): number | undefined {
    if (!variants || variants.length === 0) return undefined;
    const v = variants.find(v => v.oldPrice && parseFloat(v.oldPrice) > (parseFloat(v.totalPrice) || parseFloat(v.price)));
    return v ? parseFloat(v.oldPrice) : undefined;
  }

  getColors(variants: any[], productColor?: string): string[] {
    const colors: string[] = [];
    
    // Add product color if it exists
    if (productColor) {
      const hexColor = this.colorNameToHex(productColor);
      colors.push(hexColor);
    }
    
    // Add variant colors
    if (variants && variants.length > 0) {
      variants.forEach(v => {
        if (v.color && !colors.includes(v.color)) {
          const hexColor = this.colorNameToHex(v.color);
          if (!colors.includes(hexColor)) {
            colors.push(hexColor);
          }
        }
      });
    }
    
    return colors.length > 0 ? colors : ['#CCCCCC'];
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

  isNewProduct(createdAt: string | Date): boolean {
    if (!createdAt) return false;
    try {
      const d = new Date(createdAt);
      const sevenDaysAgo = new Date();
      sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
      return d > sevenDaysAgo;
    } catch { return false; }
  }

  updateProducts(newProducts: any[]): void {
    this.products = newProducts;
    this.productsFiltred = newProducts;
    this.calculateStockCounts();
  }

  calculateStockCounts(): void {
    this.inStockCount = this.productsFiltred.filter(p => p.stock > 0).length;
    this.outOfStockCount = this.productsFiltred.filter(p => p.stock === 0).length;
  }

  addToCart(product: any): void {
    console.log('Adding to cart (list):', product);
    console.log('🔍 QuickView:', this.showQuickView, 'selectedProduct:', this.selectedProduct?.name, 'selectedVariant:', this.selectedVariant);

    // Stock validation is handled by the backend

    // Determine the variant ID to add
    let targetVariantId = product.variantId;
    let targetVariant: any = null;
    
    // If we're in Quick View and a variant is selected, use that
    if (this.showQuickView && this.selectedProduct && this.selectedVariant) {
      targetVariantId = this.selectedVariant.idVariant || this.selectedVariant.id;
      targetVariant = this.selectedVariant;
      console.log('✅ Using variant from QuickView. targetVariantId:', targetVariantId);
    } 
    // Fallback if not set but variants available
    else if (!targetVariantId && product.variants && product.variants.length > 0) {
      targetVariantId = product.variants[0].idVariant || product.variants[0].id;
      targetVariant = product.variants[0];
    }
    // If targetVariantId is set but targetVariant is not, find the variant
    else if (targetVariantId && product.variants && product.variants.length > 0) {
      targetVariant = product.variants.find((v: any) => 
        v.idVariant === targetVariantId || v.id === targetVariantId
      );
    }

    const productId = product.idProduct || product.id_product || product.id;
    let name = product.name || product.title || 'Unknown Product';
    
    // Add variant details to name for cart display
    if (this.showQuickView && this.selectedVariant) {
      const variantDetails = [];
      if (this.selectedColor && this.selectedColor !== 'undefined') {
        variantDetails.push(`Color: ${this.selectedColor}`);
      }
      if (this.selectedSize && this.selectedSize !== 'undefined') {
        variantDetails.push(`Size: ${this.selectedSize}`);
      }
      if (variantDetails.length > 0) {
        name = `${name} (${variantDetails.join(', ')})`;
      }
    } else if (targetVariant) {
      // Add variant details from the target variant
      const variantDetails = [];
      if (targetVariant.color) {
        variantDetails.push(`Color: ${targetVariant.color}`);
      }
      if (targetVariant.size) {
        variantDetails.push(`Size: ${targetVariant.size}`);
      }
      if (variantDetails.length > 0) {
        name = `${name} (${variantDetails.join(', ')})`;
      }
    }
    
    // Determine price
    let price = product.price;
    if (this.showQuickView && this.selectedProduct) {
      price = this.getCurrentPrice();
      // Apply discount if any
      const discount = this.selectedProduct.discount || this.selectedProduct.remise || 0;
      if (discount > 0) {
        price = price - (price * discount / 100);
      }
    } else if (targetVariant) {
      price = parseFloat(targetVariant.totalPrice) || parseFloat(targetVariant.price) || product.price;
    } else if (product.discount || product.remise) {
      const discount = product.discount || product.remise || 0;
      price = price - (price * discount / 100);
    }

    // Determine image - prioritize variant image when variant selected
    let image = 'assets/images/no-product-image.jpg';
    if (this.showQuickView && this.selectedVariant?.imageUrl) {
      image = this.selectedVariant.imageUrl;
    } else if (targetVariant?.imageUrl) {
      image = targetVariant.imageUrl;
    } else if (product.image) {
      image = product.image;
    } else if (product.images && product.images.length > 0) {
      image = product.images[0].url || product.images[0].imageUrl;
    }

    // Determine variant color and size
    let variantColor = this.selectedColor || '';
    let variantSize = this.selectedSize || '';
    if (!variantColor && targetVariant?.color) {
      variantColor = targetVariant.color;
    }
    if (!variantSize && targetVariant?.size) {
      variantSize = targetVariant.size;
    }

    const cartItem: Product = {
      id: targetVariantId || productId, // Use variantId as the primary key in cart
      variantId: targetVariantId, // Explicitly set variantId for backend
      name: name,
      price: price,
      image: image,
      variantImage: image, // Store variant-specific image
      variantColor: variantColor, // Store variant color
      variantSize: variantSize, // Store variant size
      quantity: this.quantity || 1,
      rating: product.rating || 4,
      colors: product.colors || []
    };
    
    this.shopService.addToCart(cartItem);
    this.shopService.cartOpen$.next(true); // Open the drawer
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
    // Fallback if not set but variants available
    else if (!targetVariantId && product.variants && product.variants.length > 0) {
      targetVariantId = product.variants[0].idVariant || product.variants[0].id;
      targetVariant = product.variants[0];
    }
    // If targetVariantId is set but targetVariant is not, find the variant
    else if (targetVariantId && product.variants && product.variants.length > 0) {
      targetVariant = product.variants.find((v: any) => 
        v.idVariant === targetVariantId || v.id === targetVariantId
      );
    }

    let color = '';
    let size = '';

    // Add variant details
    if (targetVariant) {
      color = targetVariant.color || '';
      size = targetVariant.size || '';
    } else if (this.showQuickView) {
      color = this.selectedColor || '';
      size = this.selectedSize || '';
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
      image = product.images[0].url || product.images[0].imageUrl;
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

  filter(): void {
    if (this.minPrice !== null && this.maxPrice !== null && this.maxPrice < this.minPrice) {
      alert('Max price must be greater than min price');
      return;
    }

    const min = this.minPrice !== null ? this.minPrice : 0;
    const max = this.maxPrice !== null ? this.maxPrice : Infinity;

    const activeBrands = Object.entries(this.selectedBrands)
      .filter(([_, v]) => v).map(([b]) => b);

    const activeColors = Object.entries(this.selectedColors)
      .filter(([_, v]) => v).map(([c]) => c);

    this.productsFiltred = this.products.filter(product => {
      const finalPrice = product.price - (product.price * product.remise / 100);
      const priceMatch = finalPrice >= min && finalPrice <= max;
      const stockMatch = (this.inStockSelected && product.stock > 0) ||
                         (this.outOfStockSelected && product.stock === 0) ||
                         (!this.inStockSelected && !this.outOfStockSelected);
      const brandMatch = activeBrands.length === 0 || activeBrands.includes(product.brand);
      const colorMatch = activeColors.length === 0 ||
                         product.colors.some((c: string) => activeColors.includes(c));
      return priceMatch && stockMatch && brandMatch && colorMatch;
    });

    this.calculateStockCounts();
  }

  resetColors(): void {
    Object.keys(this.selectedColors).forEach(c => this.selectedColors[c] = false);
    this.filter();
  }

  resetAvailability(): void {
    this.inStockSelected = false;
    this.outOfStockSelected = false;
    this.filter();
  }

  resetPrice(): void {
    this.minPrice = null;
    this.maxPrice = null;
    this.filter();
  }

  resetBrands(): void {
    Object.keys(this.selectedBrands).forEach(b => this.selectedBrands[b] = false);
    this.filter();
  }

  get selectedColorsCount(): number {
    return Object.values(this.selectedColors).filter(v => v).length;
  }

  get selectedBrandsCount(): number {
    return Object.values(this.selectedBrands).filter(v => v).length;
  }

  get selectedAvailabilityCount(): number {
    return (this.inStockSelected ? 1 : 0) + (this.outOfStockSelected ? 1 : 0);
  }

  getBrandProductCount(brand: string): number {
    return this.products.filter(p => p.brand === brand).length;
  }

  toggleCartVisibility(): void {
    this.cartVisible = !this.cartVisible;
  }

  toggleQuickView(product: any): void {
    this.selectedProduct = product;
    this.showQuickView = !this.showQuickView;
    this.currentImageIndex = 0;
    this.quantity = 1;
    this.selectedVariant = null;
    this.selectedColor = '';
    this.selectedSize = '';

    // Set default variant if available
    if (product.variants && product.variants.length > 0) {
      this.selectedVariant = product.variants[0];
      this.selectedColor = product.variants[0].color || '';
      this.selectedSize = product.variants[0].size || '';
    }
  }

  selectVariant(variant: any): void {
    this.selectedVariant = variant;
    this.selectedColor = variant.color || '';
    this.selectedSize = variant.size || '';
    this.currentImageIndex = 0;
  }

  selectColor(color: string): void {
    this.selectedColor = color;
    // Find a variant with this color
    const variant = this.selectedProduct?.variants?.find((v: any) => v.color === color);
    console.log('✅ selectColor called:', color, 'Found variant:', variant);
    if (variant) {
      this.selectedVariant = variant;
      this.selectedSize = variant.size || '';
      console.log('✅ selectedVariant set to:', this.selectedVariant);
    }
    this.currentImageIndex = 0;
  }

  selectSize(size: string): void {
    this.selectedSize = size;
    // Find a variant with this size
    const variant = this.selectedProduct?.variants?.find((v: any) => v.size === size);
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
    if (this.selectedVariant?.imageUrl) {
      return [{ url: this.selectedVariant.imageUrl }, ...this.selectedProduct.images];
    }
    return this.selectedProduct.images;
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

  onImageError(event: any): void {
    event.target.src = 'assets/images/no-product-image.jpg';
  }

  getStars(rating: number): string {
    return '★'.repeat(Math.floor(rating)) + '☆'.repeat(5 - Math.floor(rating));
  }

  convertPrice(price: number): number {
    return Math.round(price * this.currentCurrencyRate * 100) / 100;
  }

  formatPrice(price: number): string {
    const convertedPrice = this.convertPrice(price);
    return `${this.currentCurrencySymbol}${convertedPrice.toFixed(2)}`;
  }
}


