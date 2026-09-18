/**
 * Modelos de dominio de la feature de ejemplo "usuarios". Ver convenciones de
 * `LIN-FE-ANG-001 §5.4` (sufijos `Request`/`Response`, sin sufijo para el
 * modelo de dominio interno) y el servicio de referencia de `§9.2`.
 */
export interface Usuario {
  id: number;
  nombre: string;
  dni: string;
  correo: string;
  estado: 'ACTIVO' | 'INACTIVO';
}

export interface CreateUsuarioRequest {
  nombre: string;
  dni: string;
  correo: string;
}

export interface UpdateUsuarioRequest {
  nombre: string;
  correo: string;
  estado: 'ACTIVO' | 'INACTIVO';
}

export interface FiltroUsuarioRequest {
  nombre?: string;
  dni?: string;
  estado?: 'ACTIVO' | 'INACTIVO';
  pagina?: number;
  tamanio?: number;
}
