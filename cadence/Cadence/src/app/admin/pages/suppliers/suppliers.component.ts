import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, AdminSupplier } from '../../services/admin.service';

@Component({
  selector: 'app-suppliers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './suppliers.component.html',
  styleUrls: ['./suppliers.component.css']
})
export class SuppliersComponent implements OnInit {
  suppliers: AdminSupplier[] = [];
  isLoading = true;
  error: string | null = null;

  searchTerm: string = '';
  filterStatus: string = '';

  showModal = false;
  editingSupplier: AdminSupplier | null = null;
  formName = '';
  formEmail = '';
  formPhone = '';
  formAddress = '';
  formStatus: 'active' | 'inactive' = 'active';
  isSaving = false;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadSuppliers();
  }

  loadSuppliers(): void {
    this.isLoading = true;
    this.error = null;
    this.adminService.getSuppliers().subscribe({
      next: (suppliers) => {
        this.suppliers = suppliers;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Could not load suppliers. Make sure the backend is running.';
        this.isLoading = false;
      }
    });
  }

  get filteredSuppliers(): AdminSupplier[] {
    return this.suppliers.filter(s => {
      const matchesSearch = s.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           s.email.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = !this.filterStatus || s.status === this.filterStatus;
      return matchesSearch && matchesStatus;
    });
  }

  addSupplier(): void {
    this.editingSupplier = null;
    this.formName = '';
    this.formEmail = '';
    this.formPhone = '';
    this.formAddress = '';
    this.formStatus = 'active';
    this.showModal = true;
  }

  editSupplier(id: number): void {
    const s = this.suppliers.find(x => x.id === id);
    if (!s) return;
    this.editingSupplier = s;
    this.formName = s.name;
    this.formEmail = s.email;
    this.formPhone = s.phone;
    this.formAddress = s.address;
    this.formStatus = s.status;
    this.showModal = true;
  }

  saveSupplier(): void {
    if (!this.formName.trim() || !this.formEmail.trim()) return;
    this.isSaving = true;

    const payload = {
      name: this.formName.trim(),
      email: this.formEmail.trim(),
      phone: this.formPhone.trim(),
      address: this.formAddress.trim(),
      status: this.formStatus
    };

    const request$ = this.editingSupplier
      ? this.adminService.updateSupplier(this.editingSupplier.id, payload)
      : this.adminService.createSupplier(payload);

    request$.subscribe({
      next: () => {
        this.showModal = false;
        this.isSaving = false;
        this.loadSuppliers();
      },
      error: () => {
        alert('Failed to save supplier.');
        this.isSaving = false;
      }
    });
  }

  cancelModal(): void {
    this.showModal = false;
  }

  deleteSupplier(id: number): void {
    if (!confirm('Delete this supplier?')) return;
    this.adminService.deleteSupplier(id).subscribe({
      next: () => this.suppliers = this.suppliers.filter(s => s.id !== id),
      error: () => alert('Failed to delete supplier.')
    });
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n.charAt(0)).join('').toUpperCase().slice(0, 2);
  }

  getAvatarColor(id: number): string {
    const colors = ['#4a8b6f', '#6fa89e', '#3f7861', '#5a9d7f'];
    return colors[id % colors.length];
  }
}
