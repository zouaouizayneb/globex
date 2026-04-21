import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, AdminClient } from '../../services/admin.service';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clients.component.html',
  styleUrls: ['./clients.component.css']
})
export class ClientsComponent implements OnInit {
  clients: AdminClient[] = [];
  isLoading = true;
  error: string | null = null;

  searchTerm: string = '';
  filterStatus: string = '';

  showModal = false;
  editingClient: AdminClient | null = null;
  formName = '';
  formEmail = '';
  formPhone = '';
  formCountry = '';
  formStatus: 'active' | 'inactive' = 'active';
  isSaving = false;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {
    this.isLoading = true;
    this.error = null;
    this.adminService.getClients().subscribe({
      next: (clients) => {
        this.clients = clients;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Could not load clients. Make sure the backend is running.';
        this.isLoading = false;
      }
    });
  }

  get filteredClients(): AdminClient[] {
    return this.clients.filter(c => {
      const matchesSearch = c.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           c.email.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = !this.filterStatus || c.status === this.filterStatus;
      return matchesSearch && matchesStatus;
    });
  }

  addClient(): void {
    this.editingClient = null;
    this.formName = '';
    this.formEmail = '';
    this.formPhone = '';
    this.formCountry = '';
    this.formStatus = 'active';
    this.showModal = true;
  }

  editClient(id: number): void {
    const c = this.clients.find(x => x.id === id);
    if (!c) return;
    this.editingClient = c;
    this.formName = c.name;
    this.formEmail = c.email;
    this.formPhone = c.phone || '';
    this.formCountry = c.country || '';
    this.formStatus = c.status;
    this.showModal = true;
  }

  saveClient(): void {
    if (!this.formName.trim() || !this.formEmail.trim()) return;
    this.isSaving = true;

    const payload = {
      name: this.formName.trim(),
      email: this.formEmail.trim(),
      phone: this.formPhone.trim(),
      country: this.formCountry.trim(),
      status: this.formStatus
    };

    const request$ = this.editingClient
      ? this.adminService.updateClient(this.editingClient.id, payload)
      : this.adminService.createClient(payload);

    request$.subscribe({
      next: () => {
        this.showModal = false;
        this.isSaving = false;
        this.loadClients();
      },
      error: () => {
        alert('Failed to save client.');
        this.isSaving = false;
      }
    });
  }

  cancelModal(): void {
    this.showModal = false;
  }

  deleteClient(id: number): void {
    if (!confirm('Delete this client?')) return;
    this.adminService.deleteClient(id).subscribe({
      next: () => this.clients = this.clients.filter(c => c.id !== id),
      error: () => alert('Failed to delete client.')
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
