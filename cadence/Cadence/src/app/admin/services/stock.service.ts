import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class StockService {

  private apiUrl = 'http://localhost:8080/api/stocks';

  constructor(private http: HttpClient) {}

  getAllStock() {
    return this.http.get<any[]>(this.apiUrl);
  }

  updateStock(id: number, quantity: number) {
    return this.http.put(`${this.apiUrl}/${id}`, { quantity });
  }
}