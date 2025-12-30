import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { CartComponent } from './features/cart/cart.component';
import { InventoryComponent } from './features/inventory/inventory.component';
import { ManageProductsComponent } from './features/products/manage-products/manage-products.component';
import { ProductDetailComponent } from './features/products/product-detail/product-detail.component';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { 
    path: 'products', 
    component: ProductListComponent,
    canActivate: [AuthGuard]
  },
  { 
    path: 'products/:id', 
    component: ProductDetailComponent,
    canActivate: [AuthGuard]
  },
  { 
    path: 'cart', 
    component: CartComponent,
    canActivate: [AuthGuard]
  },
  { 
    path: 'manage-products', 
    component: ManageProductsComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { role: 'MANAGER' }
  },
  { 
    path: 'inventory', 
    component: InventoryComponent,
    canActivate: [AuthGuard, RoleGuard], // Manager only
    data: { role: 'MANAGER' }
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
