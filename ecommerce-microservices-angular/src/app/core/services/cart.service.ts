import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';
import { InventoryService } from './inventory.service';

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

export interface CartValidationResponse {
    userId: number;
    items: any[];
    totalAmount: number;
    isValid: boolean;
    validationErrors: string[];
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartServiceUrl = `${environment.apiUrl}/cart-service/api/carts`;
  private userId: number = 0;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private inventoryService: InventoryService
  ) {
    // Get user ID from authenticated user instead of random ID
    this.updateUserId();
    
    // Listen for auth changes
    this.authService.currentUser$.subscribe(user => {
      this.updateUserId();
    });
  }

  private updateUserId(): void {
    const user = this.authService.getCurrentUser();
    this.userId = user?.id || 0;
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

  getValidatedCart(): Observable<CartValidationResponse> {
    return this.http.get<CartValidationResponse>(`${this.cartServiceUrl}/${this.userId}/validate`);
  }

  addToCart(item: CartItem): Observable<CartResponse> {
    // First check stock before adding to cart
    return this.inventoryService.checkStock(item.skuCode).pipe(
      switchMap(stockResponse => {
        if (!stockResponse || stockResponse.quantity < item.quantity) {
          throw new Error('Insufficient stock available');
        }
        
        // If stock is available, proceed with adding to cart
        return this.http.post<BackendCartResponse>(`${this.cartServiceUrl}/${this.userId}/items`, item).pipe(
          map(response => this.mapBackendResponse(response))
        );
      })
    );
  }
  
  addToCartWithoutValidation(item: CartItem): Observable<CartResponse> {
    // Direct add without stock validation (for internal use)
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
