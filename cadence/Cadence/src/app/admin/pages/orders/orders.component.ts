import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, AdminOrder } from '../../services/admin.service';

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
  generatingInvoice: { [key: string]: boolean } = {};

  isPreviewOpen = false;
  selectedOrder: AdminOrder | null = null;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.error = null;
    this.adminService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Could not load orders. Make sure the backend is running.';
        this.isLoading = false;
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

  async generateInvoicePDF(order: AdminOrder): Promise<void> {
    try {
      this.generatingInvoice[order.id] = true;
      
      const { jsPDF } = await import('jspdf');
      const html2canvas = (await import('html2canvas')).default;
      
      const invoiceHTML = this.generateInvoiceHTML(order);
      const canvas = await html2canvas(invoiceHTML, {
        scale: 2,
        useCORS: true,
        backgroundColor: '#ffffff'
      });
      
      const imgData = canvas.toDataURL('image/png');
      const pdf = new jsPDF({
        orientation: 'portrait',
        unit: 'mm',
        format: 'a4'
      });
      
      const imgWidth = 210 - 20;
      const imgHeight = (canvas.height * imgWidth) / canvas.width;
      
      pdf.addImage(imgData, 'PNG', 10, 10, imgWidth, imgHeight);
      pdf.save(`Invoice-${order.id}.pdf`);
      
    } catch (error) {
      console.error('Error generating PDF:', error);
      alert('Failed to generate invoice PDF');
    } finally {
      this.generatingInvoice[order.id] = false;
    }
  }

  private generateInvoiceHTML(order: AdminOrder): HTMLElement {
    const div = document.createElement('div');
    div.style.padding = '20px';
    div.style.fontFamily = 'Arial, sans-serif';
    div.style.backgroundColor = '#ffffff';
    div.style.width = '800px';
    
    const today = new Date().toLocaleDateString();
    
    div.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #4a8b6f; padding-bottom: 20px; margin-bottom: 20px;">
        <div>
          <h1 style="color: #4a8b6f; margin: 0; font-size: 28px;">INVOICE</h1>
          <p style="color: #666; margin: 5px 0 0 0; font-size: 12px;">Cadence ERP System</p>
        </div>
        <div style="text-align: right;">
          <p style="font-size: 14px; margin: 0; font-weight: bold;">Invoice #: <span style="color: #4a8b6f;">${order.id}</span></p>
          <p style="font-size: 12px; color: #666; margin: 5px 0;">Date: ${today}</p>
          <p style="font-size: 12px; color: #666; margin: 5px 0;">Order Date: ${order.date}</p>
        </div>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 30px; margin-bottom: 30px;">
        <div>
          <h3 style="color: #333; font-size: 12px; text-transform: uppercase; margin: 0 0 10px 0; letter-spacing: 1px;">Bill To:</h3>
          <p style="margin: 0; font-weight: bold; font-size: 14px;">${order.customer}</p>
          <p style="margin: 5px 0 0 0; font-size: 12px; color: #666;">Customer</p>
        </div>
        <div>
          <h3 style="color: #333; font-size: 12px; text-transform: uppercase; margin: 0 0 10px 0; letter-spacing: 1px;">Status:</h3>
          <div style="display: inline-block; padding: 6px 12px; border-radius: 4px; background: ${this.getStatusColor(order.status)}20; color: ${this.getStatusColor(order.status)}; font-weight: 600; font-size: 12px;">
            ${order.status}
          </div>
        </div>
      </div>

      <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px; border-top: 1px solid #ddd; border-bottom: 2px solid #4a8b6f;">
        <thead>
          <tr style="background: #f8faf9;">
            <th style="padding: 12px; text-align: left; font-size: 12px; font-weight: 600; color: #333; border-bottom: 1px solid #ddd;">Description</th>
            <th style="padding: 12px; text-align: center; font-size: 12px; font-weight: 600; color: #333; border-bottom: 1px solid #ddd;">Qty</th>
            <th style="padding: 12px; text-align: right; font-size: 12px; font-weight: 600; color: #333; border-bottom: 1px solid #ddd;">Amount</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td style="padding: 12px; font-size: 12px; color: #333;">Order Items</td>
            <td style="padding: 12px; text-align: center; font-size: 12px; color: #333;">${order.items}</td>
            <td style="padding: 12px; text-align: right; font-size: 12px; color: #333; font-weight: 600;">$${order.total.toFixed(2)}</td>
          </tr>
        </tbody>
      </table>

      <div style="display: flex; justify-content: flex-end; margin-bottom: 30px;">
        <div style="width: 250px;">
          <div style="display: flex; justify-content: space-between; padding: 8px 0; border-top: 1px solid #ddd; font-size: 12px;">
            <span style="font-weight: 600; color: #333;">Subtotal:</span>
            <span style="color: #666;">$${order.total.toFixed(2)}</span>
          </div>
          <div style="display: flex; justify-content: space-between; padding: 8px 0; border-top: 1px solid #ddd; font-size: 14px; font-weight: bold;">
            <span style="color: #4a8b6f;">Total:</span>
            <span style="color: #4a8b6f;">$${order.total.toFixed(2)}</span>
          </div>
        </div>
      </div>

      <div style="border-top: 1px solid #ddd; padding-top: 20px; text-align: center;">
        <p style="font-size: 11px; color: #999; margin: 0;">Thank you for your business!</p>
        <p style="font-size: 11px; color: #999; margin: 5px 0 0 0;">Generated on ${new Date().toLocaleString()}</p>
      </div>
    `;
    
    document.body.appendChild(div);
    return div;
  }
}

