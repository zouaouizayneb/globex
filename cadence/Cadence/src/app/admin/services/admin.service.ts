import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map, catchError, of } from 'rxjs';

export interface AdminProduct {
  id: number;
  name: string;
  description?: string;
  sku: string;
  price: number;
  stock: number;
  category: string;
  categoryId?: number;
  supplierId?: number;
  status: 'active' | 'inactive' | 'draft';
  imageUrl?: string;
  rating?: number;
}

export interface AdminClient {
  id: number;
  name: string;
  email: string;
  phone: string;
  country: string;
  status: 'active' | 'inactive';
  totalOrders: number;
  totalSpent: number;
  joinDate: string;
}

export interface AdminTransporter {
  id: number;
  name: string;
  email: string;
  phone: string;
  address: string;
  status: 'active' | 'inactive';
  deliveryFee: number;
  createdAt: string;
}

export interface AdminSupplier {
  id: number;
  name: string;
  email: string;
  phone: string;
  address: string;
  status: 'active' | 'inactive';
  createdAt: string;
}

export interface AdminOrder {
  id: string;
  customer: string;
  date: string;
  total: number;
  status: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'REFUNDED';
  items: number;
}

export interface AdminCategory {
  id: number;
  name: string;
  description: string;
  productCount: number;
  status: 'active' | 'inactive';
}

export interface DashboardStats {
  totalOrders: number;
  totalProducts: number;
  totalClients: number;
  totalCategories: number;
  yearOrders: number;
  yearRevenue: number;
  monthOrders: number;
  monthRevenue: number;
  todayOrders: number;
  todayRevenue: number;
  outOfStockProducts: number;
  lowStockProducts: number;
  ordersByStatus: { [key: string]: number };
  topCategories: any[];
  recentSalesData: any[];
  currency: string;
}

export interface StockAlert {
  product: string;
  current: number;
  minimum: number;
  productId?: number;
}

export interface StockItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  status: 'in-stock' | 'low-stock' | 'out-of-stock';
  imageUrl?: string;
}

export interface Shipment {
  idShip: number;
  carrier?: string;
  transporterId?: number;
  trackingNumber?: string;
  status: 'PENDING' | 'PROCESSING' | 'SHIPPED' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED' | 'RETURNED';
  dateShip?: string;
  orderId?: number;
  shippingAddressId?: number;
  shippingMethod: 'STANDARD' | 'EXPRESS' | 'OVERNIGHT' | 'INTERNATIONAL';
  shippingCost?: number;
  estimatedDeliveryDate?: string;
  deliveredAt?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private base = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // ── DASHBOARD ──────────────────────────────────────────────
  getDashboardData(): Observable<DashboardStats> {
    return this.http.get<any>(`${this.base}/analytics/dashboard`).pipe(
      map(data => ({
        totalOrders: data.yearOrders ?? 0,
        totalProducts: data.totalProducts ?? 0,
        totalClients: data.totalClients ?? 0,
        totalCategories: data.totalCategories ?? 0,
        yearOrders: data.yearOrders ?? 0,
        yearRevenue: data.yearRevenue ?? 0,
        monthOrders: data.monthOrders ?? 0,
        monthRevenue: data.monthRevenue ?? 0,
        todayOrders: data.todayOrders ?? 0,
        todayRevenue: data.todayRevenue ?? 0,
        outOfStockProducts: data.outOfStockProducts ?? 0,
        lowStockProducts: data.lowStockProducts ?? 0,
        ordersByStatus: data.ordersByStatus ?? {},
        topCategories: data.topCategories ?? [],
        recentSalesData: data.recentSalesData ?? [],
        currency: data.currency ?? 'TND',
      })),
      catchError(() => of({ 
        totalOrders: 0, totalProducts: 0, totalClients: 0, totalCategories: 0, 
        yearOrders: 0, yearRevenue: 0, monthOrders: 0, monthRevenue: 0, 
        todayOrders: 0, todayRevenue: 0, outOfStockProducts: 0, lowStockProducts: 0,
        ordersByStatus: {}, topCategories: [], recentSalesData: [], currency: 'TND'
      }))
    );
  }

