import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface InventoryResponse {
  skuCode: string;
  quantity: number;
}

export interface InventoryValidationResponse {
  skuCode: string;
  inStock: boolean;
  availableQuantity: number;
  requestedQuantity: number;
  errorMessage: string;
}

export interface BatchInventoryRequest {
  skuCode: string;
  requiredQuantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private inventoryUrl = `${environment.apiUrl}/inventory-service/api/inventory`;

  constructor(private http: HttpClient) { }

  checkStock(skuCode: string): Observable<InventoryResponse> {
    return this.http.get<InventoryResponse>(`${this.inventoryUrl}/${skuCode}`);
  }

  checkBatchStock(skuCodes: string[]): Observable<InventoryValidationResponse[]> {
    const requests: BatchInventoryRequest[] = skuCodes.map(sku => ({
      skuCode: sku,
      requiredQuantity: 1
    }));
    return this.http.post<InventoryValidationResponse[]>(`${this.inventoryUrl}/check-batch`, requests);
  }

  deductStock(skuCodes: string[]): Observable<void> {
      // Not typically called from frontend directly, but good to have if needed for manual testing
      const requests: BatchInventoryRequest[] = skuCodes.map(sku => ({
          skuCode: sku,
          requiredQuantity: 1
      }));
      return this.http.post<void>(`${this.inventoryUrl}/deduct`, requests);
  }
}
