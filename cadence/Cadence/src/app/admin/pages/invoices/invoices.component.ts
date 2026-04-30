import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, AdminInvoice } from '../../services/admin.service';

@Component({
  selector: 'app-invoices',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './invoices.component.html',
  styleUrls: ['./invoices.component.css']
})
export class InvoicesComponent implements OnInit {
  invoices: AdminInvoice[] = [];
  isLoading = true;
  error: string | null = null;

  searchTerm: string = '';
  filterStatus: string = '';

  statuses: string[] = ['DRAFT', 'SENT', 'PAID', 'OVERDUE', 'CANCELLED'];
  generatingInvoice: { [key: number]: boolean } = {};

  isPreviewOpen = false;
  selectedInvoice: AdminInvoice | null = null;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadInvoices();
  }

  loadInvoices(): void {
    this.isLoading = true;
    this.error = null;
    this.adminService.getInvoices().subscribe({
      next: (invoices) => {
        this.invoices = invoices;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Could not load invoices. Make sure the backend is running.';
        this.isLoading = false;
      }
    });
  }

  get filteredInvoices(): AdminInvoice[] {
    return this.invoices.filter(i => {
      const matchesSearch = i.invoice_number.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           i.customer_name?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           i.order_id_display?.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = !this.filterStatus || i.status === this.filterStatus;
      return matchesSearch && matchesStatus;
    });
  }

  viewInvoice(id: number): void {
    const invoice = this.invoices.find(i => i.id_invoice === id);
    if (invoice) {
      this.previewInvoice(invoice);
    }
  }

  previewInvoice(invoice: AdminInvoice): void {
    this.selectedInvoice = invoice;
    this.isPreviewOpen = true;
    document.body.style.overflow = 'hidden';
  }

  closePreview(): void {
    this.isPreviewOpen = false;
    this.selectedInvoice = null;
    document.body.style.overflow = 'auto';
  }

  exportInvoices(): void {
    const csv = [
      ['Invoice #', 'Customer', 'Order ID', 'Date', 'Total', 'Currency', 'Status', 'Payment Method'],
      ...this.filteredInvoices.map(i => [
        i.invoice_number,
        i.customer_name,
        i.order_id_display,
        i.issue_date,
        i.total_amount,
        i.currency,
        i.status,
        i.payment_method
      ])
    ].map(row => row.join(',')).join('\n');

    const blob = new Blob([csv], { type: 'text/csv' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'invoices.csv';
    link.click();
  }

  getStatusColor(status: string): string {
    switch(status.toUpperCase()) {
      case 'DRAFT':    return '#6b7280';
      case 'SENT':     return '#3b82f6';
      case 'PAID':     return '#10b981';
      case 'OVERDUE':  return '#ef4444';
      case 'CANCELLED':return '#f59e0b';
      default:         return '#6b7280';
    }
  }

  getStatusIcon(status: string): string {
    switch(status.toUpperCase()) {
      case 'DRAFT':    return '📝';
      case 'SENT':     return '📤';
      case 'PAID':     return '✅';
      case 'OVERDUE':  return '⚠️';
      case 'CANCELLED':return '✕';
      default:         return '?';
    }
  }

  async generateInvoicePDF(invoice: AdminInvoice): Promise<void> {
    try {
      this.generatingInvoice[invoice.id_invoice] = true;
      
      const { jsPDF } = await import('jspdf');
      const html2canvas = (await import('html2canvas')).default;
      
      const invoiceHTML = this.generateInvoiceHTML(invoice);
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
      pdf.save(`Invoice-${invoice.invoice_number}.pdf`);
      
      // Mark PDF as generated
      invoice.pdf_generated = true;
      
    } catch (error) {
      console.error('Error generating PDF:', error);
      alert('Failed to generate invoice PDF');
    } finally {
      this.generatingInvoice[invoice.id_invoice] = false;
    }
  }

  downloadExistingPDF(invoice: AdminInvoice): void {
    if (invoice.pdf_path) {
      const link = document.createElement('a');
      link.href = invoice.pdf_path;
      link.download = `Invoice-${invoice.invoice_number}.pdf`;
      link.click();
    } else {
      alert('No PDF file available for this invoice');
    }
  }

  private generateInvoiceHTML(invoice: AdminInvoice): HTMLElement {
    const div = document.createElement('div');
    div.style.padding = '20px';
    div.style.fontFamily = 'Arial, sans-serif';
    div.style.backgroundColor = '#ffffff';
    div.style.width = '800px';
    
    div.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #4a8b6f; padding-bottom: 20px; margin-bottom: 20px;">
        <div>
          <h1 style="color: #4a8b6f; margin: 0; font-size: 28px;">INVOICE</h1>
          <p style="color: #666; margin: 5px 0 0 0; font-size: 12px;">Cadence ERP System</p>
        </div>
        <div style="text-align: right;">
          <p style="font-size: 14px; margin: 0; font-weight: bold;">Invoice #: <span style="color: #4a8b6f;">${invoice.invoice_number}</span></p>
          <p style="font-size: 12px; color: #666; margin: 5px 0;">Issue Date: ${invoice.issue_date}</p>
          <p style="font-size: 12px; color: #666; margin: 5px 0;">Due Date: ${invoice.due_date || 'N/A'}</p>
        </div>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 30px; margin-bottom: 30px;">
        <div>
          <h3 style="color: #333; font-size: 12px; text-transform: uppercase; margin: 0 0 10px 0; letter-spacing: 1px;">Bill To:</h3>
          <p style="margin: 0; font-weight: bold; font-size: 14px;">${invoice.customer_name || 'Unknown Customer'}</p>
          <p style="margin: 5px 0 0 0; font-size: 12px; color: #666; white-space: pre-wrap;">${invoice.billing_address || 'N/A'}</p>
        </div>
        <div>
          <h3 style="color: #333; font-size: 12px; text-transform: uppercase; margin: 0 0 10px 0; letter-spacing: 1px;">Order Reference:</h3>
          <p style="margin: 0; font-weight: bold; font-size: 14px; color: #4a8b6f;">${invoice.order_id_display || '#' + invoice.order_id}</p>
          <p style="margin: 5px 0 0 0; font-size: 12px; color: #666;">Currency: ${invoice.currency}</p>
        </div>
      </div>

      <div style="margin-bottom: 20px;">
        <h3 style="color: #333; font-size: 12px; text-transform: uppercase; margin: 0 0 10px 0; letter-spacing: 1px;">Status:</h3>
        <div style="display: inline-block; padding: 6px 12px; border-radius: 4px; background: ${this.getStatusColor(invoice.status)}20; color: ${this.getStatusColor(invoice.status)}; font-weight: 600; font-size: 12px;">
          ${this.getStatusIcon(invoice.status)} ${invoice.status}
        </div>
      </div>

      <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px; border-top: 1px solid #ddd; border-bottom: 2px solid #4a8b6f;">
        <thead>
          <tr style="background: #f8faf9;">
            <th style="padding: 12px; text-align: left; font-size: 12px; font-weight: 600; color: #333; border-bottom: 1px solid #ddd;">Description</th>
            <th style="padding: 12px; text-align: right; font-size: 12px; font-weight: 600; color: #333; border-bottom: 1px solid #ddd;">Amount</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td style="padding: 12px; font-size: 12px; color: #333;">Subtotal</td>
            <td style="padding: 12px; text-align: right; font-size: 12px; color: #333;">${invoice.currency} ${invoice.subtotal.toFixed(2)}</td>
          </tr>
          ${invoice.shipping_cost > 0 ? `
          <tr>
            <td style="padding: 12px; font-size: 12px; color: #333;">Shipping Cost</td>
            <td style="padding: 12px; text-align: right; font-size: 12px; color: #333;">${invoice.currency} ${invoice.shipping_cost.toFixed(2)}</td>
          </tr>
          ` : ''}
          ${invoice.discount_amount > 0 ? `
          <tr>
            <td style="padding: 12px; font-size: 12px; color: #333;">Discount</td>
            <td style="padding: 12px; text-align: right; font-size: 12px; color: #ef4444;">-${invoice.currency} ${invoice.discount_amount.toFixed(2)}</td>
          </tr>
          ` : ''}
          ${invoice.tax_amount > 0 ? `
          <tr>
            <td style="padding: 12px; font-size: 12px; color: #333;">Tax (${invoice.tax_rate}%)</td>
            <td style="padding: 12px; text-align: right; font-size: 12px; color: #333;">${invoice.currency} ${invoice.tax_amount.toFixed(2)}</td>
          </tr>
          ` : ''}
        </tbody>
      </table>

      <div style="display: flex; justify-content: flex-end; margin-bottom: 30px;">
        <div style="width: 250px;">
          <div style="display: flex; justify-content: space-between; padding: 8px 0; border-top: 1px solid #ddd; font-size: 12px;">
            <span style="font-weight: 600; color: #333;">Total:</span>
            <span style="color: #4a8b6f; font-weight: bold; font-size: 14px;">${invoice.currency} ${invoice.total_amount.toFixed(2)}</span>
          </div>
          ${invoice.paid_date ? `
          <div style="display: flex; justify-content: space-between; padding: 8px 0; font-size: 11px; color: #666;">
            <span>Paid on:</span>
            <span>${invoice.paid_date}</span>
          </div>
          ` : ''}
          ${invoice.payment_method ? `
          <div style="display: flex; justify-content: space-between; padding: 8px 0; font-size: 11px; color: #666;">
            <span>Payment Method:</span>
            <span>${invoice.payment_method}</span>
          </div>
          ` : ''}
        </div>
      </div>

      ${invoice.notes ? `
      <div style="background: #f9fafb; padding: 15px; margin-bottom: 20px; border-radius: 4px;">
        <h4 style="margin: 0 0 8px 0; font-size: 12px; color: #333;">Notes:</h4>
        <p style="margin: 0; font-size: 11px; color: #666; white-space: pre-wrap;">${invoice.notes}</p>
      </div>
      ` : ''}

      <div style="border-top: 1px solid #ddd; padding-top: 20px; text-align: center;">
        <p style="font-size: 11px; color: #999; margin: 0;">Thank you for your business!</p>
        <p style="font-size: 11px; color: #999; margin: 5px 0 0 0;">Generated on ${new Date().toLocaleString()}</p>
      </div>
    `;
    
    document.body.appendChild(div);
    return div;
  }
}
