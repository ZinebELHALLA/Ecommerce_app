import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CartService, CartResponse, CartItem } from '../../core/services/cart.service';
import { OrderService } from '../../core/services/order.service';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {
  cart: CartResponse | null = null;
  loading = true;
  error = '';
  checkoutMessage = '';
  checkoutError = '';

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart() {
    this.loading = true;
    this.cartService.getCart().subscribe({
      next: (data) => {
        this.cart = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading cart', err);
        // If 404, it might mean empty cart or user not found, treat as empty
        this.cart = { userId: this.cartService.getUserId(), cartItems: [], totalPrice: 0 };
        this.loading = false;
      }
    });
  }

  updateQuantity(item: CartItem, change: number) {
    const newQty = item.quantity + change;
    if (newQty < 1) return;

    this.cartService.updateItem(item.skuCode, newQty).subscribe({
      next: (data) => {
        this.cart = data;
      },
      error: (err) => {
        this.error = 'Failed to update quantity';
        setTimeout(() => this.error = '', 3000);
      }
    });
  }

  removeItem(item: CartItem) {
    this.cartService.removeItem(item.skuCode).subscribe({
      next: (data) => {
        this.cart = data;
      },
      error: (err) => {
        this.error = 'Failed to remove item';
        setTimeout(() => this.error = '', 3000);
      }
    });
  }

  checkout() {
    if (!this.cart || this.cart.cartItems.length === 0) return;

    this.loading = true;
    
    // Construct OrderRequest
    const orderRequest = {
        userId: this.cart.userId,
        orderLineItemsDtoList: this.cart.cartItems
    };

    this.orderService.placeOrder(orderRequest).subscribe({
      next: (response) => {
        this.checkoutMessage = 'Order placed successfully! Order #' + (response.orderNumber || 'Confirmed');
        this.cartService.clearCart().subscribe(() => {
            this.cart = { userId: this.cartService.getUserId(), cartItems: [], totalPrice: 0 };
            this.loading = false;
            // Redirect after 2 seconds
            setTimeout(() => {
                this.router.navigate(['/products']);
            }, 2000);
        });
      },
      error: (err) => {
        console.error('Checkout error', err);
        this.checkoutError = 'Failed to place order. Items might be out of stock.';
        this.loading = false;
        setTimeout(() => this.checkoutError = '', 5000);
      }
    });
  }
}
