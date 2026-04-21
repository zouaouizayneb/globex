import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of, map } from 'rxjs';

export interface KPIStats {
  revenue: number;
  orders: number;
  clients: number;
}

export interface SalesPerDay {
  date: string;
  total: number;
}

export interface ProductStat {
  name: string;
  value: number;
}

export interface CategoryOrders {
  category: string;
  orders: number;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private base = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getKPIStats(): Observable<KPIStats> {
    return this.http.get<any>(`${this.base}/analytics/dashboard`).pipe(
      map(data => ({
        revenue: data.todayRevenue || 0,
        orders: data.todayOrders || 0,
        clients: data.totalClients || 0
      })),
      catchError(() => of({ revenue: 0, orders: 0, clients: 0 }))
    );
  }

  getSalesPerDay(): Observable<SalesPerDay[]> {
    return this.http.get<any>(`${this.base}/analytics/dashboard`).pipe(
      map(data => (data.recentSalesData || []).map((day: any) => ({
        date: day.date,
        total: day.totalRevenue || 0
      }))),
      catchError(() => of([]))
    );
  }

  getProductStats(): Observable<ProductStat[]> {
    return this.http.get<any>(`${this.base}/analytics/dashboard`).pipe(
      map(data => (data.topProductsThisMonth || []).map((p: any) => ({
        name: p.productName || 'Unknown',
        value: p.quantitySold || 0
      }))),
      catchError(() => of([]))
    );
  }

  getCategoryOrders(): Observable<CategoryOrders[]> {
    // Falls back to empty or we could compute it from top products if category was there
    return this.http.get<any>(`${this.base}/analytics/dashboard`).pipe(
      map(() => []), 
      catchError(() => of([]))
    );
  }
}
