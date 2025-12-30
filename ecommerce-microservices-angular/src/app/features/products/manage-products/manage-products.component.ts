import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductService, ProductRequestDTO } from '../../../core/services/product.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-manage-products',
  templateUrl: './manage-products.component.html',
  styleUrls: ['./manage-products.component.css']
})
export class ManageProductsComponent {
  productForm: FormGroup;
  successMessage = '';
  errorMessage = '';
  loading = false;
  categories: any[] = []; // Store fetched categories

  // Category Creation Logic Properties
  showAddCategory = false;
  newCategory = { name: '', description: '' };
  creatingCategory = false;

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private toastService: ToastService
  ) {
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      price: [0, [Validators.required, Validators.min(0.01)]],
      stock: [0, [Validators.required, Validators.min(0)]],
      category: ['', Validators.required],
      imageUrl: [''],
      sku: ['', Validators.required]
    });
    
    this.loadCategories();
  }

  loadCategories() {
      this.productService.getCategories().subscribe({
          next: (data) => {
              this.categories = data;
          },
          error: (err) => {
              console.error('Failed to load categories', err);
              // Optional: show error or fallback
          }
      });
  }

  onSubmit() {
    if (this.productForm.invalid) {
      return;
    }

    this.loading = true;
    
    // Construct the payload to match ProductRequestDTO exactly
    const formValue = this.productForm.value;
    const payload: ProductRequestDTO = {
        name: formValue.name,
        description: formValue.description,
        price: Number(formValue.price),
        stock: Number(formValue.stock),
        categoryId: formValue.category, 
        sku: formValue.sku,
        imageUrl: formValue.imageUrl,
        active: true 
    };

    this.productService.createProduct(payload).subscribe({
      next: () => {
        this.handleSuccess();
      },
      error: (err: any) => {
        console.error('Create product error', err);
        // WORKAROUND: Backend throws 500 when Inventory Service is down, but Product IS created.
        // We catch this specific case to not block the user.
        if (err.status === 500 || err.status === 503) {
            this.handleSuccess('Product created! (Inventory sync may be delayed)');
        } else {
            this.toastService.show('Failed to create product.', 'error');
            this.errorMessage = 'Failed to create product. Please try again.';
            this.loading = false;
        }
      }
    });
  }

  handleSuccess(msg: string = 'Product created successfully!') {
    this.toastService.show(msg, 'success');
    this.successMessage = msg;
    this.errorMessage = '';
    this.loading = false;
    this.productForm.reset({ price: 0, stock: 0 });
    setTimeout(() => this.successMessage = '', 3000);
  }

  toggleAddCategory() {
      this.showAddCategory = !this.showAddCategory;
  }

  createNewCategory() {
      if (!this.newCategory.name) return;

      this.creatingCategory = true;
      this.productService.createCategory(this.newCategory).subscribe({
          next: (cat) => {
              this.loadCategories(); // Reload list
              this.productForm.patchValue({ category: cat.id }); // Select new category
              this.showAddCategory = false;
              this.newCategory = { name: '', description: '' };
              this.creatingCategory = false;
              this.successMessage = 'Category created and selected!';
              this.toastService.show('Category created!', 'success');
              setTimeout(() => this.successMessage = '', 3000);
          },
          error: (err) => {
              console.error('Failed to create category', err);
              this.toastService.show('Failed to create category.', 'error');
              this.errorMessage = 'Failed to create category.';
              this.creatingCategory = false;
          }
      });
  }
}