  getFullDashboard(): Observable<{ stats: DashboardStats; orders: AdminOrder[]; alerts: StockAlert[]; analytics: any }> {
    return forkJoin({
      analytics: this.http.get<any>(`${this.base}/analytics/dashboard`).pipe(catchError(() => of(null))),
      products:  this.http.get<any[]>(`${this.base}/products`).pipe(catchError(() => of([]))),
      clients:   this.http.get<any[]>(`${this.base}/clients`).pipe(catchError(() => of([]))),
      orders:    this.http.get<any[]>(`${this.base}/orders/all`).pipe(catchError(() => of([]))),
      inventory: this.http.get<any[]>(`${this.base}/inventory/alerts/low-stock`).pipe(catchError(() => of([]))),
    }).pipe(
      map(({ analytics, products, clients, orders, inventory }) => {
        const stats: DashboardStats = {
          totalOrders: Number(analytics?.yearOrders ?? (orders?.length || 0)),
          totalProducts: Number(analytics?.totalProducts ?? (products?.length || 0)),
          totalClients: Number(analytics?.totalClients ?? (clients?.length || 0)),
          totalCategories: Number(analytics?.totalCategories ?? 0),
          yearOrders: Number(analytics?.yearOrders ?? 0),
          yearRevenue: Number(analytics?.yearRevenue ?? 0),
          monthOrders: Number(analytics?.monthOrders ?? 0),
          monthRevenue: Number(analytics?.monthRevenue ?? 0),
          todayOrders: Number(analytics?.todayOrders ?? 0),
          todayRevenue: Number(analytics?.todayRevenue ?? 0),
          outOfStockProducts: Number(analytics?.outOfStockProducts ?? 0),
          lowStockProducts: Number(analytics?.lowStockProducts ?? 0),
          ordersByStatus: analytics?.ordersByStatus ?? {},
          topCategories: analytics?.topCategories ?? [],
          recentSalesData: analytics?.recentSalesData ?? [],
          currency: analytics?.currency ?? 'TND',
        };

        const mappedOrders: AdminOrder[] = (orders || []).slice(0, 5).map((o: any) => this.mapOrder(o));

        const alerts: StockAlert[] = (inventory ?? []).map((item: any) => ({
          product: item.productName ?? item.name ?? 'Unknown',
          current: item.currentStock ?? item.quantity ?? 0,
          minimum: item.threshold ?? item.minimumStock ?? 10,
          productId: item.variantId ?? item.productId,
        }));

        return { stats, orders: mappedOrders, alerts, analytics };
      })
    );
  }

  // ── PRODUCTS ───────────────────────────────────────────────
  getProducts(): Observable<AdminProduct[]> {
    return this.http.get<any[]>(`${this.base}/products`).pipe(
      map(products => products.map(p => this.mapProduct(p))),
      catchError(() => of([]))
    );
  }

  createProduct(product: any): Observable<any> {
    return this.http.post(`${this.base}/products`, product);
  }

  updateProduct(id: number, product: any): Observable<any> {
    return this.http.put(`${this.base}/products/${id}`, product);
  }

  deleteProduct(id: number): Observable<any> {
    return this.http.delete(`${this.base}/products/${id}`);
  }

  getProductsByCategory(categoryId: number): Observable<AdminProduct[]> {
    return this.http.get<any[]>(`${this.base}/products/category/${categoryId}`).pipe(
      map(products => products.map(p => this.mapProduct(p))),
      catchError(() => of([]))
    );
  }

  private mapProduct(p: any): AdminProduct {
    const totalStock = (p.stock ?? 0) + (p.variants ?? []).reduce((sum: number, v: any) => sum + (v.stockQuantity ?? 0), 0);
    const primaryImage = (p.images ?? []).find((img: any) => img.isPrimary) ?? p.images?.[0];
    return {
      id: p.idProduct ?? p.id,
      name: p.name ?? '',
      description: p.description ?? '',
      sku: p.variants?.[0]?.sku ?? `PROD-${p.idProduct ?? p.id}`,
      price: parseFloat(p.price) || p.variants?.[0]?.totalPrice || 0,
      stock: totalStock,
      category: p.category?.name ?? 'Uncategorized',
      categoryId: p.category?.idCategory ?? p.category?.id,
      status: totalStock > 0 ? 'active' : 'inactive',
      imageUrl: primaryImage?.imageUrl ?? null,
      rating: p.rating,
    };
  }

