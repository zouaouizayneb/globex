import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AdminService, AdminOrder, AdminProduct, AdminClient, AdminCategory, StockAlert } from '../services/admin.service';

@Component({
  selector: 'app-admin-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-navbar.component.html',
  styleUrls: ['./admin-navbar.component.css']
})
export class AdminNavbarComponent implements OnInit {
  
  notificationDropdownOpen = false;
  profileDropdownOpen = false;

  @HostListener('document:mousedown', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.icon-btn') && !target.closest('.profile-btn') && !target.closest('.dropdown-menu')) {
      this.notificationDropdownOpen = false;
      this.profileDropdownOpen = false;
      this.searchDropdownOpen = false;
    }
  }

  pendingOrders: AdminOrder[] = [];
  lowStockAlerts: StockAlert[] = [];
  
  searchQuery = '';
  searchDropdownOpen = false;

  allProducts: AdminProduct[] = [];
  allClients: AdminClient[] = [];
  allCategories: AdminCategory[] = [];
  allOrders: AdminOrder[] = [];

  searchResults = {
    products: [] as AdminProduct[],
    clients: [] as AdminClient[],
    categories: [] as AdminCategory[],
    orders: [] as AdminOrder[]
  };

  get totalNotifications(): number {
    return this.pendingOrders.length + this.lowStockAlerts.length;
  }

  constructor(private router: Router, private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadNotifications();
    
    // Listen for refresh signals from other components
    this.adminService.refreshSignal$.subscribe(() => {
      this.loadNotifications();
    });

    this.adminService.getProducts().subscribe(p => this.allProducts = p);
    this.adminService.getClients().subscribe(c => this.allClients = c);
    this.adminService.getCategories().subscribe(c => this.allCategories = c);
  }

  loadNotifications(): void {
    this.adminService.getAllOrders().subscribe(orders => {
      this.allOrders = orders;
      const twentyFourHoursAgo = new Date(Date.now() - 24 * 60 * 60 * 1000);
      
      this.pendingOrders = orders.filter(o => {
        const isPending = o.status && o.status.toUpperCase() === 'PENDING';
        const orderDate = new Date(o.date);
        // Only show if it's pending AND was created within the last 24 hours
        return isPending && orderDate > twentyFourHoursAgo;
      });
    });

    this.adminService.getLowStockAlerts().subscribe(alerts => {
      this.lowStockAlerts = alerts;
    });
  }

  onSearch(): void {
    if (!this.searchQuery.trim()) {
      this.searchDropdownOpen = false;
      return;
    }
    
    const q = this.searchQuery.toLowerCase();
    this.searchResults.products = this.allProducts.filter(p => p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q)).slice(0, 3);
    this.searchResults.clients = this.allClients.filter(c => c.name.toLowerCase().includes(q) || c.email.toLowerCase().includes(q)).slice(0, 3);
    this.searchResults.categories = this.allCategories.filter(c => c.name.toLowerCase().includes(q)).slice(0, 3);
    this.searchResults.orders = this.allOrders.filter(o => o.id.toString().toLowerCase().includes(q) || o.customer.toLowerCase().includes(q)).slice(0, 3);
    
    this.searchDropdownOpen = 
      this.searchResults.products.length > 0 || 
      this.searchResults.clients.length > 0 || 
      this.searchResults.categories.length > 0 || 
      this.searchResults.orders.length > 0;
  }

  goToSearch(path: string): void {
    this.router.navigate([path]);
    this.searchDropdownOpen = false;
    this.searchQuery = '';
  }

  toggleNotificationDropdown(event: MouseEvent) {
    event.stopPropagation();
    this.notificationDropdownOpen = !this.notificationDropdownOpen;
    this.profileDropdownOpen = false;
  }

  toggleProfileDropdown(event: MouseEvent) {
    event.stopPropagation();
    this.profileDropdownOpen = !this.profileDropdownOpen;
    this.notificationDropdownOpen = false;
  }

  goToProfile() {
    this.router.navigate(['/admin/account']);
    this.profileDropdownOpen = false;
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    
    this.router.navigate(['/login']);
    
    this.profileDropdownOpen = false;
  }
}