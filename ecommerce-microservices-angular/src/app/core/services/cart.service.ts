import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CartItem {
  id: string; // Used for UI identification/removal if needed, or mapped from backend ID
  skuCode: string;
  unitPrice: number; // Frontend uses unitPrice
  totalPrice: number;
  quantity: number;
  productName: string; 
}

export interface CartResponse {
  userId: number;
  cartItems: CartItem[];
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
    return this.http.get<CartResponse>(`${this.cartServiceUrl}/${this.userId}`);
  }

  addToCart(item: CartItem): Observable<CartResponse> {
    return this.http.post<CartResponse>(`${this.cartServiceUrl}/${this.userId}/items`, item);
  }

  updateItem(skuCode: string, quantity: number): Observable<CartResponse> {
      return this.http.put<CartResponse>(`${this.cartServiceUrl}/${this.userId}/items/${skuCode}`, { quantity });
  }

  removeItem(skuCode: string): Observable<CartResponse> {
    return this.http.delete<CartResponse>(`${this.cartServiceUrl}/${this.userId}/items/${skuCode}`);
  }

  clearCart(): Observable<void> {
    return this.http.delete<void>(`${this.cartServiceUrl}/${this.userId}`);
  }
  
  getUserId(): number {
      return this.userId;
  }
}
