import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ProductResponseDTO {
    id: string;
    name: string;
    description: string;
    price: number;
    sku: string; // Matched with Backend
    imageUrl?: string;
    categoryId?: string;
    stock?: number; // Backend returns this!
}

export interface ProductRequestDTO {
    name: string;
    description: string;
    price: number;
    sku: string; // CORRECTED: Matches backend 'sku' field
    stock: number; // ADDED: Backend uses this for inventory
    categoryId: string;
    imageUrl?: string; // Optional but good to have
    active?: boolean;
}

// RESTORED INTERFACES
export interface Product {
    id: string;
    name: string;
    description: string;
    price: number;
    sku: string;
    imageUrl?: string;
    categoryId?: string;
    stock?: number | null; 
    active?: boolean; 
    inStock?: boolean; 
    checkingStock?: boolean;
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
  private categoryUrl = `${environment.apiUrl}/product-service/api/categories`;
  private inventoryServiceUrl = `${environment.apiUrl}/inventory-service/api/inventory`;

  constructor(private http: HttpClient) {}

  getAllProducts(): Observable<ProductResponseDTO[]> {
    return this.http.get<ProductResponseDTO[]>(this.productServiceUrl);
  }

  getProductById(id: string): Observable<ProductResponseDTO> {
    return this.http.get<ProductResponseDTO>(`${this.productServiceUrl}/id/${id}`);
  }

  createProduct(product: ProductRequestDTO): Observable<ProductResponseDTO> {
    return this.http.post<ProductResponseDTO>(this.productServiceUrl, product);
  }

  updateProduct(id: string, product: ProductRequestDTO): Observable<ProductResponseDTO> {
    return this.http.put<ProductResponseDTO>(`${this.productServiceUrl}/id/${id}`, product);
  }

  deleteProduct(id: string): Observable<void> {
    return this.http.delete<void>(`${this.productServiceUrl}/id/${id}`);
  }

  // RESTORED METHODS
  getProducts(): Observable<Product[]> {
    // Mapping DTO to legacy Product interface if needed, or just casting if backend returns same structure
    // Since backend returns ProductResponseDTO, we can cast or map. 
    // Assuming structure is compatible enough for now or user just needs the method signature to exist.
    return this.http.get<Product[]>(this.productServiceUrl);
  }

  getProduct(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.productServiceUrl}/id/${id}`);
  }

  getCategories(): Observable<Category[]> {
      return this.http.get<Category[]>(this.categoryUrl);
  }

  createCategory(category: { name: string, description: string }): Observable<Category> {
      return this.http.post<Category>(this.categoryUrl, category);
  }

  checkStock(skuCode: string): Observable<{ skuCode: string, inStock: boolean }> {
      return this.http.get<{ skuCode: string, inStock: boolean }>(`${this.inventoryServiceUrl}/${skuCode}`);
  }
}
