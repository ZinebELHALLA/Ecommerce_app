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
  loading = true;
  error = '';
  cartMessage = '';

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

  checkInventory() {
    this.products.forEach(product => {
      // Assuming skuCode is available or using ID as skuCode if missing
      const sku = product.skuCode || product.id.toString(); 
      if (sku) {
        this.productService.checkStock(sku).subscribe({
          next: (stock) => {
            product.inStock = stock.inStock;
            product.checkingStock = false;
          },
          error: () => {
             // Fallback if inventory service fails or product not found in inventory
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

    // Use skuCode or ID 
    const sku = product.skuCode || product.id.toString();

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
