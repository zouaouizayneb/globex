import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, AdminOrder, AdminTransporter, Shipment } from '../../services/admin.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {
  orders: AdminOrder[] = [];
  isLoading = true;
  error: string | null = null;

  searchTerm: string = '';
  filterStatus: string = '';

  statuses: string[] = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'];
  transporters: AdminTransporter[] = [];
  shipments: Shipment[] = [];
  selectedStatusByOrder: Record<number, AdminOrder['status']> = {};
  selectedTransporterByOrder: Record<number, number | null> = {};
  isSaving: Record<number, boolean> = {};
  showSuccess: Record<number, boolean> = {};

  isPreviewOpen = false;
  selectedOrder: AdminOrder | null = null;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadOrders();
    this.loadTransporters();
    this.loadShipments();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.error = null;
    this.adminService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.orders.forEach(order => {
          this.selectedStatusByOrder[order.orderId] = order.status;
          if (order.transporterName && this.transporters.length) {
            const transporter = this.transporters.find(t => t.name === order.transporterName);
            if (transporter) {
              this.selectedTransporterByOrder[order.orderId] = transporter.id;
            }
          }
        });
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Could not load orders. Make sure the backend is running.';
        this.isLoading = false;
      }
    });
  }

  loadTransporters(): void {
    this.adminService.getTransporters().subscribe({
      next: (transporters) => {
        this.transporters = transporters.filter(t => t.status !== 'inactive');
        
        // Sync with already loaded orders
        if (this.orders.length) {
          this.orders.forEach(order => {
            if (order.transporterName) {
              const transporter = this.transporters.find(t => t.name === order.transporterName);
              if (transporter) {
                this.selectedTransporterByOrder[order.orderId] = transporter.id;
              }
            }
          });
        }
        
        this.syncTransporterSelection();
      }
    });
  }

  loadShipments(): void {
    this.adminService.getShipments().subscribe({
      next: (shipments) => {
        this.shipments = shipments;
        this.syncTransporterSelection();
      }
    });
  }

  syncTransporterSelection(): void {
    if (!this.transporters.length || !this.shipments.length) return;
    
    this.shipments.forEach(s => {
      if (s.orderId && s.carrier) {
        const transporter = this.transporters.find(t => t.name === s.carrier);
        if (transporter) {
          this.selectedTransporterByOrder[s.orderId] = transporter.id;
        }
      }
    });
  }

  get filteredOrders(): AdminOrder[] {
    return this.orders.filter(o => {
      const matchesSearch = o.id.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           o.customer.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = !this.filterStatus || o.status === this.filterStatus;
      return matchesSearch && matchesStatus;
    });
  }

  viewOrder(id: string): void {
    const order = this.orders.find(o => o.id === id);
    if (order) {
      this.previewOrder(order);
    }
  }

  saveOrderUpdates(order: AdminOrder): void {
    const selectedStatus = this.selectedStatusByOrder[order.orderId];
    const selectedTransporterId = this.selectedTransporterByOrder[order.orderId];
    const transporter = this.transporters.find(t => t.id === selectedTransporterId);
    const existingShipment = this.shipments.find(s => s.orderId === order.orderId);

    this.isSaving[order.orderId] = true;
    this.adminService.adminUpdateOrder(order.orderId, selectedStatus, selectedTransporterId).subscribe({
      next: (updatedOrder) => {
        order.status = selectedStatus;
        order.transporterName = updatedOrder.shipment?.carrier || transporter?.name || order.transporterName;
        
        // Update selection state explicitly
        if (selectedTransporterId) {
          this.selectedTransporterByOrder[order.orderId] = selectedTransporterId;
        }

        this.isSaving[order.orderId] = false;
        this.showSuccess[order.orderId] = true;
        setTimeout(() => this.showSuccess[order.orderId] = false, 3000);
        
        this.loadShipments(); // Refresh shipments list to stay in sync
      },
      error: () => {
        this.isSaving[order.orderId] = false;
        this.error = `Could not update ${order.id}.`;
      }
    });
  }

  previewOrder(order: AdminOrder): void {
    this.selectedOrder = order;
    this.isPreviewOpen = true;
    document.body.style.overflow = 'hidden'; // Prevent background scrolling
  }

  closePreview(): void {
    this.isPreviewOpen = false;
    this.selectedOrder = null;
    document.body.style.overflow = 'auto';
  }

  exportOrders(): void {
    const csv = [
      ['Order ID', 'Customer', 'Date', 'Total', 'Status', 'Items'],
      ...this.filteredOrders.map(o => [o.id, o.customer, o.date, o.total, o.status, o.items])
    ].map(row => row.join(',')).join('\n');

    const blob = new Blob([csv], { type: 'text/csv' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'orders.csv';
    link.click();
  }

  getStatusColor(status: string): string {
    switch(status.toUpperCase()) {
      case 'PENDING':    return '#f59e0b';
      case 'CONFIRMED':  return '#3b82f6';
      case 'PROCESSING': return '#6366f1';
      case 'SHIPPED':    return '#8b5cf6';
      case 'DELIVERED':  return '#10b981';
      case 'CANCELLED':  return '#ef4444';
      case 'REFUNDED':   return '#ec4899';
      default:           return '#6b7280';
    }
  }

  getStatusIcon(status: string): string {
    switch(status.toUpperCase()) {
      case 'PENDING':    return '⏳';
      case 'CONFIRMED':  return '✅';
      case 'PROCESSING': return '⚙️';
      case 'SHIPPED':    return '📦';
      case 'DELIVERED':  return '✓';
      case 'CANCELLED':  return '✕';
      case 'REFUNDED':   return '↩';
      default:           return '?';
    }
  }

}

