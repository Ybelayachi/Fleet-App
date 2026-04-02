import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { AuthService } from '../../core/auth/auth.service';
import { switchMap } from 'rxjs/operators';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    InputTextModule,
    ButtonModule,
    ProgressSpinnerModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  loading = false;
  errorMessage: string | null = null;

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { email, password } = this.form.getRawValue();
    if (!email || !password) {
      return;
    }

    this.loading = true;
    this.errorMessage = null;

    this.auth
      .login(email, password)
      .pipe(switchMap(() => this.auth.ensureProfile()))
      .subscribe({
        next: (role) => {
          this.loading = false;
          if (role === 'ROLE_ADMIN') {
            this.router.navigate(['/admin/users']);
          } else if (role === 'ROLE_FLEET_MANAGER') {
            this.router.navigate(['/fleet/dashboard']);
          } else {
            this.router.navigate(['/driver/vehicles']);
          }
        },
        error: () => {
          this.loading = false;
          this.errorMessage = 'E-mail ou mot de passe invalide.';
        }
      });
  }
}
