import { Component, OnInit } from '@angular/core';
import { ServicesService } from '../../services/services.service';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ShopService } from '../../pages/shared/shop.services';
import { CurrencyService } from '../../services/currency.service';

@Component({
  selector: 'app-list-article',
  imports: [CommonModule, FormsModule, RouterModule],
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
    const colors = this.getColors(p.variants);

    return {
      id: p.idProduct || p.id_product || p.id,
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
      stock: p.stock ?? p.availabilities ?? 1,
      isNew: this.isNewProduct(p.createdAt || p.created_at || p.date_add),
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

  getColors(variants: any[]): string[] {
    if (!variants || variants.length === 0) return ['#CCCCCC'];
    const unique = [...new Set(variants.map(v => v.color).filter(c => !!c))];
    return unique.length > 0 ? unique : ['#CCCCCC'];
  }

  isNewProduct(createdAt: string | Date): boolean {
    if (!createdAt) return false;
    try {
      const d = new Date(createdAt);
      const thirtyDaysAgo = new Date();
      thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
      return d > thirtyDaysAgo;
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
    const cartItem = { ...product, quantity: 1, image: product.images[0]?.url };
    this.shopService.addToCart(cartItem);
    this.shopService.cartOpen$.next(true); // Open the drawer
  }

  addToWishlist(product: any): void {
    const wishItem = { ...product, image: product.images[0]?.url || product.image };
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
  }

  nextImage(): void {
    if (this.selectedProduct?.images?.length) {
      this.currentImageIndex = (this.currentImageIndex + 1) % this.selectedProduct.images.length;
    }
  }

  prevImage(): void {
    if (this.selectedProduct?.images?.length) {
      this.currentImageIndex =
        (this.currentImageIndex - 1 + this.selectedProduct.images.length) % this.selectedProduct.images.length;
    }
  }

  increaseQuantity(): void { this.quantity++; }
  decreaseQuantity(): void { if (this.quantity > 1) this.quantity--; }

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


