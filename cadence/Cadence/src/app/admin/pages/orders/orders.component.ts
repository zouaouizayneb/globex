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
        console.log('Loaded orders:', orders);
        this.orders = orders.map(order => ({
          ...order,
          selectedStatus: order.status,
          selectedTransporterId: null,
          isSaving: false,
          showSuccess: false
        }));
        console.log('Orders with selection state:', this.orders);
        this.isLoading = false;
        // Initialize transporter selection after orders are loaded
        this.initializeTransporterSelection();
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
        // Initialize transporter selection after transporters are loaded
        this.initializeTransporterSelection();
        this.syncTransporterSelection();
      }
    });
  }

  initializeTransporterSelection(): void {
    if (!this.orders.length || !this.transporters.length) return;

    console.log('Initializing transporter selection');
    this.orders.forEach(order => {
      if (order.transporterName) {
        const transporter = this.transporters.find(t => t.name === order.transporterName);
        if (transporter) {
          console.log(`Setting transporter ${transporter.id} for order ${order.orderId}`);
          order.selectedTransporterId = transporter.id;
        }
      }
    });
    console.log('Orders after transporter init:', this.orders);
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

    console.log('Syncing transporter selection from shipments');
    this.shipments.forEach(s => {
      if (s.orderId && s.carrier) {
        const transporter = this.transporters.find(t => t.name === s.carrier);
        if (transporter) {
          console.log(`Syncing: shipment orderId ${s.orderId} with carrier ${s.carrier}`);
          const order = this.orders.find(o => o.orderId === s.orderId);
          if (order) {
            order.selectedTransporterId = transporter.id;
          }
        }
      }
    });
    console.log('Orders after sync:', this.orders);
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
    if (!order.orderId) {
      console.error('Cannot save order: orderId is undefined', order);
      this.error = 'Cannot update order: Missing order ID';
      return;
    }

    const selectedStatus = order.selectedStatus || order.status;
    const selectedTransporterId = order.selectedTransporterId;
    const transporter = this.transporters.find(t => t.id === selectedTransporterId);
    const existingShipment = this.shipments.find(s => s.orderId === order.orderId);

    console.log(`Saving updates for order ${order.orderId}: status=${selectedStatus}, transporter=${selectedTransporterId}`);
    console.log('Order object:', order);

    order.isSaving = true;
    this.adminService.adminUpdateOrder(order.orderId, selectedStatus, selectedTransporterId).subscribe({
      next: (updatedOrder) => {
        order.status = selectedStatus;
        order.transporterName = updatedOrder.shipment?.carrier || transporter?.name || order.transporterName;

        order.isSaving = false;
        order.showSuccess = true;
        setTimeout(() => order.showSuccess = false, 3000);

        this.loadShipments(); // Refresh shipments list to stay in sync
      },
      error: () => {
        order.isSaving = false;
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

