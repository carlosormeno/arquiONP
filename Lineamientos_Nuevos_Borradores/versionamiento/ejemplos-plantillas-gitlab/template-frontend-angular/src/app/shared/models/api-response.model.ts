/**
 * Modelo de respuesta estándar de las APIs REST de ONP.
 *
 * Contrato definido en `LIN-API-REST-001 §4`; implementación Java de referencia
 * en `LIN-DEV-JAVA-001 §13.4.4`. Ver `LIN-FE-ANG-001 §9.1`.
 */
export interface ApiResponse<T> {
  codHttp: number;
  codDetRespuesta: string;
  menDetRespuesta: string;
  data: T | null;
  errors: ApiError[] | null;
  meta: ApiMeta | null;
}

/** Ver `LIN-FE-ANG-001 §9.1`. */
export interface ApiError {
  campo: string;
  mensaje: string;
}

/** Ver `LIN-FE-ANG-001 §9.1`. */
export interface ApiMeta {
  timestamp: string;
  requestId: string;
  version: string;
  pagina?: number;
  tamanio?: number;
  totalElementos?: number;
  totalPaginas?: number;
}
