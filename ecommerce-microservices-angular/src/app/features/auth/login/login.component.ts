import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  returnUrl = '/products';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private toastService: ToastService
  ) {
    // Redirect if already logged in
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/products']);
    }
  }

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    // Get return url from route parameters or default to '/products'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/products';
  }


  onSubmit(): void {
  if (this.loginForm.invalid) {
    return;
  }
  this.loading = true;
  const credentials = this.loginForm.value;
  this.authService.login(credentials).subscribe({
    next: (response) => {
      // Response is LoginResponse, not { token, user }
      this.toastService.show(`Welcome back, ${response.username}!`, 'success');
      this.router.navigate([this.returnUrl]);
    },
    error: (error) => {
      console.error('Login error:', error);
      this.toastService.show('Login failed. Please check your credentials.', 'error');
      this.loading = false;
    }
  });
}

  get f() {
    return this.loginForm.controls;
  }
}
