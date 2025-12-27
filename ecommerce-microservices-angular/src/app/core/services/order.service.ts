import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CartItem } from './cart.service';

export interface OrderRequest {
    userId: number;
    orderLineItemsDtoList: CartItem[]; // Assuming backend expects similar structure or mapped
}

export interface OrderResponse {
    orderNumber: string;
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
}
