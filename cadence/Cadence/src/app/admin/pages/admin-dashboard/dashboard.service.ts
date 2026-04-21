import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface KPIData {
  revenue: number;
  orders: number;
  clients: number;
}

export interface SalesDataPoint {
  date: string;
  total: number;
}

export interface ProductData {
  name: string;
  value: number;
}

export interface CategoryOrderData {
  category: string;
  count: number;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = 'http://localhost:8080/api/stats'; 
  constructor(private http: HttpClient) { }

  
  getKPIData(): Observable<KPIData> {
    return this.http.get<KPIData>(`${this.apiUrl}/kpi`);
  }

 
  getSalesPerDay(): Observable<SalesDataPoint[]> {
    return this.http.get<SalesDataPoint[]>(`${this.apiUrl}/sales-per-day`);
  }

 
  getProductDistribution(): Observable<ProductData[]> {
    return this.http.get<ProductData[]>(`${this.apiUrl}/products`);
  }

 
  getOrdersByCategory(): Observable<CategoryOrderData[]> {
    return this.http.get<CategoryOrderData[]>(`${this.apiUrl}/orders-by-category`);
  }

 
  getAllDashboardData() {
    return {
      kpi$: this.getKPIData(),
      salesPerDay$: this.getSalesPerDay(),
      productDistribution$: this.getProductDistribution(),
      ordersByCategory$: this.getOrdersByCategory()
    };
  }
}
