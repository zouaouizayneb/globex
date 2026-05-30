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
  formCategoryId: number | '' = '';
  formSupplierId: number | '' = '';
  formPrice: number = 0;
  formStatus: 'active' | 'inactive' | 'draft' = 'active';
  formVariants: { sku: string; additionalPrice: number; color: string; size: string; stockQuantity: number; imageUrls: string[] }[] = [];
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
    this.formCategoryId = '';
    this.formSupplierId = '';
    this.formPrice = 0;
    this.formStatus = 'active';
    this.formVariants = [{ sku: `PROD-${Date.now()}`, additionalPrice: 0, color: '', size: '', stockQuantity: 0, imageUrls: [''] }];
    this.showModal = true;
  }

  editProduct(id: number): void {
    const p = this.products.find(x => x.id === id);
    if (!p) return;
    this.editingProduct = p;
    this.formName = p.name;
    this.formDescription = p.description || '';
    this.formCategoryId = p.categoryId || '';
    this.formSupplierId = p.supplierId || '';
    this.formPrice = p.price || 0;
    this.formStatus = p.status || 'active';
    this.formVariants = [{ sku: p.sku || '', additionalPrice: 0, color: '', size: '', stockQuantity: p.stock || 0, imageUrls: p.imageUrl ? [p.imageUrl] : [''] }];
    this.showModal = true;
  }

  addVariant(): void {
    this.formVariants.push({ sku: `PROD-${Date.now()}-${this.formVariants.length}`, additionalPrice: 0, color: '', size: '', stockQuantity: 0, imageUrls: [''] });
  }

  removeVariant(index: number): void {
    if (this.formVariants.length > 1) {
      this.formVariants.splice(index, 1);
    }
  }

  addImageUrl(variantIndex: number): void {
    this.formVariants[variantIndex].imageUrls.push('');
  }

  removeImageUrl(variantIndex: number, imageIndex: number): void {
    if (this.formVariants[variantIndex].imageUrls.length > 0) {
      this.formVariants[variantIndex].imageUrls.splice(imageIndex, 1);
    }
  }

  saveProduct(): void {
    if (!this.formName.trim() || this.formVariants.length === 0) return;
    if (this.formPrice <= 0) {
      alert('Please enter a base price greater than 0.');
      return;
    }
    this.isSaving = true;

    const payload = {
      name: this.formName.trim(),
      description: this.formDescription.trim(),
      price: this.formPrice,
      category: this.formCategoryId ? { idCategory: this.formCategoryId } : null,
      supplier: this.formSupplierId ? { idSupplier: this.formSupplierId } : null,
      status: this.formStatus.toUpperCase(),
      variants: this.formVariants.map(v => ({
        sku: v.sku,
        additionalPrice: v.additionalPrice,
        color: v.color,
        size: v.size,
        stockQuantity: v.stockQuantity,
        images: v.imageUrls.filter(url => url.trim()).map((url, index) => ({ imageUrl: url.trim(), isPrimary: index === 0 }))
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
      next: () => {
        this.products = this.products.filter(p => p.id !== id);
        this.adminService.triggerRefresh();
      },
      error: (err) => {
        console.error('Failed to delete product:', err);
        console.error('Error status:', err.status);
        console.error('Error body:', err.error);
        let errorMessage = 'Failed to delete product.';
        if (err.status === 403) {
          errorMessage = 'Access denied. You need ADMIN privileges to delete products.';
        } else if (err.status === 401) {
          errorMessage = 'Authentication required. Please log in again.';
        } else if (err.status === 404) {
          errorMessage = 'Product not found.';
        } else {
          errorMessage = err.error?.message || err.error?.error || err.message || errorMessage;
        }
        alert(errorMessage);
      }
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
