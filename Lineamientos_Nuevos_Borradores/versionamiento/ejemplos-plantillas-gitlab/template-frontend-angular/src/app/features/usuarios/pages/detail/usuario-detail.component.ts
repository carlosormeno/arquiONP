import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Usuario } from '../../models/usuario.model';
import { UsuarioService } from '../../services/usuario.service';

/**
 * Vista de Detalle ONP (`LIN-FE-ANG-001 §7` — tipo "Detalle"): solo lectura
 * de una entidad. Ruta: `/usuarios/:id`.
 */
@Component({
  selector: 'onp-usuario-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatProgressBarModule],
  templateUrl: './usuario-detail.component.html',
  styleUrl: './usuario-detail.component.scss',
})
export class UsuarioDetailComponent {
  private usuarioService = inject(UsuarioService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  isLoading = signal(false);
  usuario = signal<Usuario | null>(null);

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.isLoading.set(true);
    this.usuarioService
      .obtener(id)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe((respuesta) => this.usuario.set(respuesta.data));
  }

  regresar(): void {
    this.router.navigate(['/usuarios']);
  }
}
