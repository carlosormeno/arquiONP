import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';

import { NotificationService } from '../../../../core/services/notification.service';
import { ApiError } from '../../../../shared/models/api-response.model';
import { UsuarioService } from '../../services/usuario.service';

/**
 * Vista de Formulario ONP (`LIN-FE-ANG-001 §7.2`): alta y edición de la
 * entidad "usuario" con `ReactiveFormsModule`/`FormBuilder`, validaciones
 * declaradas en el grupo, botones Guardar/Cancelar y mapeo de errores de
 * validación de API (§10.2) a los controles del formulario.
 */
@Component({
  selector: 'onp-usuario-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  templateUrl: './usuario-form.component.html',
  styleUrl: './usuario-form.component.scss',
})
export class UsuarioFormComponent {
  private fb = inject(FormBuilder);
  private usuarioService = inject(UsuarioService);
  private notifier = inject(NotificationService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  isLoading = signal(false);

  private idUsuario = signal<number | null>(this.leerIdDeRuta());
  readonly esEdicion = computed(() => this.idUsuario() !== null);

  frmRegistro = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    correo: ['', [Validators.required, Validators.email]],
    estado: ['ACTIVO' as 'ACTIVO' | 'INACTIVO'],
  });

  constructor() {
    const id = this.idUsuario();
    if (id !== null) {
      this.frmRegistro.get('dni')?.disable(); // el DNI no se edita (§7.2)
      this.cargarUsuario(id);
    }
  }

  guardar(): void {
    if (this.frmRegistro.invalid) {
      this.frmRegistro.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const id = this.idUsuario();
    const valores = this.frmRegistro.getRawValue();

    const peticion = id
      ? this.usuarioService.actualizar(id, {
          nombre: valores.nombre ?? '',
          correo: valores.correo ?? '',
          estado: (valores.estado as 'ACTIVO' | 'INACTIVO') ?? 'ACTIVO',
        })
      : this.usuarioService.crear({
          nombre: valores.nombre ?? '',
          dni: valores.dni ?? '',
          correo: valores.correo ?? '',
        });

    peticion.pipe(finalize(() => this.isLoading.set(false))).subscribe({
      next: () => {
        this.notifier.showSuccess(id ? 'Usuario actualizado.' : 'Usuario creado.');
        this.router.navigate(['/usuarios']);
      },
      error: (error) => {
        const erroresApi = (error?.error?.errors ?? null) as ApiError[] | null;
        if (erroresApi) {
          this.mapApiErrors(erroresApi);
        }
        this.notifier.showError('No fue posible guardar el usuario.');
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/usuarios']);
  }

  private mapApiErrors(errors: ApiError[]): void {
    errors.forEach((error) => {
      const control = this.frmRegistro.get(error.campo);
      if (control) {
        control.setErrors({ apiError: error.mensaje });
      }
    });
  }

  private cargarUsuario(id: number): void {
    this.isLoading.set(true);
    this.usuarioService
      .obtener(id)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe((respuesta) => {
        if (respuesta.data) {
          this.frmRegistro.patchValue(respuesta.data);
        }
      });
  }

  private leerIdDeRuta(): number | null {
    const idParam = this.route.snapshot.paramMap.get('id');
    return idParam ? Number(idParam) : null;
  }
}
