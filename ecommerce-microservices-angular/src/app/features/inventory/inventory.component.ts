import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService, ProductResponseDTO } from '../../core/services/product.service';
import { AuthService } from '../../core/services/auth.service';
import { forkJoin } from 'rxjs';

interface InventoryItem {
  id: string;
  name: string;
  skuCode: string;
  price: number;
  description: string;
  category: string; // Assuming category is derived or stored
  quantity: number;
  status: string;
}

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.css']
})
export class InventoryComponent implements OnInit {
  inventoryItems: InventoryItem[] = [];
  loading = true;
  error: string | null = null;
  categories: string[] = [];
  selectedCategory: string = 'All';

  constructor(
    private productService: ProductService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadInventory();
  }

  loadInventory(): void {
    this.loading = true;
    this.productService.getAllProducts().subscribe({
      next: (products: ProductResponseDTO[]) => {
        // OPTION B IMPLEMENTATION: Product Service is the Source of Truth
        // We strictly use the 'stock' field from ProductResponseDTO.
        // No separate InventoryService calls.
        
        this.inventoryItems = products.map(product => {
          const quantity = product.stock !== undefined ? product.stock : 0;
          
          let status = 'Out of Stock';
          if (quantity > 10) status = 'In Stock';
          else if (quantity > 0) status = 'Low Stock';

          return {
            id: product.id,
            name: product.name,
            skuCode: product.sku, 
            price: product.price,
            description: product.description,
            category: 'Uncategorized', 
            quantity: quantity,
            status: status
          };
        });
        
        this.extractCategories(products);
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error fetching products', err);
        this.error = 'Failed to load products.';
        this.loading = false;
      }
    });
  }


  extractCategories(products: ProductResponseDTO[]): void {
    // Placeholder if category exists
    this.categories = ['All', 'Uncategorized'];
  }

  filterByCategory(category: string): void {
    this.selectedCategory = category;
    // Real implementation would filter this.inventoryItems or a display list
  }
}
