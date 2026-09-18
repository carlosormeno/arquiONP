import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { NotificationService } from '../../../../core/services/notification.service';
import { Usuario } from '../../models/usuario.model';
import { UsuarioService } from '../../services/usuario.service';

/**
 * Vista de Listado ONP (`LIN-FE-ANG-001 §7.1`): filtros de búsqueda, tabla
 * paginada con acciones y botón "Nuevo".
 */
@Component({
  selector: 'onp-usuario-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './usuario-list.component.html',
  styleUrl: './usuario-list.component.scss',
})
export class UsuarioListComponent {
  private fb = inject(FormBuilder);
  private usuarioService = inject(UsuarioService);
  private notifier = inject(NotificationService);

  readonly columnas = ['nombre', 'dni', 'correo', 'estado', 'acciones'];

  readonly usuarios = signal<Usuario[]>([]);
  readonly isLoading = signal(false);
  readonly totalElementos = signal(0);
  readonly pagina = signal(0);
  readonly tamanio = signal(10);

  frmFiltros = this.fb.group({
    nombre: [''],
    dni: [''],
    estado: [''],
  });

  constructor() {
    this.cargarDatos();
  }

  buscar(): void {
    this.pagina.set(0);
    this.cargarDatos();
  }

  limpiar(): void {
    this.frmFiltros.reset({ nombre: '', dni: '', estado: '' });
    this.buscar();
  }

  onPageChange(event: PageEvent): void {
    this.pagina.set(event.pageIndex);
    this.tamanio.set(event.pageSize);
    this.cargarDatos();
  }

  onSortChange(_sort: Sort): void {
    // Ordenamiento del lado servidor: se integraría en FiltroUsuarioRequest
    // cuando el backend real exponga los parámetros de orden.
  }

  eliminar(usuario: Usuario): void {
    this.usuarioService.eliminar(usuario.id).subscribe({
      next: () => {
        this.notifier.showSuccess('Usuario eliminado correctamente.');
        this.cargarDatos();
      },
    });
  }

  private cargarDatos(): void {
    this.isLoading.set(true);
    const filtrosForm = this.frmFiltros.getRawValue();

    this.usuarioService
      .listar({
        nombre: filtrosForm.nombre || undefined,
        dni: filtrosForm.dni || undefined,
        estado: (filtrosForm.estado as 'ACTIVO' | 'INACTIVO') || undefined,
        pagina: this.pagina(),
        tamanio: this.tamanio(),
      })
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe((respuesta) => {
        this.usuarios.set(respuesta.data ?? []);
        this.totalElementos.set(respuesta.meta?.totalElementos ?? respuesta.data?.length ?? 0);
      });
  }
}
