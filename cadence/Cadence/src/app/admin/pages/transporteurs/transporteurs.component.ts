import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, AdminTransporter } from '../../services/admin.service';

@Component({
  selector: 'app-transporteurs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transporteurs.component.html',
  styleUrls: ['./transporteurs.component.css']
})
export class TransporteursComponent implements OnInit {
  transporters: AdminTransporter[] = [];
  isLoading = true;
  error: string | null = null;

  searchTerm: string = '';
  filterStatus: string = '';

  showModal = false;
  editingTransporter: AdminTransporter | null = null;
  formName = '';
  formEmail = '';
  formPhone = '';
  formAddress = '';
  formDeliveryFee: number = 0;
  formStatus: 'active' | 'inactive' = 'active';
  isSaving = false;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadTransporters();
  }

  loadTransporters(): void {
    this.isLoading = true;
    this.error = null;
    this.adminService.getTransporters().subscribe({
      next: (transporters) => {
        this.transporters = transporters;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Could not load transporters. Make sure the backend is running.';
        this.isLoading = false;
      }
    });
  }

  get filteredTransporters(): AdminTransporter[] {
    return this.transporters.filter(t => {
      const matchesSearch = t.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           t.email.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = !this.filterStatus || t.status === this.filterStatus;
      return matchesSearch && matchesStatus;
    });
  }

  addTransporter(): void {
    this.editingTransporter = null;
    this.formName = '';
    this.formEmail = '';
    this.formPhone = '';
    this.formAddress = '';
    this.formDeliveryFee = 0;
    this.formStatus = 'active';
    this.showModal = true;
  }

  editTransporter(id: number): void {
    const t = this.transporters.find(x => x.id === id);
    if (!t) return;
    this.editingTransporter = t;
    this.formName = t.name;
    this.formEmail = t.email;
    this.formPhone = t.phone;
    this.formAddress = t.address;
    this.formDeliveryFee = t.deliveryFee;
    this.formStatus = t.status;
    this.showModal = true;
  }

  saveTransporter(): void {
    if (!this.formName.trim() || !this.formEmail.trim()) return;
    this.isSaving = true;

    const payload = {
      name: this.formName.trim(),
      email: this.formEmail.trim(),
      phone: this.formPhone.trim(),
      address: this.formAddress.trim(),
      deliveryFee: this.formDeliveryFee,
      status: this.formStatus
    };

    const request$ = this.editingTransporter
      ? this.adminService.updateTransporter(this.editingTransporter.id, payload)
      : this.adminService.createTransporter(payload);

    request$.subscribe({
      next: () => {
        this.showModal = false;
        this.isSaving = false;
        this.loadTransporters();
      },
      error: () => {
        alert('Failed to save transporter.');
        this.isSaving = false;
      }
    });
  }

  cancelModal(): void {
    this.showModal = false;
  }

  deleteTransporter(id: number): void {
    if (!confirm('Delete this transporter?')) return;
    this.adminService.deleteTransporter(id).subscribe({
      next: () => this.transporters = this.transporters.filter(t => t.id !== id),
      error: () => alert('Failed to delete transporter.')
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
