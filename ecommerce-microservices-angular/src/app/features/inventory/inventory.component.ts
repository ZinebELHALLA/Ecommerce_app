import { Component, OnInit } from '@angular/core';
import { ProductService, ProductResponseDTO } from '../../core/services/product.service';
import { InventoryService, AddStockRequest } from '../../core/services/inventory.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
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
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.css']
})
export class InventoryComponent implements OnInit {
  inventoryItems: InventoryItem[] = [];
  loading = true;
  error: string | null = null;
  categories: string[] = [];
  selectedCategory: string = 'All';
  
  // Add Stock Form
  showAddStockForm = false;
  addStockForm = {
    skuCode: '',
    quantity: 1
  };
  addStockMessage = '';
  addStockError = '';

  constructor(
    private productService: ProductService,
    private inventoryService: InventoryService,
    private toastService: ToastService,
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
  
  toggleAddStockForm(): void {
    this.showAddStockForm = !this.showAddStockForm;
    if (!this.showAddStockForm) {
      // Reset form when closing
      this.addStockForm = { skuCode: '', quantity: 1 };
      this.addStockMessage = '';
      this.addStockError = '';
    }
  }
  
  addStock(): void {
    if (!this.addStockForm.skuCode || !this.addStockForm.quantity) {
      this.addStockError = 'Please fill in all fields.';
      return;
    }
    
    const request: AddStockRequest = {
      skuCode: this.addStockForm.skuCode,
      quantity: this.addStockForm.quantity
    };
    
    this.inventoryService.addStock(request).subscribe({
      next: (response) => {
        this.addStockMessage = `Successfully added ${request.quantity} units to ${request.skuCode}`;
        this.addStockError = '';
        
        // Update local inventory if the item exists
        const existingItem = this.inventoryItems.find(item => item.skuCode === request.skuCode);
        if (existingItem) {
          existingItem.quantity += request.quantity;
          // Update status
          if (existingItem.quantity > 10) existingItem.status = 'In Stock';
          else if (existingItem.quantity > 0) existingItem.status = 'Low Stock';
        }
        
        // Reset form
        this.addStockForm = { skuCode: '', quantity: 1 };
        
        // Show toast
        this.toastService.show(`Stock added successfully for ${request.skuCode}`, 'success');
        
        // Auto close form after success
        setTimeout(() => {
          this.showAddStockForm = false;
          this.addStockMessage = '';
        }, 2000);
      },
      error: (err) => {
        console.error('Error adding stock:', err);
        this.addStockError = err.error?.message || 'Failed to add stock. Please try again.';
        this.addStockMessage = '';
      }
    });
  }
}
