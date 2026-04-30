import { Component, ViewChild, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ServicesService } from '../services/services.service';
import { ShopService } from '../pages/shared/shop.services';
import { CartComponent } from '../pages/cart/cart.component';
import { CurrencyService } from '../services/currency.service';

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [RouterModule, CommonModule, CartComponent, FormsModule],
  templateUrl: './nav-bar.component.html',
  styleUrl: './nav-bar.component.css'
})
export class NavBarComponent implements OnInit {
  @ViewChild('cartDrawer') cartDrawer!: CartComponent;

  isHomeVisible: boolean = false;
  categories: any[] = [];
  searchOpen = false;
  searchQuery = '';
  searchResults: any[] = [];
  isSearching = false;

  allProducts: any[] = [];
  allCategories: any[] = [];

  categorizedSearchResults = {
    products: [] as any[],
    categories: [] as any[]
  };

  private homeCloseTimer: any;
  private shopAllCloseTimer: any;
  private productsCloseTimer: any;
  private pagesCloseTimer: any;
  private blogsCloseTimer: any;

  currencyDropdownOpen: boolean = false;
  languageDropdownOpen: boolean = false;
  selectedCurrency: string = 'USD';
  selectedLanguage: string = 'English';
  
  currencies = [
    { code: 'USD', symbol: '$', flagCode: 'us', rate: 1 },
    { code: 'AUD', symbol: '$', flagCode: 'au', rate: 1.53 },
    { code: 'CAD', symbol: '$', flagCode: 'ca', rate: 1.36 },
    { code: 'GBP', symbol: '£', flagCode: 'gb', rate: 0.79 },
    { code: 'EUR', symbol: '€', flagCode: 'eu', rate: 0.92 },
    { code: 'TND', symbol: 'د.ت', flagCode: 'tn', rate: 3.12 }
  ];
  
  languages = [
    { name: 'English', flagCode: 'gb' },
    { name: 'العربية', flagCode: 'tn' },
    { name: 'Deutsch', flagCode: 'de' },
    { name: 'Español', flagCode: 'es' },
    { name: 'Français', flagCode: 'fr' }
  ];

  constructor(
    public services: ServicesService, 
    public shop: ShopService, 
    private currencyService: CurrencyService,
    private router: Router
  ) {
    this.getCategories();
  }

  ngOnInit() {
    this.currencyService.getSelectedCurrency().subscribe(currency => {
      this.selectedCurrency = currency;
    });

    const savedLanguage = localStorage.getItem('selectedLanguage');
    if (savedLanguage) {
      this.selectedLanguage = savedLanguage;
    }

    this.shop.cartOpen$.subscribe(isOpen => {
      if (isOpen && this.cartDrawer) {
        this.openCart();
        this.shop.cartOpen$.next(false);
      }
    });

    this.loadSearchData();
  }

  loadSearchData() {
    this.services.getAllProducts().subscribe(products => {
      this.allProducts = products;
    });
    this.services.getAllCategories().subscribe(categories => {
      this.allCategories = categories;
    });
  }

  get isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  openCart() {
    this.cartDrawer.open();
  }

  get cartCount(): number {
    return this.shop.cartCount;
  }

