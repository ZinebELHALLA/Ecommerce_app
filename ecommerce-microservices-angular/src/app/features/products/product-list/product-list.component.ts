import { Component, OnInit } from '@angular/core';
import { Product, ProductService, Category } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { ToastService } from '../../../core/services/toast.service';
import { forkJoin } from 'rxjs';

interface ProductWithStock extends Product {
  inStock?: boolean;
  checkingStock?: boolean;
}

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {
  products: ProductWithStock[] = [];
  filteredProducts: ProductWithStock[] = [];
  loading = true;
  error = '';
  cartMessage = '';
  
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
    // FIX: Avoid N+1 API calls. Use the stock field from ProductResponseDTO first.
    // If we really need live inventory from InventoryService, do it in ONE batch call.
    
    // 1. Initial pass: use stock from product service
    this.products.forEach(p => {
        if (p.stock !== undefined && p.stock !== null) {
            p.inStock = p.stock > 0;
            p.checkingStock = false;
        }
    });

    // 2. Batch update (Optional: only if we suspect ProductService stock is stale)
    // We filter products that still need checking (if any didn't have stock field)
    const skusToCheck = this.products
        .filter(p => (p.inStock === undefined))
        .map(p => p.sku || p.id)
        .filter(sku => !!sku);

    if (skusToCheck.length > 0) {
        // Since we don't have a direct batch endpoint in ProductService for *stock*,
        // we can assume for now that ProductService stock is the source of truth for the list view.
        // If we integrated InventoryService, we would call it here ONCE with all SKUs.
        
        // For stabilization, we rely on ProductService. 
        // If stock is missing, we default to TRUE (optimistic) to not block UI, 
        // or FALSE if we want to be safe. Given "Zero Stock" bug, let's trust the 'stock' field if present.
        
        this.products.forEach(p => {
            if (p.inStock === undefined) {
                 // Fallback: If product is active, assume in stock unless explicitly 0
                 p.inStock = p.active !== false;
                 p.checkingStock = false;
            }
        });
    }
  }

  addToCart(product: ProductWithStock) {
    if (!product.inStock) return;

    const sku = product.sku || product.id;

    // Backend CartService expects: { skuCode, price, quantity }
    // NOT unitPrice - that's only for internal CartItem interface
    const cartItem: any = {
      skuCode: sku,
      price: product.price,  // ← Backend expects 'price', not 'unitPrice'
      quantity: 1
    };

    this.cartService.addToCart(cartItem).subscribe({
      next: () => {
        this.toastService.show(`${product.name} added to cart!`, 'success');
      },
      error: () => {
        this.toastService.show('Failed to add to cart.', 'error');
      }
    });
  }
}
