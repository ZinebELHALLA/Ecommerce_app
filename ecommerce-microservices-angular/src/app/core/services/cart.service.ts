import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface CartItem {
  id: string; // Frontend ID - always set from skuCode
  skuCode: string;
  unitPrice: number; // Frontend uses unitPrice
  price?: number; // Backend uses price
  totalPrice: number;
  subtotal?: number; // Backend uses subtotal
  quantity: number;
  productName: string; 
}

export interface CartResponse {
  userId: number;
  cartItems: CartItem[]; // Frontend uses cartItems
  totalPrice: number;
}

// Backend response format
interface BackendCartResponse {
  userId: number;
  items: CartItem[]; // Backend uses items
  totalPrice: number;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartServiceUrl = `${environment.apiUrl}/cart-service/api/carts`;
  private userId: number;

  constructor(private http: HttpClient) {
    // Generate a random user ID for anonymous session if not exists
    const storedId = localStorage.getItem('anonymous_user_id');
    if (storedId) {
      this.userId = parseInt(storedId, 10);
    } else {
      this.userId = Math.floor(Math.random() * 1000000);
      localStorage.setItem('anonymous_user_id', this.userId.toString());
    }
  }

  getCart(): Observable<CartResponse> {
    return this.http.get<BackendCartResponse>(`${this.cartServiceUrl}/${this.userId}`).pipe(
      map(response => ({
        userId: response.userId,
        cartItems: response.items.map(item => ({
          id: item.skuCode, // Use skuCode as ID for frontend
          skuCode: item.skuCode,
          unitPrice: item.price || 0,
          totalPrice: item.subtotal || 0,
          quantity: item.quantity,
          productName: item.skuCode // Use SKU as name for now
        })),
        totalPrice: response.totalPrice
      }))
    );
  }

  addToCart(item: CartItem): Observable<CartResponse> {
    return this.http.post<BackendCartResponse>(`${this.cartServiceUrl}/${this.userId}/items`, item).pipe(
      map(response => this.mapBackendResponse(response))
    );
  }

  updateItem(skuCode: string, quantity: number): Observable<CartResponse> {
    return this.http.put<BackendCartResponse>(`${this.cartServiceUrl}/${this.userId}/items/${skuCode}`, { quantity }).pipe(
      map(response => this.mapBackendResponse(response))
    );
  }

  removeItem(skuCode: string): Observable<CartResponse> {
    return this.http.delete<BackendCartResponse>(`${this.cartServiceUrl}/${this.userId}/items/${skuCode}`).pipe(
      map(response => this.mapBackendResponse(response))
    );
  }

  clearCart(): Observable<void> {
    return this.http.delete<void>(`${this.cartServiceUrl}/${this.userId}`);
  }
  
  getUserId(): number {
    return this.userId;
  }

  private mapBackendResponse(response: BackendCartResponse): CartResponse {
    return {
      userId: response.userId,
      cartItems: response.items.map(item => ({
        id: item.skuCode,
        skuCode: item.skuCode,
        unitPrice: item.price || 0,
        totalPrice: item.subtotal || 0,
        quantity: item.quantity,
        productName: item.skuCode
      })),
      totalPrice: response.totalPrice
    };
  }
}