  // ── CLIENTS ────────────────────────────────────────────────
  getClients(): Observable<AdminClient[]> {
    return forkJoin({
      clients: this.http.get<any[]>(`${this.base}/clients`).pipe(catchError(() => of([]))),
      orders: this.http.get<any[]>(`${this.base}/orders/all`).pipe(catchError(() => of([])))
    }).pipe(
      map(({ clients, orders }) => {
        console.log('Clients orders from API:', orders);
        return clients.map(c => this.mapClient(c, orders));
      })
    );
  }

  deleteClient(id: number): Observable<any> {
    return this.http.delete(`${this.base}/clients/${id}`);
  }

  createClient(client: any): Observable<any> {
    return this.http.post(`${this.base}/clients`, client);
  }

  updateClient(id: number, client: any): Observable<any> {
    return this.http.put(`${this.base}/clients/${id}`, client);
  }

  // ── TRANSPORTEURS ───────────────────────────────────────────
  getTransporters(): Observable<AdminTransporter[]> {
    return this.http.get<any[]>(`${this.base}/transporteurs`).pipe(
      map(transporteurs => transporteurs.map(t => this.mapTransporter(t))),
      catchError(() => of([]))
    );
  }

  deleteTransporter(id: number): Observable<any> {
    return this.http.delete(`${this.base}/transporteurs/${id}`);
  }

  createTransporter(transporter: any): Observable<any> {
    return this.http.post(`${this.base}/transporteurs`, transporter);
  }

  updateTransporter(id: number, transporter: any): Observable<any> {
    return this.http.put(`${this.base}/transporteurs/${id}`, transporter);
  }

  private mapTransporter(t: any): AdminTransporter {
    return {
      id: t.idTransporteur ?? t.id_transporteur ?? t.id,
      name: t.name ?? t.companyName ?? 'Unknown',
      email: t.email ?? '',
      phone: t.phone ?? t.phoneNumber ?? '',
      address: t.address ?? '',
      status: t.status ?? 'active',
      deliveryFee: t.deliveryFee ?? 0,
      createdAt: t.createdAt ?? ''
    };
  }

  // ── SUPPLIERS ─────────────────────────────────────────────
  getSuppliers(): Observable<AdminSupplier[]> {
    return this.http.get<any[]>(`${this.base}/suppliers`).pipe(
      map(suppliers => suppliers.map(s => this.mapSupplier(s))),
      catchError(() => of([]))
    );
  }

  deleteSupplier(id: number): Observable<any> {
    return this.http.delete(`${this.base}/suppliers/${id}`);
  }

  createSupplier(supplier: any): Observable<any> {
    return this.http.post(`${this.base}/suppliers`, supplier);
  }

  updateSupplier(id: number, supplier: any): Observable<any> {
    return this.http.put(`${this.base}/suppliers/${id}`, supplier);
  }

  private mapSupplier(s: any): AdminSupplier {
    return {
      id: s.idSupplier ?? s.id,
      name: s.name ?? s.companyName ?? 'Unknown',
      email: s.email ?? '',
      phone: s.phone ?? s.phoneNumber ?? '',
      address: s.address ?? '',
      status: s.status ?? 'active',
      createdAt: s.createdAt ?? ''
    };
  }

