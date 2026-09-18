import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

/**
 * Vista de Login ONP (`LIN-FE-ANG-001 §7` — tipo "Login"). No usa el
 * `ShellComponent`: sin menú lateral ni cabecera completa.
 *
 * Implementa el tipo de vista Formulario (§7.2): `ReactiveFormsModule` con
 * `FormBuilder`, validaciones declaradas en el grupo, campos requeridos con
 * asterisco en el label.
 */
@Component({
  selector: 'onp-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private notifier = inject(NotificationService);
  private router = inject(Router);

  isLoading = signal(false);
  ocultarClave = signal(true);

  frmLogin = this.fb.group({
    usuario: ['', [Validators.required, Validators.maxLength(50)]],
    clave: ['', [Validators.required]],
  });

  onSubmit(): void {
    if (this.frmLogin.invalid) {
      this.frmLogin.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const { usuario, clave } = this.frmLogin.getRawValue();

    this.authService
      .login({ usuario: usuario ?? '', clave: clave ?? '' })
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: () => {
          this.notifier.showSuccess('Bienvenido/a.');
          this.router.navigate(['/home']);
        },
        error: () => {
          this.notifier.showError('Usuario o clave incorrectos.');
        },
      });
  }
}
