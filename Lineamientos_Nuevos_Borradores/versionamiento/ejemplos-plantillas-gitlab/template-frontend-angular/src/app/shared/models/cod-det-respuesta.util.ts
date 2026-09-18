/**
 * Interpretación del catálogo `codDetRespuesta` (`LIN-API-REST-001 §4.2`, fuente
 * única del catálogo) descrito en `LIN-FE-ANG-001 §9.6`.
 *
 * El componente que consume un servicio sigue siendo responsable de decidir el
 * feedback concreto al usuario; esta utilidad solo resuelve la categoría.
 */
export type CategoriaCodDetRespuesta =
  | 'exito'
  | 'exito-parcial'
  | 'validacion'
  | 'negocio'
  | 'autenticacion'
  | 'limite-peticiones'
  | 'integracion'
  | 'sistema'
  | 'desconocido';

/**
 * Clasifica un `codDetRespuesta` según los rangos normativos de `LIN-FE-ANG-001 §9.6`.
 *
 * Nota: `'200'` NO es éxito (ver nota de la sección 9.6) — cae dentro del rango
 * `200`-`203` de error de negocio.
 */
export function categorizarCodDetRespuesta(codDetRespuesta: string): CategoriaCodDetRespuesta {
  switch (codDetRespuesta) {
    case '000':
      return 'exito';
    case '001':
      return 'exito-parcial';
    case '302':
      return 'limite-peticiones';
  }

  if (codDetRespuesta >= '100' && codDetRespuesta <= '103') {
    return 'validacion';
  }
  if (codDetRespuesta >= '200' && codDetRespuesta <= '203') {
    return 'negocio';
  }
  if (codDetRespuesta >= '300' && codDetRespuesta <= '301') {
    return 'autenticacion';
  }
  if (codDetRespuesta >= '400' && codDetRespuesta <= '402') {
    return 'integracion';
  }
  if (codDetRespuesta >= '500' && codDetRespuesta <= '502') {
    return 'sistema';
  }
  return 'desconocido';
}

/** `true` si el `codDetRespuesta` representa éxito total o parcial. */
export function esCodDetRespuestaExitoso(codDetRespuesta: string): boolean {
  const categoria = categorizarCodDetRespuesta(codDetRespuesta);
  return categoria === 'exito' || categoria === 'exito-parcial';
}
