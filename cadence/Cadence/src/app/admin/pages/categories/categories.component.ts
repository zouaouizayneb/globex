import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, AdminCategory, AdminProduct } from '../../services/admin.service';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.css']
})
export class CategoriesComponent implements OnInit {
  categories: AdminCategory[] = [];
  isLoading = true;
  error: string | null = null;

  searchTerm: string = '';
  filterStatus: string = '';

  // Edit / Create modal state
  showModal = false;
  editingCategory: AdminCategory | null = null;
  formName = '';
  formDescription = '';
  isSaving = false;

  // Product display state
  selectedCategory: AdminCategory | null = null;
  categoryProducts: AdminProduct[] = [];
  isLoadingProducts = false;
  showProductsView = false;

  // Product edit modal state
  showProductModal = false;
  editingProduct: AdminProduct | null = null;
  productForm = {
    name: '',
    description: '',
    sku: '',
    price: 0,
    stock: 0,
    categoryId: 0,
    status: 'active' as 'active' | 'inactive' | 'draft'
  };
  isSavingProduct = false;
  errorLoadingProducts: string | null = null;
  debugInfo: string = '';

  // Product Preview state
  isProductPreviewOpen = false;
  selectedProductForPreview: AdminProduct | null = null;

  constructor(
    private adminService: AdminService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.isLoading = true;
    this.error = null;
    this.adminService.getCategories().subscribe({
      next: (cats) => {
        this.categories = cats;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Could not load categories. Make sure the backend is running.';
        this.isLoading = false;
      }
    });
  }

  get filteredCategories(): AdminCategory[] {
    return this.categories.filter(c => {
      const matchesSearch = c.name.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = !this.filterStatus || c.status === this.filterStatus;
      return matchesSearch && matchesStatus;
    });
  }

  addCategory(): void {
    this.editingCategory = null;
    this.formName = '';
    this.formDescription = '';
    this.showModal = true;
  }

  editCategory(category: AdminCategory): void {
    this.editingCategory = category;
    this.formName = category.name;
    this.formDescription = category.description;
    this.showModal = true;
  }

  saveCategory(): void {
    if (!this.formName.trim()) return;
    this.isSaving = true;
    const payload = { name: this.formName.trim(), description: this.formDescription.trim() };

    const request$ = this.editingCategory
      ? this.adminService.updateCategory(this.editingCategory.id, payload)
      : this.adminService.createCategory(payload);

    request$.subscribe({
      next: () => {
        this.showModal = false;
        this.isSaving = false;
        this.loadCategories();
      },
      error: () => {
        alert('Failed to save category.');
        this.isSaving = false;
      }
    });
  }

  cancelModal(): void {
    this.showModal = false;
  }

  deleteCategory(id: number): void {
    if (!confirm('Delete this category?')) return;
    this.adminService.deleteCategory(id).subscribe({
      next: () => this.categories = this.categories.filter(c => c.id !== id),
      error: () => alert('Failed to delete category.')
    });
  }

  // ═══════════════════════════════════════════════════════════
  // PRODUCT MANAGEMENT IN CATEGORY
  // ═══════════════════════════════════════════════════════════

  viewCategoryProducts(category: AdminCategory): void {
    console.log('--- DEBUG: viewCategoryProducts called ---');
    console.log('Category Object:', JSON.stringify(category));
    
    if (!category || (category.id === undefined && (category as any).idCategory === undefined)) {
      console.error('Invalid Category ID!', category);
      this.errorLoadingProducts = 'Invalid Category ID detected.';
      return;
    }

    const categoryId = category.id || (category as any).idCategory;
    console.log('Target Category ID:', categoryId);

    this.selectedCategory = category;
    this.isLoadingProducts = true;
    this.errorLoadingProducts = null;
    this.showProductsView = true;
    this.categoryProducts = [];
    this.debugInfo = `Loading ID: ${categoryId}...`;
    
    window.scrollTo({ top: 0, behavior: 'smooth' });

    this.adminService.getProductsByCategory(categoryId).subscribe({
      next: (products) => {
        console.log('--- DEBUG: getProductsByCategory SUCCESS ---');
        console.log('Received Products:', products.length);
        this.categoryProducts = products;
        this.isLoadingProducts = false;
        this.debugInfo = `Loaded ${products.length} products for ID ${categoryId}`;
        this.cdr.detectChanges(); // Force UI update
      },
      error: (err) => {
        console.error('--- DEBUG: getProductsByCategory ERROR ---', err);
        this.errorLoadingProducts = 'Could not load products for this category.';
        this.isLoadingProducts = false;
        this.debugInfo = `Error loading ID ${categoryId}`;
        this.cdr.detectChanges();
      }
    });
  }

  backToCategories(): void {
    this.showProductsView = false;
    this.selectedCategory = null;
    this.categoryProducts = [];
  }

  addProductToCategory(): void {
    if (!this.selectedCategory) return;
    this.editingProduct = null;
    this.productForm = {
      name: '',
      description: '',
      sku: '',
      price: 0,
      stock: 0,
      categoryId: this.selectedCategory.id,
      status: 'active'
    };
    this.showProductModal = true;
  }

  editProduct(product: AdminProduct): void {
    this.editingProduct = product;
    this.productForm = {
      name: product.name,
      description: product.description || '',
      sku: product.sku,
      price: product.price,
      stock: product.stock,
      categoryId: this.selectedCategory?.id || product.categoryId || 0,
      status: product.status
    };
    this.showProductModal = true;
  }

  saveProduct(): void {
    if (!this.productForm.name.trim() || !this.productForm.sku.trim()) {
      alert('Name and SKU are required');
      return;
    }

    this.isSavingProduct = true;
    const payload = {
      name: this.productForm.name.trim(),
      description: this.productForm.description.trim(),
      sku: this.productForm.sku.trim(),
      price: this.productForm.price,
      stock: this.productForm.stock,
      categoryId: this.productForm.categoryId,
      status: this.productForm.status
    };

    const request$ = this.editingProduct
      ? this.adminService.updateProduct(this.editingProduct.id, payload)
      : this.adminService.createProduct(payload);

    request$.subscribe({
      next: () => {
        this.showProductModal = false;
        this.isSavingProduct = false;
        if (this.selectedCategory) {
          this.viewCategoryProducts(this.selectedCategory);
        }
      },
      error: () => {
        alert('Failed to save product');
        this.isSavingProduct = false;
      }
    });
  }

  deleteProduct(productId: number): void {
    if (!confirm('Delete this product?')) return;
    this.adminService.deleteProduct(productId).subscribe({
      next: () => {
        this.categoryProducts = this.categoryProducts.filter(p => p.id !== productId);
      },
      error: () => alert('Failed to delete product')
    });
  }

  cancelProductModal(): void {
    this.showProductModal = false;
  }

  // PREVIEW LOGIC
  openProductPreview(product: AdminProduct): void {
    this.selectedProductForPreview = product;
    this.isProductPreviewOpen = true;
    document.body.style.overflow = 'hidden';
  }

  closeProductPreview(): void {
    this.isProductPreviewOpen = false;
    this.selectedProductForPreview = null;
    document.body.style.overflow = 'auto';
  }
}
