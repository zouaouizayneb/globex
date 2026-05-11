import { Subject } from 'rxjs';
import { takeUntil, forkJoin } from 'rxjs';

import { DashboardService, KPIData, SalesDataPoint, ProductData, CategoryOrderData } from './dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './dashboard-professional.component.html',
  styleUrls: ['./dashboard-professional.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  kpiData: KPIData = { revenue: 0, orders: 0, clients: 0 };

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
          // Chart logic removed to prevent conflicts
          this.isLoadingCharts = false;
        },
        error: (err) => {
          console.error('Error loading chart data:', err);
          this.errorMessage = 'Failed to load chart data';
          this.isLoadingCharts = false;
        }
      });
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
