import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { ProductsComponent } from './pages/products/products.component';
import { OrdersComponent } from './pages/orders/orders.component';
import { ClientsComponent } from './pages/clients/clients.component';
import { CategoriesComponent } from './pages/categories/categories.component';
import { StockManagementComponent } from './pages/stock-management/stock-management.component';
import { ShipmentsComponent } from './pages/shipments/shipments.component';
import { TransporteursComponent } from './pages/transporteurs/transporteurs.component';
import { SuppliersComponent } from './pages/suppliers/suppliers.component';
import { InvoicesComponent } from './pages/invoices/invoices.component';

export const adminRoutes: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'products', component: ProductsComponent },
      { path: 'orders', component: OrdersComponent },
      { path: 'invoices', component: InvoicesComponent },
      { path: 'shipments', component: ShipmentsComponent },
      { path: 'clients', component: ClientsComponent },
      { path: 'categories', component: CategoriesComponent },
      { path: 'stock-management', component: StockManagementComponent },
      { path: 'transporteurs', component: TransporteursComponent },
      { path: 'suppliers', component: SuppliersComponent }
    ]
  }
];
