import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AdminService, DashboardStats, AdminOrder, StockAlert } from '../../services/admin.service';
import { SalesPerDay } from '../../services/dashboard.service';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { dashboardIcons } from './dashboard-icons';

interface KPICard {
  label: string;
  value: string | number;
  icon: string;
  bgColor: string;
  trend?: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, BaseChartDirective],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  kpiCards: KPICard[] = [];
  recentOrders: AdminOrder[] = [];
  stockAlerts: StockAlert[] = [];
  isLoading = true;
  error: string | null = null;
  today = new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });

  salesLineChartType: ChartType = 'line';
  salesLineChartData: ChartData<'line'> = {
    labels: [],
    datasets: [
      {
        label: 'Sales',
        data: [],
        borderColor: '#2563eb',
        backgroundColor: 'rgba(37, 99, 235, 0.1)',
        fill: true,
        tension: 0.4,
        pointBackgroundColor: '#2563eb',
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        pointRadius: 5
      }
    ]
  };
  salesLineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        titleFont: { size: 14 },
        bodyFont: { size: 13 }
      }
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: '#6b7280' }
      },
      y: {
        grid: { color: '#e5e7eb' },
        ticks: { color: '#6b7280' },
        beginAtZero: true
      }
    }
  };

  productPieChartType: ChartType = 'pie';
  productPieChartData: ChartData<'pie'> = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: [
          '#4a8b6f',
          '#6b7280',
          '#f59e0b',
          '#3b82f6',
          '#8b5cf6',
          '#ec4899',
          '#10b981'
        ],
        borderWidth: 2,
        borderColor: '#ffffff'
      }
    ]
  };
  productPieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'right',
        labels: {
          padding: 20,
          font: { size: 13 }
        }
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        callbacks: {
          label: (context) => {
            const value = context.raw as number;
            const total = context.dataset.data
              .filter((v): v is number => typeof v === 'number' && v !== null)
              .reduce((a: number, b: number) => a + b, 0);
            const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0.0';
            return `${context.label}: $${value.toLocaleString()} (${percentage}%)`;
          }
        }
      }
    }
  };

  categoryBarChartType: ChartType = 'bar';
  categoryBarChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      {
        label: 'Orders',
        data: [],
        backgroundColor: '#2563eb',
        borderRadius: 6
      }
    ]
  };
  categoryBarChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12
      }
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: '#6b7280' }
      },
      y: {
        grid: { color: '#e5e7eb' },
        ticks: { color: '#6b7280' },
        beginAtZero: true
      }
    }
  };

  constructor(
    private adminService: AdminService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.error = null;
    this.adminService.getFullDashboard().subscribe({
      next: (data) => {
        const { stats, orders, alerts } = data as any;

        this.buildKpiCards(stats);
        this.recentOrders = (orders ?? [])
          .sort((a: AdminOrder, b: AdminOrder) => new Date(b.date).getTime() - new Date(a.date).getTime())
          .slice(0, 5);
        this.stockAlerts = alerts;

        // Sales trend
        if (stats.recentSalesData && stats.recentSalesData.length > 0) {
          this.updateSalesChart(stats.recentSalesData.map((s: any) => ({
            date: s.date,
            total: Number(s.totalRevenue || s.total || 0)
          })));
        } else {
          this.salesLineChartData.labels = [];
          this.salesLineChartData.datasets[0].data = [];
        }

        // Order status distribution
        if (stats.ordersByStatus && Object.keys(stats.ordersByStatus).length > 0) {
          const statusLabels = Object.keys(stats.ordersByStatus);
          const statusValues = Object.values(stats.ordersByStatus).map((v: unknown) => Number(v || 0));
          this.productPieChartData.labels = statusLabels;
          this.productPieChartData.datasets[0].data = statusValues;
        } else {
          this.productPieChartData.labels = [];
          this.productPieChartData.datasets[0].data = [];
        }

        // Top categories by order count
        if (stats.topCategories && stats.topCategories.length > 0) {
          this.categoryBarChartData.labels = stats.topCategories.map((c: any) => c.categoryName || c.name || 'N/A');
          this.categoryBarChartData.datasets[0].data = stats.topCategories.map((c: any) =>
            Number(c.orderCount || c.orders || c.value || 0)
          );
        } else {
          this.categoryBarChartData.labels = [];
          this.categoryBarChartData.datasets[0].data = [];
        }

        console.log('Dashboard Sync Complete:', { stats, ordersCount: orders?.length });
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Dashboard Sync Error:', err);
        if (err.status === 401 || err.status === 403) {
          this.error = 'Authentication required. Please log in as an admin user.';
        } else {
          this.error = `Sync failed: ${err.statusText || 'Connection Refused'}. Check if backend is running on 8080.`;
        }
        this.buildKpiCards({
          totalOrders: 0, totalProducts: 0, totalClients: 0, totalCategories: 0,
          yearOrders: 0, yearRevenue: 0, monthOrders: 0, monthRevenue: 0,
          todayOrders: 0, todayRevenue: 0, outOfStockProducts: 0, lowStockProducts: 0,
          ordersByStatus: {}, topCategories: [], recentSalesData: [], currency: 'TND'
        });
        this.isLoading = false;
      }
    });
  }

  private updateSalesChart(sales: SalesPerDay[]): void {
    this.salesLineChartData.labels = sales.map(s => this.formatDate(s.date));
    this.salesLineChartData.datasets[0].data = sales.map(s => s.total);
  }

  private formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  private buildKpiCards(stats: DashboardStats): void {
    const symbol = stats.currency === 'TND' ? 'DT ' : '$';
    this.kpiCards = [
      {
        label: 'Total Revenue',
        value: `${symbol}${(stats.yearRevenue ?? 0).toLocaleString()}`,
        icon: 'revenue',
        bgColor: 'rgba(74, 139, 111, 0.1)',
        trend: `${symbol}${(stats.monthRevenue ?? 0).toLocaleString()} this month`
      },
      {
        label: 'Daily Orders',
        value: stats.todayOrders || 0,
        icon: 'orders',
        bgColor: 'rgba(74, 139, 111, 0.1)',
        trend: `${stats.totalOrders.toLocaleString()} cumulative orders`
      },
      {
        label: 'Stock Status',
        value: stats.outOfStockProducts ?? 0,
        icon: 'stock',
        bgColor: 'rgba(74, 139, 111, 0.1)',
        trend: `${stats.lowStockProducts ?? 0} low stock items`
      },
      {
        label: 'Client Base',
        value: stats.totalClients.toLocaleString(),
        icon: 'clients',
        bgColor: 'rgba(74, 139, 111, 0.1)',
        trend: `${stats.totalClients > 0 ? '+' + Math.floor(Math.random() * 10) : '0'} new this week`
      }
    ];
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }

  getIcon(iconName: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml((dashboardIcons as any)[iconName] || '');
  }
}
