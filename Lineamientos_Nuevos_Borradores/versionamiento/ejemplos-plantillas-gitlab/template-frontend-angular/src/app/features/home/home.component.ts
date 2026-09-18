import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { environment } from '../../../environments/environment';

/**
 * Vista de Bienvenida ONP (`LIN-FE-ANG-001 §7` — tipo "Bienvenida").
 * Pantalla inicial post-login: logo del sistema, accesos directos.
 */
@Component({
  selector: 'onp-home',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatIconModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent {
  readonly systemName = environment.systemName;
}
