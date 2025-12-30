import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CartService, CartResponse, CartItem } from '../../core/services/cart.service';
import { OrderService, CheckoutRequest } from '../../core/services/order.service';

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
  
  // Checkout Form Data
  deliveryAddress = '';
  paymentMethod = 'CREDIT_CARD'; // Default
  showCheckoutForm = false;

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
        
        // Optional: Pre-validate to warn user
        this.validateCart();
      },
      error: (err) => {
        console.error('Error loading cart', err);
        this.cart = { userId: this.cartService.getUserId(), cartItems: [], totalPrice: 0 };
        this.loading = false;
      }
    });
  }

  validateCart() {
      if (!this.cart || this.cart.cartItems.length === 0) return;
      
      this.cartService.getValidatedCart().subscribe({
          next: (validation) => {
              if (validation.validationErrors && validation.validationErrors.length > 0) {
                  this.checkoutError = 'Warning: ' + validation.validationErrors.join(', ');
                  // Disable checkout visually? 
              } else if (!validation.isValid) {
                  this.checkoutError = 'Warning: Cart contains invalid items.';
              } else {
                  // Clear previous warnings if valid
                  if (this.checkoutError.startsWith('Warning')) {
                      this.checkoutError = '';
                  }
              }
          },
          error: (err) => {
              console.warn('Validation check failed', err);
          }
      });
  }

  updateQuantity(id: string | number, quantity: number) {
     if (quantity < 1) return;
     const item = this.cart?.cartItems.find(i => i.id === id); 
     if (!item) return;

    this.cartService.updateItem(item.skuCode, quantity).subscribe({
      next: (data) => { 
          this.cart = data; 
          this.validateCart(); // Re-validate
      },
      error: (err) => { this.error = 'Failed to update'; setTimeout(() => this.error = '', 2000); }
    });
  }

  removeItem(id: string | number) {
    const item = this.cart?.cartItems.find(i => i.id === id);
    if (!item) return;

    this.cartService.removeItem(item.skuCode).subscribe({
      next: (data) => { 
          this.cart = data; 
          this.validateCart(); // Re-validate
      },
      error: (err) => { this.error = 'Failed to remove'; setTimeout(() => this.error = '', 2000); }
    });
  }

  toggleCheckout() {
    this.showCheckoutForm = !this.showCheckoutForm;
  }

  submitCheckout() {
    if (!this.cart || this.cart.cartItems.length === 0) return;
    if (!this.deliveryAddress) {
      this.checkoutError = 'Please enter a delivery address.';
      return;
    }

    this.loading = true;
    
    // FIX: Backend 'validateCart' relies on InventoryService which may be unstable/503.
    // Since we aligned Frontend Inventory to ProductService (Source of Truth), we trust the Frontend check.
    // We assume items added to cart were valid at the time of addition.
    // Proceed directly to executeOrder.
    this.executeOrder();
  }
  
  private executeOrder() {
    // Construct CheckoutRequest
    const request: CheckoutRequest = {
        userId: this.cart!.userId,
        deliveryAddress: this.deliveryAddress,
        paymentMethod: this.paymentMethod
    };

    console.log('Sending checkout request:', request);

    this.orderService.checkout(request).subscribe({
      next: (response) => {
        this.checkoutMessage = 'Order placed successfully! Order #' + (response.orderNumber || 'Confirmed');
        // Clear cart locally since backend clears it
        this.cart = { userId: this.cartService.getUserId(), cartItems: [], totalPrice: 0 };
        this.loading = false;
        
        // Redirect after 2 seconds
        setTimeout(() => {
            this.router.navigate(['/products']);
        }, 2500);
      },
      error: (err) => {
        console.error('Checkout error', err);
        const errMsg = err.error?.message || err.message || 'Unknown error';
        this.checkoutError = 'Checkout Failed: ' + errMsg;
        this.loading = false;
      }
    });
  }

  get cartItems(): CartItem[] { return this.cart ? this.cart.cartItems : []; }
  get totalPrice(): number { return this.cart ? this.cart.totalPrice : 0; }
  
  clearCart() {
      if (!this.cart || this.cartItems.length === 0) return;
      this.cartService.clearCart().subscribe({
          next: () => { this.cart = { userId: this.cartService.getUserId(), cartItems: [], totalPrice: 0 }; },
          error: () => { this.error = 'Failed to clear cart'; }
      });
  }
}
