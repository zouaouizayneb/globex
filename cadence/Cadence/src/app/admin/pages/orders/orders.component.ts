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
      }
    });
  }

  loadShipments(): void {
    this.adminService.getShipments().subscribe({
      next: (shipments) => {
        this.shipments = shipments;
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

    this.adminService.updateOrderStatus(order.orderId, selectedStatus).subscribe({
      next: () => {
        order.status = selectedStatus;

        if (!transporter) {
          return;
        }

        const shipmentPayload: any = {
          carrier: transporter.name,
          status: selectedStatus,
          dateShip: new Date().toISOString().split('T')[0],
          shippingMethod: 'STANDARD',
          order: { idOrder: order.orderId }
        };

        const shipmentRequest = existingShipment
          ? this.adminService.updateShipment(existingShipment.idShip, shipmentPayload)
          : this.adminService.createShipment(shipmentPayload);

        shipmentRequest.subscribe({
          next: () => {
            order.transporterName = transporter.name;
            this.loadShipments();
          },
          error: () => {
            this.error = 'Order status updated, but transporter assignment failed.';
          }
        });
      },
      error: () => {
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

