import { Component, OnInit } from '@angular/core';
import { Product, ProductService, Category } from '../../core/services/product.service';
import { CartService } from '../../core/services/cart.service';
import { InventoryService } from '../../core/services/inventory.service';
import { ToastService } from '../../core/services/toast.service';
import { forkJoin } from 'rxjs';

interface ProductWithStock extends Product {
  inStock?: boolean;
  checkingStock?: boolean;
  availableQuantity?: number;
}

@Component({
  selector: 'app-catalog',
  templateUrl: './catalog.component.html',
  styleUrls: ['./catalog.component.css']
})
export class CatalogComponent implements OnInit {
  products: ProductWithStock[] = [];
  filteredProducts: ProductWithStock[] = [];
  loading = true;
  error = '';
  
  // Search & Filter
  searchTerm = '';
  selectedCategory = '';
  priceRange: number | null = null;
  
  // Categories
  categories: Category[] = [];
  categoryMap = new Map<string, string>();

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private inventoryService: InventoryService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loading = true;
    forkJoin({
        products: this.productService.getProducts(),
        categories: this.productService.getCategories()
    }).subscribe({
        next: (data) => {
            // Process Categories
            this.categories = data.categories;
            this.categories.forEach(c => this.categoryMap.set(c.id, c.name));

            // Process Products
            this.products = data.products.map(p => ({ ...p, checkingStock: true }));
            this.filteredProducts = [...this.products];
            
            this.loading = false;
            this.checkInventory();
        },
        error: (err) => {
            console.error('Error loading data', err);
            this.error = 'Failed to load store data.';
            this.loading = false;
        }
    });
  }

  filterProducts() {
    let temp = this.products;

    // Search
    if (this.searchTerm) {
      const lower = this.searchTerm.toLowerCase();
      temp = temp.filter(p => 
        p.name.toLowerCase().includes(lower) || 
        p.description.toLowerCase().includes(lower)
      );
    }

    // Category
    if (this.selectedCategory) {
        temp = temp.filter(p => p.categoryId === this.selectedCategory);
    }

    // Price Max
    if (this.priceRange) {
        temp = temp.filter(p => p.price <= this.priceRange!);
    }

    this.filteredProducts = temp;
  }

  getCategoryName(id?: string): string {
      if (!id) return 'Uncategorized';
      return this.categoryMap.get(id) || 'Unknown Category';
  }

  checkInventory() {
    // Check stock for each product using inventory service
    const skuCodes = this.products
        .map(p => p.sku || p.id)
        .filter(sku => !!sku);

    if (skuCodes.length > 0) {
        this.inventoryService.checkBatchStock(skuCodes).subscribe({
            next: (inventoryData) => {
                this.products.forEach(p => {
                    const sku = p.sku || p.id;
                    const inventoryItem = inventoryData.find(inv => inv.skuCode === sku);
                    
                    if (inventoryItem) {
                        p.inStock = inventoryItem.inStock;
                        p.availableQuantity = inventoryItem.availableQuantity;
                    } else {
                        p.inStock = false;
                        p.availableQuantity = 0;
                    }
                    p.checkingStock = false;
                });
                
                // Update filtered products
                this.filterProducts();
            },
            error: (err) => {
                console.error('Error checking inventory:', err);
                // Fallback: assume in stock if inventory service fails
                this.products.forEach(p => {
                    p.inStock = true;
                    p.checkingStock = false;
                    p.availableQuantity = 1;
                });
            }
        });
    }
  }

  addToCart(product: ProductWithStock, quantity: number = 1) {
    if (!product.inStock || (product.availableQuantity && quantity > product.availableQuantity)) {
        this.toastService.show('Product out of stock or insufficient quantity!', 'error');
        return;
    }

    const sku = product.sku || product.id;

    const cartItem: any = {
      skuCode: sku,
      price: product.price,
      quantity: quantity
    };

    this.cartService.addToCart(cartItem).subscribe({
      next: () => {
        this.toastService.show(`${product.name} added to cart!`, 'success');
        // Update local stock count
        if (product.availableQuantity) {
          product.availableQuantity -= quantity;
          product.inStock = product.availableQuantity > 0;
        }
      },
      error: () => {
        this.toastService.show('Failed to add to cart.', 'error');
      }
    });
  }
}