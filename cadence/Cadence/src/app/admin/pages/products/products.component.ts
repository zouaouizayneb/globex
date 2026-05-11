import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, AdminProduct, AdminSupplier } from '../../services/admin.service';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.css']
})
export class ProductsComponent implements OnInit {
  products: AdminProduct[] = [];
  suppliers: AdminSupplier[] = [];
  isLoading = true;
  error: string | null = null;

  searchTerm: string = '';
  filterStatus: string = '';
  filterCategory: string = '';

  categories: string[] = [];
  rawCategories: any[] = [];
  statuses: string[] = ['active', 'inactive', 'draft'];

  showModal = false;
  editingProduct: AdminProduct | null = null;
  formName = '';
  formDescription = '';
  formImageUrls: string[] = [];
  formCategoryId: number | '' = '';
  formSupplierId: number | '' = '';
  formPrice: number = 0;
  formColor: string = '';
  formSize: string = '';
  formStock: number = 0;
  formStatus: 'active' | 'inactive' | 'draft' = 'active';
  formVariants: { sku: string; additionalPrice: number; color: string; size: string; stockQuantity: number; imageUrl: string }[] = [];
  isSaving = false;

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const userRole = this.authService.getUserRole();
    console.log('Current user role:', userRole);
    console.log('Is admin?', this.authService.isAdmin());

    if (!this.authService.isAdmin()) {
      console.error('Access denied: User does not have ADMIN role');
      alert('Access denied: You need ADMIN privileges to access this page. Current role: ' + (userRole || 'None'));
      this.router.navigate(['/login']);
      return;
    }
    this.loadProducts();
    this.loadCategories();
    this.loadSuppliers();
  }

  loadCategories(): void {
    this.adminService.getCategories().subscribe(cats => {
      this.rawCategories = cats;
    });
  }

  loadSuppliers(): void {
    this.adminService.getSuppliers().subscribe({
      next: (suppliers) => {
        this.suppliers = suppliers.filter(s => s.status === 'active');
      },
      error: () => {
        console.error('Failed to load suppliers');
      }
    });
  }

  loadProducts(): void {
    this.isLoading = true;
    this.error = null;
    this.adminService.getProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.categories = [...new Set(products.map(p => p.category).filter(Boolean))];
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Could not load products. Make sure the backend is running.';
        this.isLoading = false;
      }
    });
  }

  get filteredProducts(): AdminProduct[] {
    return this.products.filter(p => {
      const matchesSearch = p.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           p.sku.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = !this.filterStatus || p.status === this.filterStatus;
      const matchesCategory = !this.filterCategory || p.category === this.filterCategory;
      return matchesSearch && matchesStatus && matchesCategory;
    });
  }

  addProduct(): void {
    this.editingProduct = null;
    this.formName = '';
    this.formDescription = '';
    this.formImageUrls = [''];
    this.formCategoryId = '';
    this.formSupplierId = '';
    this.formPrice = 0;
    this.formColor = '';
    this.formSize = '';
    this.formStock = 0;
    this.formStatus = 'active';
    this.formVariants = [{ sku: `PROD-${Date.now()}`, additionalPrice: 0, color: '', size: '', stockQuantity: 0, imageUrl: '' }];
    this.showModal = true;
  }

  editProduct(id: number): void {
    const p = this.products.find(x => x.id === id);
    if (!p) return;
    this.editingProduct = p;
    this.formName = p.name;
    this.formDescription = p.description || '';
    this.formImageUrls = p.imageUrl ? [p.imageUrl] : [];
    this.formCategoryId = p.categoryId || '';
    this.formSupplierId = p.supplierId || '';
    this.formPrice = p.price || 0;
    this.formColor = p.color || '';
    this.formSize = p.size || '';
    this.formStock = p.stock || 0;
    this.formStatus = p.status || 'active';
    this.formVariants = [{ sku: p.sku || '', additionalPrice: 0, color: '', size: '', stockQuantity: p.stock, imageUrl: p.imageUrl || '' }];
    this.showModal = true;
  }

  addVariant(): void {
    this.formVariants.push({ sku: `PROD-${Date.now()}-${this.formVariants.length}`, additionalPrice: 0, color: '', size: '', stockQuantity: 0, imageUrl: '' });
  }

  removeVariant(index: number): void {
    if (this.formVariants.length > 1) {
      this.formVariants.splice(index, 1);
    }
  }

  addImageUrl(): void {
    this.formImageUrls.push('');
  }

  removeImageUrl(index: number): void {
    if (this.formImageUrls.length > 0) {
      this.formImageUrls.splice(index, 1);
    }
  }

  saveProduct(): void {
    if (!this.formName.trim() || this.formVariants.length === 0) return;
    this.isSaving = true;

    const images = this.formImageUrls
      .filter(url => url.trim())
      .map((url, index) => ({ imageUrl: url.trim(), isPrimary: index === 0 }));

    const payload = {
      name: this.formName.trim(),
      description: this.formDescription.trim(),
      price: this.formPrice,
      color: this.formColor.trim(),
      size: this.formSize.trim(),
      stock: this.formStock,
      category: this.formCategoryId ? { idCategory: this.formCategoryId } : null,
      images: images,
      variants: this.formVariants.map(v => ({
        sku: v.sku,
        additionalPrice: v.additionalPrice,
        color: v.color,
        size: v.size,
        stockQuantity: v.stockQuantity,
        imageUrl: v.imageUrl
      }))
    };

    const request$ = this.editingProduct
      ? this.adminService.updateProduct(this.editingProduct.id, payload)
      : this.adminService.createProduct(payload);

    request$.subscribe({
      next: () => {
        this.showModal = false;
        this.isSaving = false;
        this.loadProducts();
        this.adminService.triggerRefresh();
      },
      error: (err) => {
        console.error('Failed to save product:', err);
        console.error('Error status:', err.status);
        console.error('Error body:', err.error);
        console.error('Full error:', JSON.stringify(err));
        let errorMessage = 'Failed to save product.';
        if (err.status === 403) {
          errorMessage = 'Access denied. You need ADMIN privileges to save products. Please log in with an admin account.';
        } else if (err.status === 401) {
          errorMessage = 'Authentication required. Please log in again.';
        } else {
          errorMessage = err.error?.message || err.error?.error || err.message || errorMessage;
        }
        alert(errorMessage);
        this.isSaving = false;
      }
    });
  }

  cancelModal(): void {
    this.showModal = false;
  }

  deleteProduct(id: number): void {
    if (!confirm('Delete this product?')) return;
    this.adminService.deleteProduct(id).subscribe({
      next: () => this.products = this.products.filter(p => p.id !== id),
      error: () => alert('Failed to delete product.')
    });
  }

  getStatusColor(status: string): string {
    switch(status) {
      case 'active': return '#10b981';
      case 'inactive': return '#ef4444';
      case 'draft': return '#f59e0b';
      default: return '#6b7280';
    }
  }

  getStockStatus(stock: number): string {
    if (stock === 0) return 'Out of Stock';
    if (stock < 20) return 'Low Stock';
    return 'In Stock';
  }

  getStockColor(stock: number): string {
    if (stock === 0) return '#ef4444';
    if (stock < 20) return '#f59e0b';
    return '#10b981';
  }
}
