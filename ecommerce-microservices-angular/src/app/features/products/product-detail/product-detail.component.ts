import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Product, ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  loading = true;
  error = '';
  addingToCart = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadProduct(id);
    } else {
      this.error = 'Product ID not found';
      this.loading = false;
    }
  }

  loadProduct(id: string): void {
    this.productService.getProduct(id).subscribe({
      next: (data) => {
        this.product = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading product', err);
        this.error = 'Failed to load product details';
        this.loading = false;
      }
    });
  }

  get isInStock(): boolean {
    if (!this.product) return false;
    // Same logic as product list: null stock = available if active
    if (this.product.stock === null || this.product.stock === undefined) {
      return this.product.active !== false;
    }
    return this.product.stock > 0;
  }

  get stockDisplay(): string {
    if (!this.product) return '';
    if (this.product.stock != null && this.product.stock > 0) {
      return `${this.product.stock} available`;
    }
    return this.isInStock ? 'Available' : 'Out of Stock';
  }

  addToCart(): void {
    if (!this.product || !this.isInStock || this.addingToCart) return;

    this.addingToCart = true;
    const sku = this.product.sku || this.product.id;

    const cartItem: any = {
      skuCode: sku,
      price: this.product.price,
      quantity: 1
    };

    this.cartService.addToCart(cartItem).subscribe({
      next: () => {
        this.toastService.show(`${this.product!.name} added to cart! 🛍️`, 'success');
        this.addingToCart = false;
      },
      error: () => {
        this.toastService.show('Failed to add to cart', 'error');
        this.addingToCart = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/products']);
  }
}
