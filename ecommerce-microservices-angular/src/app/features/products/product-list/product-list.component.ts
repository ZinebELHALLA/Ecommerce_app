import { Component, OnInit } from '@angular/core';
import { Product, ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';

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
  categories: string[] = [];

  constructor(
    private productService: ProductService,
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts() {
    this.productService.getProducts().subscribe({
      next: (data: Product[]) => {
        this.products = data.map(p => ({ ...p, checkingStock: true }));
        this.extractCategories();
        this.filteredProducts = [...this.products];
        this.loading = false;
        this.checkInventory();
      },
      error: (err: any) => {
        console.error('Error loading products', err);
        this.error = 'Failed to load products. Please ensure the backend is running.';
        this.loading = false;
      }
    });
  }
  
  extractCategories() {
    const cats = new Set(this.products.map(p => p.categoryId).filter(c => !!c));
    this.categories = Array.from(cats) as string[];
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

  checkInventory() {
    this.products.forEach(product => {
      // Use sku from product DTO, fallback to ID if missing (shouldn't happen with correct DTO)
      const sku = product.sku || product.id; 
      if (sku) {
        this.productService.checkStock(sku).subscribe({
          next: (stock) => {
            product.inStock = stock.inStock;
            product.checkingStock = false;
          },
          error: () => {
             product.inStock = false; 
             product.checkingStock = false;
          }
        });
      } else {
        product.checkingStock = false;
        product.inStock = false;
      }
    });
  }

  addToCart(product: ProductWithStock) {
    if (!product.inStock) return;

    const sku = product.sku || product.id;

    this.cartService.addToCart({
      skuCode: sku,
      price: product.price,
      quantity: 1
    }).subscribe({
      next: () => {
        this.cartMessage = `Added ${product.name} to cart!`;
        setTimeout(() => this.cartMessage = '', 3000);
      },
      error: () => {
        this.cartMessage = 'Failed to add to cart.';
        setTimeout(() => this.cartMessage = '', 3000);
      }
    });
  }
}
