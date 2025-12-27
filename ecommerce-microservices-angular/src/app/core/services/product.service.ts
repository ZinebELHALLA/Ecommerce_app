import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Product {
    id: string;
    name: string;
    description: string;
    price: number;
    quantity?: number; // From inventory
    skuCode?: string; // Needed for inventory check
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
    // Note: Backend might have a bug with /id path, trying standard approach
    return this.http.get<Product>(`${this.productServiceUrl}/${id}`);
  }

  checkStock(skuCode: string): Observable<{ skuCode: string, inStock: boolean }> {
      return this.http.get<{ skuCode: string, inStock: boolean }>(`${this.inventoryServiceUrl}/${skuCode}`);
  }
}
