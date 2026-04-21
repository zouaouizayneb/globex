import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Subject } from 'rxjs';
import { takeUntil, forkJoin } from 'rxjs';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions, ChartType } from 'chart.js';

import { DashboardService, KPIData, SalesDataPoint, ProductData, CategoryOrderData } from './dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, HttpClientModule, BaseChartDirective],
  templateUrl: './dashboard-professional.component.html',
  styleUrls: ['./dashboard-professional.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  kpiData: KPIData = { revenue: 0, orders: 0, clients: 0 };

  lineChartData!: ChartConfiguration<'line'>['data'];
  lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: true,
    plugins: {
      legend: {
        display: true,
        position: 'top'
      },
      title: {
        display: false
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: {
          color: 'rgba(0, 0, 0, 0.05)'
        }
      },
      x: {
        grid: {
          display: false
        }
      }
    }
  };

  pieChartData!: ChartConfiguration<'pie'>['data'];
  pieChartOptions: ChartOptions<'pie'> = {
    responsive: true,
    maintainAspectRatio: true,
    plugins: {
      legend: {
        display: true,
        position: 'right'
      }
    }
  };

  barChartData!: ChartConfiguration<'bar'>['data'];
  barChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: true,
    plugins: {
      legend: {
        display: true,
        position: 'top'
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: {
          color: 'rgba(0, 0, 0, 0.05)'
        }
      },
      x: {
        grid: {
          display: false
        }
      }
    }
  };

  lineChartType: ChartType = 'line';
  pieChartType: ChartType = 'pie';
  barChartType: ChartType = 'bar';

  isLoadingKPI = true;
  isLoadingCharts = true;
  errorMessage = '';

  private destroy$ = new Subject<void>();

  private colors = {
    primary: '#4a8b6f',
    secondary: '#6fa89e',
    accent: '#8b5cf6',
    success: '#10b981',
    warning: '#f59e0b',
    danger: '#ef4444'
  };

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.loadDashboardData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }


  private loadDashboardData(): void {
    const dashboardData = this.dashboardService.getAllDashboardData();

    dashboardData.kpi$
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.kpiData = data;
          this.isLoadingKPI = false;
        },
        error: (err) => {
          console.error('Error loading KPI data:', err);
          this.errorMessage = 'Failed to load KPI data';
          this.isLoadingKPI = false;
        }
      });

    forkJoin([
      dashboardData.salesPerDay$,
      dashboardData.productDistribution$,
      dashboardData.ordersByCategory$
    ])
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ([salesData, productData, orderData]) => {
          this.initializeLineChart(salesData);
          this.initializePieChart(productData);
          this.initializeBarChart(orderData);
          this.isLoadingCharts = false;
        },
        error: (err) => {
          console.error('Error loading chart data:', err);
          this.errorMessage = 'Failed to load chart data';
          this.isLoadingCharts = false;
        }
      });
  }

  private initializeLineChart(salesData: SalesDataPoint[]): void {
    const dates = salesData.map(d => this.formatDate(d.date));
    const totals = salesData.map(d => d.total);

    this.lineChartData = {
      labels: dates,
      datasets: [
        {
          label: 'Daily Sales',
          data: totals,
          borderColor: this.colors.primary,
          backgroundColor: `rgba(74, 139, 111, 0.1)`,
          borderWidth: 3,
          fill: true,
          tension: 0.4,
          pointRadius: 5,
          pointBackgroundColor: this.colors.primary,
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointHoverRadius: 7
        }
      ]
    };
  }


  private initializePieChart(productData: ProductData[]): void {
    const labels = productData.map(p => p.name);
    const data = productData.map(p => p.value);

    const backgroundColors = [
      this.colors.primary,
      this.colors.secondary,
      this.colors.accent,
      this.colors.success,
      this.colors.warning,
      this.colors.danger,
      '#8884d8',
      '#82ca9d'
    ];

    this.pieChartData = {
      labels: labels,
      datasets: [
        {
          label: 'Product Distribution',
          data: data,
          backgroundColor: backgroundColors.slice(0, data.length),
          borderColor: '#fff',
          borderWidth: 2
        }
      ]
    };
  }


  private initializeBarChart(orderData: CategoryOrderData[]): void {
    const categories = orderData.map(o => o.category);
    const counts = orderData.map(o => o.count);

    this.barChartData = {
      labels: categories,
      datasets: [
        {
          label: 'Orders Count',
          data: counts,
          backgroundColor: this.colors.primary,
          borderColor: this.colors.primary,
          borderWidth: 1,
          borderRadius: 6
        }
      ]
    };
  }


  private formatDate(dateString: string): string {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    } catch {
      return dateString;
    }
  }

  formatNumber(num: number): string {
    return num.toLocaleString();
  }

  retryLoadData(): void {
    this.errorMessage = '';
    this.isLoadingKPI = true;
    this.isLoadingCharts = true;
    this.loadDashboardData();
  }
}
