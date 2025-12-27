import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Product {
    id: string;
    name: string;
    description: string;
    price: number;
    sku: string; // Matches backend DTO
    imageUrl?: string;
    categoryId?: string;
    stock?: number; // Backend DTO has this
    inStock?: boolean; // From inventory check
    checkingStock?: boolean; // UI state
}

export interface Category {
    id: string;
    name: string;
    description?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private productServiceUrl = `${environment.apiUrl}/product-service/api/products`;
  private inventoryServiceUrl = `${environment.apiUrl}/inventory-service/api/inventory`;

  constructor(private http: HttpClient) {}

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.productServiceUrl);
  }

  getProduct(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.productServiceUrl}/${id}`);
  }

  checkStock(skuCode: string): Observable<{ skuCode: string, inStock: boolean }> {
      return this.http.get<{ skuCode: string, inStock: boolean }>(`${this.inventoryServiceUrl}/${skuCode}`);
  }

  createProduct(product: any): Observable<Product> {
    return this.http.post<Product>(this.productServiceUrl, product);
  }

  getCategories(): Observable<Category[]> {
      return this.http.get<Category[]>(`${environment.apiUrl}/product-service/api/categories`);
  }

  createCategory(category: { name: string, description: string }): Observable<Category> {
      return this.http.post<Category>(`${environment.apiUrl}/product-service/api/categories`, category);
  }
}
