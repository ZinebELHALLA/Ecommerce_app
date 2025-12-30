import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
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

export interface Order {
    id?: string;
    orderNumber: string;
    userId: number;
    status: string;
    totalAmount: number;
    deliveryAddress: string;
    paymentMethod: string;
    orderDate: string;
    items: OrderItem[];
}

export interface OrderItem {
    skuCode: string;
    productName?: string;
    quantity: number;
    price: number;
    totalPrice: number;
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
  
  getUserOrders(userId: number): Observable<Order[]> {
    // Get all orders and filter by userId on the frontend
    return this.http.get<Order[]>(`${this.orderServiceUrl}`).pipe(
      map(orders => orders.filter(order => order.userId === userId))
    );
  }
  
  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.orderServiceUrl}`);
  }
  
  getOrderDetails(orderNumber: string): Observable<Order> {
    return this.http.get<Order>(`${this.orderServiceUrl}/${orderNumber}`);
  }
}
