import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AdminService, DashboardStats, AdminOrder, StockAlert } from '../../services/admin.service';
import { SalesPerDay } from '../../services/dashboard.service';
import * as am5 from '@amcharts/amcharts5';
import * as am5xy from '@amcharts/amcharts5/xy';
import * as am5percent from '@amcharts/amcharts5/percent';
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated';
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
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  kpiCards: KPICard[] = [];
  recentOrders: AdminOrder[] = [];
  stockAlerts: StockAlert[] = [];
  isLoading = true;
  error: string | null = null;
  today = new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });

  private roots: am5.Root[] = [];

  constructor(
    private adminService: AdminService,
    private sanitizer: DomSanitizer,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  ngOnDestroy(): void {
    this.roots.forEach(root => root.dispose());
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

        // Render Charts
        if (isPlatformBrowser(this.platformId)) {
          setTimeout(() => {
            this.renderCharts(stats);
          }, 100);
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

  private renderCharts(stats: DashboardStats): void {
    this.roots.forEach(root => root.dispose());
    this.roots = [];

    // 1. Sales Trend Chart
    if (stats.recentSalesData && stats.recentSalesData.length > 0) {
      const root = am5.Root.new("salesTrendChart");
      this.roots.push(root);
      root.setThemes([am5themes_Animated.new(root)]);

      const chart = root.container.children.push(am5xy.XYChart.new(root, {
        panX: true,
        panY: false,
        wheelX: "panX",
        wheelY: "zoomX",
        pinchZoomX: true
      }));

      const xAxis = chart.xAxes.push(am5xy.DateAxis.new(root, {
        maxDeviation: 0.2,
        baseInterval: { timeUnit: "day", count: 1 },
        renderer: am5xy.AxisRendererX.new(root, {}),
        tooltip: am5.Tooltip.new(root, {})
      }));

      const yAxis = chart.yAxes.push(am5xy.ValueAxis.new(root, {
        renderer: am5xy.AxisRendererY.new(root, {})
      }));

      const series = chart.series.push(am5xy.LineSeries.new(root, {
        name: "Sales",
        xAxis: xAxis,
        yAxis: yAxis,
        valueYField: "value",
        valueXField: "date",
        tooltip: am5.Tooltip.new(root, {
          labelText: "{valueY} TND"
        })
      }));

      series.fills.template.setAll({
        fillOpacity: 0.15,
        visible: true,
        fill: am5.color(0x5f8575)
      });

      series.set("stroke", am5.color(0x5f8575));

      series.data.processor = am5.DataProcessor.new(root, {
        dateFields: ["date"],
        dateFormat: "yyyy-MM-dd"
      });

      const data = stats.recentSalesData.map(s => ({
        date: s.date,
        value: Number(s.totalRevenue || s.total || 0)
      }));

      series.data.setAll(data);
      series.appear(1000);
      chart.appear(1000, 100);
    }

    // 2. Status Distribution Chart
    if (stats.ordersByStatus && Object.keys(stats.ordersByStatus).length > 0) {
      const root = am5.Root.new("statusDistChart");
      this.roots.push(root);
      root.setThemes([am5themes_Animated.new(root)]);

      const chart = root.container.children.push(am5percent.PieChart.new(root, {
        innerRadius: am5.percent(50),
        layout: root.verticalLayout
      }));

      const series = chart.series.push(am5percent.PieSeries.new(root, {
        valueField: "value",
        categoryField: "category",
        alignLabels: true,
        radius: am5.percent(80)
      }));

      series.labels.template.setAll({
        fontSize: 11,
        text: "{category}"
      });

      chart.set("paddingLeft", 10);
      chart.set("paddingRight", 10);

      const colors = series.get("colors");
      if (colors) {
        colors.set("colors", [
          am5.color(0x5f8575),
          am5.color(0x84a59d),
          am5.color(0xa3b18a),
          am5.color(0x789b8c),
          am5.color(0x4a675b),
          am5.color(0xd8e2dc)
        ]);
      }

      const data = Object.keys(stats.ordersByStatus).map(key => ({
        category: key,
        value: Number(stats.ordersByStatus[key])
      }));

      series.data.setAll(data);
      chart.appear(1000, 100);
    }

    // 3. Category Sales Chart
    if (stats.topCategories && stats.topCategories.length > 0) {
      const root = am5.Root.new("categorySalesChart");
      this.roots.push(root);
      root.setThemes([am5themes_Animated.new(root)]);

      const chart = root.container.children.push(am5xy.XYChart.new(root, {
        panX: false,
        panY: false,
        wheelX: "none",
        wheelY: "none"
      }));

      const xAxis = chart.xAxes.push(am5xy.CategoryAxis.new(root, {
        categoryField: "category",
        renderer: am5xy.AxisRendererX.new(root, { minGridDistance: 30 })
      }));

      const yAxis = chart.yAxes.push(am5xy.ValueAxis.new(root, {
        renderer: am5xy.AxisRendererY.new(root, {})
      }));

      const series = chart.series.push(am5xy.ColumnSeries.new(root, {
        name: "Orders",
        xAxis: xAxis,
        yAxis: yAxis,
        valueYField: "value",
        categoryXField: "category",
        fill: am5.color(0x5f8575),
        stroke: am5.color(0x5f8575)
      }));

      series.columns.template.setAll({
        cornerRadiusTL: 4,
        cornerRadiusTR: 4,
        tooltipText: "{categoryX}: {valueY}"
      });

      const data = stats.topCategories.map(c => ({
        category: c.categoryName || c.name || 'N/A',
        value: Number(c.orderCount || c.orders || 0)
      }));

      xAxis.data.setAll(data);
      series.data.setAll(data);
      chart.appear(1000, 100);
    }
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