  private mapClient(c: any, allOrders: any[]): AdminClient {
    const user = c.user ?? {};
    const clientId = c.idClient ?? c.id;
    const userId = user.idUser ?? user.id;

    const clientOrders = allOrders.filter(o => 
      (o.client?.idClient === clientId || o.client?.id === clientId) || 
      (o.user?.idUser === userId || o.user?.id === userId)
    );
    
    const totalSpent = clientOrders.reduce((sum: number, o: any) => sum + (parseFloat(o.totalAmount) || 0), 0);

    return {
      id: clientId,
      name: user.fullname ?? user.username ?? c.fullname ?? c.name ?? 'Unknown',
      email: user.email ?? c.email ?? '',
      phone: user.phoneNumber ?? user.phone ?? c.phone ?? c.phoneNumber ?? '',
      country: c.country ?? user.country ?? '',
      status: (user.isActive !== false && c.status !== 'inactive') ? 'active' : 'inactive',
      totalOrders: clientOrders.length,
      totalSpent,
      joinDate: user.createdAt ?? c.createdAt ?? '',
    };
  }

  // ── ORDERS ─────────────────────────────────────────────────
  getAllOrders(): Observable<AdminOrder[]> {
    return this.http.get<any[]>(`${this.base}/orders/all`).pipe(
      map(orders => {
        console.log('Orders from API:', orders);
        return orders.map(o => this.mapOrder(o));
      }),
      catchError((error) => {
        console.error('Error fetching orders:', error);
        return of([]);
      })
    );
  }

  private mapOrder(o: any): AdminOrder {
    const customer = o.user?.fullname ?? o.user?.username ?? o.client?.name ?? `Client #${o.client_id ?? o.user_id}`;
    const mapped = {
      id: `#ORD-${String(o.id_order ?? o.idOrder ?? o.id).padStart(3, '0')}`,
      customer,
      date: o.date_order ?? o.dateOrder ?? o.date ?? '',
      total: parseFloat(o.total_amount ?? o.totalAmount ?? 0),
      status: o.status ?? 'PENDING',
      items: o.orderDetails?.length ?? o.items ?? 0,
    };
    console.log('Mapped order:', mapped);
    return mapped;
  }

  // ── CATEGORIES ─────────────────────────────────────────────
  getCategories(): Observable<AdminCategory[]> {
    return forkJoin({
      cats: this.http.get<any[]>(`${this.base}/categories`).pipe(catchError(() => of([]))),
      prods: this.http.get<any[]>(`${this.base}/products`).pipe(catchError(() => of([])))
    }).pipe(
      map(({ cats, prods }) => {
        return cats.map(c => {
          const categoryId = c.idCategory ?? c.id;
          const matchingProducts = prods.filter(p => (p.category?.idCategory ?? p.category?.id) === categoryId);
          return {
            id: categoryId,
            name: c.name ?? '',
            description: c.description ?? '',
            productCount: matchingProducts.length,
            status: 'active',
          };
        });
      })
    );
  }

  createCategory(data: { name: string; description: string }): Observable<any> {
    return this.http.post(`${this.base}/categories`, data);
  }

  updateCategory(id: number, data: { name: string; description: string }): Observable<any> {
    return this.http.put(`${this.base}/categories/${id}`, data);
  }

  deleteCategory(id: number): Observable<any> {
    return this.http.delete(`${this.base}/categories/${id}`);
  }

  // ── STOCK ───────────────────────────────────────────────────
  getStock(): Observable<StockItem[]> {
    return forkJoin({
      stocks: this.http.get<any[]>(`${this.base}/stocks`).pipe(catchError(() => of([]))),
      products: this.http.get<any[]>(`${this.base}/products`).pipe(catchError(() => of([])))
    }).pipe(
      map(({ stocks, products }) => {
        console.log('Stocks from API:', stocks);
        console.log('Products from API:', products);
        
        return stocks.map((s, index) => {
          let product = null;
          let variant = s.variant;
          
          if (variant) {
             product = products.find(p => p.variants?.some((v: any) => v.idVariant === variant.idVariant));
          } else if (s.variant_id) {
             product = products.find(p => p.variants?.some((v: any) => v.idVariant === s.variant_id));
             if (product) variant = product.variants.find((v: any) => v.idVariant === s.variant_id);
          }
          
          if (!product && s.product) {
            product = products.find(p => p.idProduct === s.product?.idProduct || p.id === s.product?.id);
          }
          if (!product && products[index]) {
            product = products[index];
          }
          
          const quantity = s.quantity || 0;
          let status: 'in-stock' | 'low-stock' | 'out-of-stock' = 'in-stock';
          if (quantity === 0) status = 'out-of-stock';
          else if (quantity <= 10) status = 'low-stock';

          const primaryImage = (product?.images ?? []).find((img: any) => img.isPrimary) ?? product?.images?.[0];
          const finalImage = variant?.imageUrl || primaryImage?.imageUrl || null;
          
          let productName = product?.name || `Product #${index + 1}`;
          if (variant && variant.sku) {
             productName += ` (${variant.sku})`;
          }

          const stockItem: StockItem = {
            id: s.idStock || index,
            productId: product?.idProduct || product?.id || 0,
            productName: productName,
            quantity,
            status,
            imageUrl: finalImage
          };
          
          console.log('Mapped stock item:', stockItem);
          return stockItem;
        });
      })
    );
  }

