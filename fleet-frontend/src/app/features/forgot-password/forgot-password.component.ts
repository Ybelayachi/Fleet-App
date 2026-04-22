import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NgIf } from '@angular/common';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, InputTextModule, ButtonModule, ProgressSpinnerModule],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css'
})
export class ForgotPasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  step: 1 | 2 = 1;
  loading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  /** Token returned by the backend (shown to user in dev, would be sent by email in prod) */
  resetToken = '';

  readonly emailForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  readonly resetForm = this.fb.group({
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]]
  });

  sendRequest(): void {
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMessage = null;
    const email = this.emailForm.getRawValue().email ?? '';
    this.auth.forgotPassword(email).subscribe({
      next: (res) => {
        this.loading = false;
        this.resetToken = res.token;
        this.step = 2;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Aucun compte associé à cet e-mail.';
        this.cdr.markForCheck();
      }
    });
  }

  confirmReset(): void {
    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }
    const { newPassword, confirmPassword } = this.resetForm.getRawValue();
    if (newPassword !== confirmPassword) {
      this.errorMessage = 'Les mots de passe ne correspondent pas.';
      return;
    }
    this.loading = true;
    this.errorMessage = null;
    this.auth.resetPassword(this.resetToken, newPassword ?? '').subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Mot de passe réinitialisé avec succès.';
        this.cdr.markForCheck();
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Le token est invalide ou a expiré.';
        this.cdr.markForCheck();
      }
    });
  }

  backToLogin(): void {
    this.router.navigate(['/login']);
  }
}
