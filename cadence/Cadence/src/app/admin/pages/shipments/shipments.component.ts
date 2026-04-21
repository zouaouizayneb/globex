import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, Shipment, AdminTransporter } from '../../services/admin.service';

@Component({
  selector: 'app-shipments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './shipments.component.html',
  styleUrls: ['./shipments.component.css']
})
export class ShipmentsComponent implements OnInit {
  shipments: Shipment[] = [];
  transporters: AdminTransporter[] = [];
  isLoading = true;
  error: string | null = null;

  searchTerm: string = '';
  filterStatus: string = '';
  filterShippingMethod: string = '';

  statuses: string[] = ['PENDING', 'PROCESSING', 'SHIPPED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED', 'RETURNED'];
  shippingMethods: string[] = ['STANDARD', 'EXPRESS', 'OVERNIGHT', 'INTERNATIONAL'];

  editingShipment: Shipment | null = null;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadShipments();
    this.loadTransporters();
  }

  loadShipments(): void {
    this.isLoading = true;
    this.error = null;
    this.adminService.getShipments().subscribe({
      next: (shipments) => {
        this.shipments = shipments;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Could not load shipments. Make sure the backend is running.';
        this.isLoading = false;
      }
    });
  }

  loadTransporters(): void {
    this.adminService.getTransporters().subscribe({
      next: (transporters) => {
        this.transporters = transporters.filter(t => t.status === 'active');
      },
      error: () => {
        console.error('Failed to load transporters');
      }
    });
  }

  get filteredShipments(): Shipment[] {
    return this.shipments.filter(s => {
      const matchesSearch = s.trackingNumber?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           s.carrier?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           s.orderId?.toString().includes(this.searchTerm);
      const matchesStatus = !this.filterStatus || s.status === this.filterStatus;
      const matchesMethod = !this.filterShippingMethod || s.shippingMethod === this.filterShippingMethod;
      return matchesSearch && matchesStatus && matchesMethod;
    });
  }

  editShipment(shipment: Shipment): void {
    this.editingShipment = { ...shipment };
  }

  cancelEdit(): void {
    this.editingShipment = null;
  }

  saveShipment(): void {
    if (!this.editingShipment) return;

    this.adminService.updateShipment(this.editingShipment.idShip, this.editingShipment).subscribe({
      next: () => {
        this.loadShipments();
        this.editingShipment = null;
      },
      error: () => {
        alert('Failed to update shipment');
      }
    });
  }

  getStatusColor(status: string): string {
    switch(status?.toUpperCase()) {
      case 'PENDING':      return '#f59e0b';
      case 'PROCESSING':   return '#6366f1';
      case 'SHIPPED':      return '#8b5cf6';
      case 'IN_TRANSIT':   return '#3b82f6';
      case 'DELIVERED':    return '#10b981';
      case 'CANCELLED':    return '#ef4444';
      case 'RETURNED':     return '#ec4899';
      default:             return '#6b7280';
    }
  }

  getStatusIcon(status: string): string {
    switch(status?.toUpperCase()) {
      case 'PENDING':      return '⏳';
      case 'PROCESSING':   return '⚙️';
      case 'SHIPPED':      return '📦';
      case 'IN_TRANSIT':   return '🚚';
      case 'DELIVERED':    return '✓';
      case 'CANCELLED':    return '✕';
      case 'RETURNED':     return '↩';
      default:             return '?';
    }
  }

  getShippingMethodIcon(method: string): string {
    switch(method?.toUpperCase()) {
      case 'STANDARD':     return '🚚';
      case 'EXPRESS':      return '⚡';
      case 'OVERNIGHT':    return '🌙';
      case 'INTERNATIONAL': return '🌍';
      default:             return '📦';
    }
  }
}
