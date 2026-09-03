import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms'; // Updated to NonNullableFormBuilder
import { Router, RouterLink } from '@angular/router';
import { AuthService, LoginRequest } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {

  private fb = inject(NonNullableFormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loading = false;
  error = '';

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  ngOnInit(): void {
    // Instantly drops the global error banner the moment the user modifies their inputs
    this.form.valueChanges.subscribe(() => {
      if (this.error) {
        this.error = '';
      }
    });
  }

  login(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.error = '';

    // Values are clean, strict, non-nullable strings now
    const request: LoginRequest = this.form.getRawValue();

    this.authService.login(request).subscribe({
      next: (response) => {
        this.loading = false;
        console.log('Login successful:', response);
        this.authService.saveToken(response.token);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading = false;
        // Matches the custom global ErrorResponse backend JSON key we updated earlier
        this.error = err?.error?.errorMessage ?? err?.error?.message ?? 'Invalid email or password.';
      }
    });
  }
}