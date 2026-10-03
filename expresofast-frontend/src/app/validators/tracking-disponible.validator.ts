import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { Observable, catchError, map, of, switchMap, timer } from 'rxjs';
import { EnvioService } from '../services/envio.service';

/**
 * Validador ASÍNCRONO del número de tracking.
 * Angular lo ejecuta solo cuando los validadores síncronos del control ya pasaron
 * (obligatorio + formato EXP-1234), y mientras espera la respuesta el control queda
 * en estado PENDING.
 *
 * Devuelve un Observable porque la respuesta llega más tarde desde la red: el
 * Event Loop sigue atendiendo al usuario y Angular se suscribe para recibir el resultado.
 */
export function trackingDisponibleValidator(envioService: EnvioService, esperaMs = 400): AsyncValidatorFn {
  return (control: AbstractControl<string>): Observable<ValidationErrors | null> => {
    const numero = (control.value ?? '').trim();
    if (!numero) {
      return of(null);
    }

    // timer() actúa como debounce: si el usuario sigue escribiendo, Angular cancela
    // (unsubscribe) este Observable antes de que se dispare la petición HTTP.
    return timer(esperaMs).pipe(
      switchMap(() => envioService.verificarTracking(numero)),
      map((existe) => (existe ? { trackingTomado: true } : null)),
      // Si el backend no responde no se bloquea el formulario: el POST
      // volverá a validar el duplicado (HTTP 409).
      catchError(() => of(null)),
    );
  };
}
