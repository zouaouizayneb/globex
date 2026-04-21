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

  searchTerm: string = '';
  filterStatus: string = '';

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
      },
      error: () => {
        this.error = 'Could not load stock data. Make sure the backend is running.';
        this.isLoading = false;
      }
    });
  }

  loadLowStockAlerts(): void {
    this.adminService.getLowStockAlerts(10).subscribe({
      next: (alerts) => {
        this.lowStockAlerts = alerts;
      }
    });
  }

  get filteredStocks(): StockItem[] {
    return this.stocks.filter(s => {
      const matchesSearch = s.productName.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = !this.filterStatus || s.status === this.filterStatus;
      return matchesSearch && matchesStatus;
    });
  }

  increment(stock: StockItem) {
    stock.quantity++;
    this.updateStock(stock);
  }

  decrement(stock: StockItem) {
    if (stock.quantity > 0) {
      stock.quantity--;
      this.updateStock(stock);
    }
  }

  updateStock(stock: StockItem) {
    this.adminService.updateStockQuantity(stock.id, stock.quantity).subscribe({
      next: () => {
        this.updateStockStatus(stock);
        this.loadLowStockAlerts();
      },
      error: () => {
        alert('Failed to update stock. Please try again.');
      }
    });
  }

  updateStockStatus(stock: StockItem) {
    if (stock.quantity === 0) {
      stock.status = 'out-of-stock';
    } else if (stock.quantity <= 10) {
      stock.status = 'low-stock';
    } else {
      stock.status = 'in-stock';
    }
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
