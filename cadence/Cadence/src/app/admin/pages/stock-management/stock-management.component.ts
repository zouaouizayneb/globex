import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, StockItem, StockAlert } from '../../services/admin.service';

@Component({
  selector: 'app-stock-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stock-management.component.html',
  styleUrls: ['./stock-management.component.css']
})
export class StockManagementComponent implements OnInit {
  stocks: StockItem[] = [];
  lowStockAlerts: StockAlert[] = [];
  isLoading = true;
  error: string | null = null;

  // UI helpers
  searchTerm = '';
  filterStatus = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadStock();
  }

  loadStock(): void {
    this.isLoading = true;
    this.error = null;
    this.adminService.getStock().subscribe({
      next: (stocks) => {
        this.stocks = stocks;
        this.loadLowStockAlerts();
        this.isLoading = false;
        this.adminService.triggerRefresh();
      },
      error: () => {
        this.error = 'Could not load stock data. Make sure the backend is running.';
        this.isLoading = false;
      }
    });
  }

  loadLowStockAlerts(): void {
    this.adminService.getLowStockAlerts(10).subscribe({
      next: alerts => this.lowStockAlerts = alerts
    });
  }

  get filteredStocks() {
    return this.stocks.filter(s => {
      const matchesSearch = s.productName.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = this.filterStatus ? s.status === this.filterStatus : true;
      return matchesSearch && matchesStatus;
    });
  }

  incrementStock(stock: StockItem): void {
    stock.quantity++;
    this.updateStock(stock);
  }

  decrementStock(stock: StockItem): void {
    if (stock.quantity > 0) {
      stock.quantity--;
      this.updateStock(stock);
    }
  }

  private updateStock(stock: StockItem): void {
    this.adminService.updateStockQuantity(stock.id, stock.quantity).subscribe({
      next: () => {
        this.loadStock();
        this.loadLowStockAlerts();
        this.adminService.triggerRefresh();
      },
      error: () => alert('Failed to update stock. Please try again.')
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'in-stock': return 'status-in-stock';
      case 'low-stock': return 'status-low-stock';
      case 'out-of-stock': return 'status-out-stock';
      default: return '';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'in-stock': return 'In Stock';
      case 'low-stock': return 'Low Stock';
      case 'out-of-stock': return 'Out of Stock';
      default: return status;
    }
  }
}
