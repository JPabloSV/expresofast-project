import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validador SÍNCRONO a nivel de FormGroup (validación cruzada).
 * Recibe el grupo completo, lee las dos fechas y exige que la entrega estimada
 * sea ESTRICTAMENTE posterior al despacho. Si no se cumple, el error queda en el
 * FormGroup ({ fechasInvalidas: true }), con lo que todo el formulario es inválido.
 */
export function fechaEntregaPosteriorValidator(
  campoDespacho = 'fechaDespacho',
  campoEntrega = 'fechaEntregaEstimada',
): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const despacho: string = grupo.get(campoDespacho)?.value ?? '';
    const entrega: string = grupo.get(campoEntrega)?.value ?? '';

    // Si falta alguna fecha, de eso se encarga Validators.required en cada control.
    if (!despacho || !entrega) {
      return null;
    }

    // Los <input type="date"> entregan 'AAAA-MM-DD'; Date.parse los interpreta a la misma hora (UTC).
    return Date.parse(entrega) > Date.parse(despacho) ? null : { fechasInvalidas: true };
  };
}
