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

  getStatusColor(status: string | undefined): string {
    switch(status?.toUpperCase()) {
      case 'DRAFT':    return '#6b7280';
      case 'SENT':     return '#3b82f6';
      case 'PAID':     return '#10b981';
      case 'OVERDUE':  return '#ef4444';
      case 'CANCELLED':return '#f59e0b';
      default:         return '#6b7280';
    }
  }

  getStatusIcon(status: string | undefined): string {
    switch(status?.toUpperCase()) {
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

      const pdf = new jsPDF({
        orientation: 'portrait',
        unit: 'mm',
        format: 'a4'
      });

      // Add header
      pdf.setFontSize(28);
      pdf.setTextColor(74, 139, 111);
      pdf.text('INVOICE', 105, 25, { align: 'center' });

      pdf.setFontSize(10);
      pdf.setTextColor(102, 102, 102);
      pdf.text('Cadence ERP System', 105, 32, { align: 'center' });

      // Add invoice details
      pdf.setFontSize(12);
      pdf.setTextColor(0, 0, 0);
      pdf.text(`Invoice #: ${invoice.invoice_number || 'N/A'}`, 150, 50, { align: 'right' });
      pdf.setFontSize(10);
      pdf.setTextColor(102, 102, 102);
      pdf.text(`Issue Date: ${invoice.issue_date || 'N/A'}`, 150, 56, { align: 'right' });
      pdf.text(`Due Date: ${invoice.due_date || 'N/A'}`, 150, 62, { align: 'right' });

      // Add bill to section
      pdf.setFontSize(10);
      pdf.setTextColor(51, 51, 51);
      pdf.setFont('helvetica', 'bold');
      pdf.text('BILL TO:', 20, 80);
      pdf.setFont('helvetica', 'normal');
      pdf.setFontSize(12);
      pdf.setTextColor(0, 0, 0);
      pdf.text(invoice.customer_name || 'Unknown Customer', 20, 88);
      pdf.setFontSize(10);
      pdf.setTextColor(102, 102, 102);
      const addressLines = (invoice.billing_address || 'N/A').split('\n');
      addressLines.forEach((line, index) => {
        pdf.text(line, 20, 94 + (index * 5));
      });

      // Add order reference
      pdf.setFontSize(10);
      pdf.setTextColor(51, 51, 51);
      pdf.setFont('helvetica', 'bold');
      pdf.text('ORDER REFERENCE:', 120, 80);
      pdf.setFont('helvetica', 'normal');
      pdf.setFontSize(12);
      pdf.setTextColor(74, 139, 111);
      pdf.text(invoice.order_id_display || '#' + (invoice.order_id || 'N/A'), 120, 88);
      pdf.setFontSize(10);
      pdf.setTextColor(102, 102, 102);
      pdf.text(`Currency: ${invoice.currency || 'USD'}`, 120, 94);

      // Add status
      pdf.setFontSize(10);
      pdf.setTextColor(51, 51, 51);
      pdf.setFont('helvetica', 'bold');
      pdf.text('STATUS:', 20, 120);
      pdf.setFont('helvetica', 'normal');
      const statusColor = this.getStatusColor(invoice.status);
      const rgbColor = this.hexToRgb(statusColor);
      pdf.setTextColor(rgbColor.r, rgbColor.g, rgbColor.b);
      pdf.text(`${this.getStatusIcon(invoice.status || '')} ${invoice.status || 'N/A'}`, 45, 120);

      // Add table header
      pdf.setFillColor(248, 250, 249);
      pdf.rect(20, 130, 170, 8, 'F');
      pdf.setFontSize(10);
      pdf.setTextColor(51, 51, 51);
      pdf.setFont('helvetica', 'bold');
      pdf.text('Description', 25, 136);
      pdf.text('Amount', 175, 136, { align: 'right' });

      // Add table rows
      pdf.setFont('helvetica', 'normal');
      pdf.setTextColor(0, 0, 0);
      let y = 146;
      pdf.text('Subtotal', 25, y);
      pdf.text(`${invoice.currency || 'USD'} ${(invoice.subtotal || 0).toFixed(2)}`, 175, y, { align: 'right' });
      y += 8;

      if ((invoice.shipping_cost || 0) > 0) {
        pdf.text('Shipping Cost', 25, y);
        pdf.text(`${invoice.currency || 'USD'} ${(invoice.shipping_cost || 0).toFixed(2)}`, 175, y, { align: 'right' });
        y += 8;
      }

      if ((invoice.discount_amount || 0) > 0) {
        pdf.setTextColor(239, 68, 68);
        pdf.text('Discount', 25, y);
        pdf.text(`-${invoice.currency || 'USD'} ${(invoice.discount_amount || 0).toFixed(2)}`, 175, y, { align: 'right' });
        pdf.setTextColor(0, 0, 0);
        y += 8;
      }

      if ((invoice.tax_amount || 0) > 0) {
        pdf.text(`Tax (${invoice.tax_rate || 0}%)`, 25, y);
        pdf.text(`${invoice.currency || 'USD'} ${(invoice.tax_amount || 0).toFixed(2)}`, 175, y, { align: 'right' });
        y += 8;
      }

      // Add total
      pdf.setDrawColor(221, 221, 221);
      pdf.line(120, y + 5, 175, y + 5);
      y += 12;
      pdf.setFontSize(12);
      pdf.setFont('helvetica', 'bold');
      pdf.setTextColor(74, 139, 111);
      pdf.text('Total:', 120, y);
      pdf.text(`${invoice.currency || 'USD'} ${(invoice.total_amount || 0).toFixed(2)}`, 175, y, { align: 'right' });

      // Add payment info
      y += 10;
      pdf.setFontSize(9);
      pdf.setFont('helvetica', 'normal');
      pdf.setTextColor(102, 102, 102);
      if (invoice.paid_date) {
        pdf.text(`Paid on: ${invoice.paid_date}`, 120, y);
        y += 5;
      }
      if (invoice.payment_method) {
        pdf.text(`Payment Method: ${invoice.payment_method}`, 120, y);
      }

      // Add notes if present
      if (invoice.notes) {
        y += 15;
        pdf.setFillColor(249, 250, 251);
        pdf.rect(20, y, 170, 20, 'F');
        pdf.setFontSize(10);
        pdf.setTextColor(51, 51, 51);
        pdf.setFont('helvetica', 'bold');
        pdf.text('Notes:', 25, y + 7);
        pdf.setFont('helvetica', 'normal');
        pdf.setFontSize(9);
        pdf.setTextColor(102, 102, 102);
        const noteLines = pdf.splitTextToSize(invoice.notes, 160);
        pdf.text(noteLines, 25, y + 13);
      }

      // Add footer
      pdf.setFontSize(9);
      pdf.setTextColor(153, 153, 153);
      pdf.text('Thank you for your business!', 105, 280, { align: 'center' });
      pdf.text(`Generated on ${new Date().toLocaleString()}`, 105, 285, { align: 'center' });

      pdf.save(`Invoice-${invoice.invoice_number}.pdf`);

      // Mark PDF as generated
      invoice.pdf_generated = true;

    } catch (error) {
      console.error('Error generating PDF:', error);
      alert('Failed to generate invoice PDF: ' + (error as Error).message);
    } finally {
      this.generatingInvoice[invoice.id_invoice] = false;
    }
  }

  private hexToRgb(hex: string): { r: number; g: number; b: number } {
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
      r: parseInt(result[1], 16),
      g: parseInt(result[2], 16),
      b: parseInt(result[3], 16)
    } : { r: 0, g: 0, b: 0 };
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
}