  async getCategories() {
    this.categories = [
        { id: 4, label: 'Agriculture', image: 'https://images.unsplash.com/photo-1471193945509-9ad0617afabf?w=800&h=600&fit=crop' },
        { id: 3, label: 'Home & garden', image: 'https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?w=800&h=600&fit=crop' },
        { id: 2, label: 'Clothing', image: 'https://images.unsplash.com/photo-1445205170230-053b83016050' },
        { id: 1, label: 'Electronics', image: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&h=600&fit=crop' }
    ];
  }

  showHome() {
    if (this.homeCloseTimer) {
      clearTimeout(this.homeCloseTimer);
      this.homeCloseTimer = undefined;
    }
    this.isHomeVisible = true;
  }

  hideHome() {
    if (this.homeCloseTimer) {
      clearTimeout(this.homeCloseTimer);
    }
    this.homeCloseTimer = setTimeout(() => {
      this.isHomeVisible = false;
    }, 400);
  }

  keepHomeOpen(event: MouseEvent) {
    event.stopPropagation();
    this.showHome();
  }

  closeHome(event: MouseEvent) {
    event.stopPropagation();
    this.hideHome();
  }

  isShopAllVisible: boolean = false;
  showShopAll() {
    if (this.shopAllCloseTimer) {
      clearTimeout(this.shopAllCloseTimer);
      this.shopAllCloseTimer = undefined;
    }
    this.isShopAllVisible = true;
  }

  hideShopAll() {
    if (this.shopAllCloseTimer) {
      clearTimeout(this.shopAllCloseTimer);
    }
    this.shopAllCloseTimer = setTimeout(() => {
      this.isShopAllVisible = false;
    }, 400);
  }

  keepShopAllOpen(event: MouseEvent) {
    event.stopPropagation();
    this.showShopAll();
  }

  closeShopAll(event: MouseEvent) {
    event.stopPropagation();
    this.hideShopAll();
  }

  isProductsVisible: boolean = false;
  isPagesVisible: boolean = false;
  isBlogsVisible: boolean = false;

  showProducts() {
    if (this.productsCloseTimer) {
      clearTimeout(this.productsCloseTimer);
      this.productsCloseTimer = undefined;
    }
    this.isProductsVisible = true;
  }
  hideProducts() {
    if (this.productsCloseTimer) {
      clearTimeout(this.productsCloseTimer);
    }
    this.productsCloseTimer = setTimeout(() => {
      this.isProductsVisible = false;
    }, 400);
  }
  keepProductsOpen(event: MouseEvent) {
    event.stopPropagation();
    this.showProducts();
  }
  closeProducts(event: MouseEvent) {
    event.stopPropagation();
    this.hideProducts();
  }

  showPages() {
    if (this.pagesCloseTimer) {
      clearTimeout(this.pagesCloseTimer);
      this.pagesCloseTimer = undefined;
    }
    this.isPagesVisible = true;
  }
  hidePages() {
    if (this.pagesCloseTimer) {
      clearTimeout(this.pagesCloseTimer);
    }
    this.pagesCloseTimer = setTimeout(() => {
      this.isPagesVisible = false;
    }, 400);
  }
  keepPagesOpen(event: MouseEvent) {
    event.stopPropagation();
    this.showPages();
  }
  closePages(event: MouseEvent) {
    event.stopPropagation();
    this.hidePages();
  }

  showBlogs() {
    if (this.blogsCloseTimer) {
      clearTimeout(this.blogsCloseTimer);
      this.blogsCloseTimer = undefined;
    }
    this.isBlogsVisible = true;
  }
  hideBlogs() {
    if (this.blogsCloseTimer) {
      clearTimeout(this.blogsCloseTimer);
    }
    this.blogsCloseTimer = setTimeout(() => {
      this.isBlogsVisible = false;
    }, 400);
  }
  keepBlogsOpen(event: MouseEvent) {
    event.stopPropagation();
    this.showBlogs();
  }
  closeBlogs(event: MouseEvent) {
    event.stopPropagation();
    this.hideBlogs();
  }

  toggleCurrencyDropdown() {
    this.currencyDropdownOpen = !this.currencyDropdownOpen;
    this.languageDropdownOpen = false;
  }

  toggleLanguageDropdown() {
    this.languageDropdownOpen = !this.languageDropdownOpen;
    this.currencyDropdownOpen = false;
  }

  selectCurrency(currency: any) {
    this.currencyService.setCurrency(currency.code);
    this.currencyDropdownOpen = false;
  }

  selectLanguage(language: any) {
    this.selectedLanguage = language.name;
    this.languageDropdownOpen = false;
    localStorage.setItem('selectedLanguage', language.name);
  }

  updateCurrencyDisplay(currency: any) {

    console.log(`Currency changed to ${currency.code} with rate ${currency.rate}`);
  }

  getCurrentCurrencyRate(): number {
    const storedRate = localStorage.getItem('currencyRate');
    return storedRate ? parseFloat(storedRate) : 1;
  }

  getCurrentCurrencySymbol(): string {
    const currency = this.currencies.find(c => c.code === this.selectedCurrency);
    return currency ? currency.symbol : '$';
  }

  getCurrentCurrencyFlagCode(): string {
    const currency = this.currencies.find(c => c.code === this.selectedCurrency);
    return currency?.flagCode || this.currencies[0]?.flagCode || 'us';
  }

  getCurrentLanguageFlagCode(): string {
    const language = this.languages.find(l => l.name === this.selectedLanguage);
    return language?.flagCode || this.languages[0]?.flagCode || 'gb';
  }

  toggleSearch() {
    this.searchOpen = !this.searchOpen;
    if (!this.searchOpen) {
      this.searchQuery = '';
      this.searchResults = [];
      this.categorizedSearchResults.products = [];
      this.categorizedSearchResults.categories = [];
    }
  }

  onSearch() {
    if (!this.searchQuery.trim()) {
      this.categorizedSearchResults.products = [];
      this.categorizedSearchResults.categories = [];
      this.searchResults = [];
      return;
    }

    this.isSearching = true;

    const q = this.searchQuery.toLowerCase();
    
    this.categorizedSearchResults.products = this.allProducts.filter(p => {
      const nameMatch = p.name && p.name.toLowerCase().includes(q);
      const skuMatch = p.sku && p.sku.toLowerCase().includes(q);
      const descMatch = p.description && p.description.toLowerCase().includes(q);
      return nameMatch || skuMatch || descMatch;
    }).slice(0, 5);

    this.categorizedSearchResults.categories = this.allCategories.filter(c => {
      const nameMatch = c.name && c.name.toLowerCase().includes(q);
      const labelMatch = c.label && c.label.toLowerCase().includes(q);
      return nameMatch || labelMatch;
    }).slice(0, 3);

    this.searchResults = this.categorizedSearchResults.products;
    
    this.isSearching = false;
  }

  onSearchKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.performSearch();
    }
  }

  performSearch() {
    if (this.searchQuery.trim()) {
      if (this.categorizedSearchResults.products.length > 0) {
        const product = this.categorizedSearchResults.products[0];
        const categoryId = product.category?.idCategory || product.category?.id || product.category_id || product.category;
        if (categoryId) {
          this.router.navigate(['/list-article', categoryId]);
          this.toggleSearch();
        } else {
          this.navigateToProduct(product);
        }
      }
      else if (this.categorizedSearchResults.categories.length > 0) {
        this.navigateToCategory(this.categorizedSearchResults.categories[0]);
      }
      else {
        this.router.navigate(['/collections'], {
          queryParams: { search: this.searchQuery }
        });
        this.toggleSearch();
      }
    }
  }

  navigateToProduct(product: any) {
    this.router.navigate(['/product', product.id]);
    this.toggleSearch();
  }

  navigateToCategory(category: any) {
    const categoryId = category.id || category.idCategory;
    this.router.navigate(['/list-article', categoryId]);
    this.toggleSearch();
  }

  goToSearch(path: string): void {
    this.router.navigate([path]);
    this.searchQuery = '';
    this.categorizedSearchResults.products = [];
    this.categorizedSearchResults.categories = [];
  }
}
