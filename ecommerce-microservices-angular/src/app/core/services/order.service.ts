import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CartItem } from './cart.service';

export interface OrderRequest {
    userId: number;
    orderLineItemsDtoList: CartItem[]; // Legacy support if needed
}

export interface OrderResponse {
    orderNumber: string;
    status?: string;
}

export interface CheckoutRequest {
    userId: number;
    deliveryAddress: string;
    paymentMethod: string;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private orderServiceUrl = `${environment.apiUrl}/order-service/api/orders`;

  constructor(private http: HttpClient) {}

  placeOrder(orderRequest: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(this.orderServiceUrl, orderRequest);
  }

  checkout(checkoutRequest: CheckoutRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.orderServiceUrl}/checkout`, checkoutRequest);
  }
}