  updateStockQuantity(id: number, quantity: number): Observable<any> {
    return this.http.put(`${this.base}/stocks/${id}`, { quantity });
  }

  getLowStockAlerts(threshold: number = 10): Observable<StockAlert[]> {
    return forkJoin({
      stocks: this.http.get<any[]>(`${this.base}/stocks`).pipe(catchError(() => of([]))),
      products: this.http.get<any[]>(`${this.base}/products`).pipe(catchError(() => of([])))
    }).pipe(
      map(({ stocks, products }: { stocks: any[], products: any[] }) => {
        return stocks
          .filter(s => s.quantity <= threshold)
          .map(s => {
            let product: any = null;
            let variant: any = s.variant;
            
            if (variant) {
               product = products.find(p => p.variants?.some((v: any) => v.idVariant === variant.idVariant));
            } else if (s.variant_id) {
               product = products.find(p => p.variants?.some((v: any) => v.idVariant === s.variant_id));
               if (product) variant = product.variants.find((v: any) => v.idVariant === s.variant_id);
            }
            
            if (!product && s.product) {
              product = products.find(p => p.idProduct === s.product?.idProduct || p.id === s.product?.id);
            }
            if (!product) {
              const index = stocks.indexOf(s);
              product = products[index];
            }
            
            let productName = product?.name || `Product #${stocks.indexOf(s) + 1}`;
            if (variant && variant.sku) {
               productName += ` (${variant.sku})`;
            }
            
            return {
              product: productName,
              current: s.quantity,
              minimum: threshold,
              productId: product?.idProduct || product?.id || 0
            };
          });
      }),
      catchError(() => of([]))
    );
  }

  // ── SHIPMENTS ───────────────────────────────────────────────
  getShipments(): Observable<Shipment[]> {
    return this.http.get<any[]>(`${this.base}/shipments`).pipe(
      map(shipments => shipments.map(s => this.mapShipment(s))),
      catchError((error) => {
        console.error('Error fetching shipments:', error);
        return of([]);
      })
    );
  }

  updateShipment(id: number, shipment: Shipment): Observable<any> {
    return this.http.put(`${this.base}/shipments/${id}`, shipment);
  }

  private mapShipment(s: any): Shipment {
    return {
      idShip: s.idShip ?? s.id_ship ?? s.id,
      carrier: s.carrier,
      trackingNumber: s.trackingNumber ?? s.tracking_number,
      status: s.status ?? 'PENDING',
      dateShip: s.dateShip ?? s.date_ship ?? s.date,
      orderId: s.order?.idOrder ?? s.orderId ?? s.order_id,
      shippingAddressId: s.shippingAddress?.id ?? s.shippingAddressId ?? s.shipping_address_id,
      shippingMethod: s.shippingMethod ?? s.shipping_method ?? 'STANDARD',
      shippingCost: s.shippingCost ?? s.shipping_cost ?? 0,
      estimatedDeliveryDate: s.estimatedDeliveryDate ?? s.estimated_delivery_date,
      deliveredAt: s.deliveredAt ?? s.delivered_at,
    };
  }
}
