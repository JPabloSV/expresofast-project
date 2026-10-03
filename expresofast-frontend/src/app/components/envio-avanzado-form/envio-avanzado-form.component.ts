import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  FormArray,
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { EnvioService } from '../../services/envio.service';
import { Conductor, EnvioRegistroPayload, Vehiculo } from '../../models/envio.model';
import { fechaEntregaPosteriorValidator } from '../../validators/fechas.validator';
import { trackingDisponibleValidator } from '../../validators/tracking-disponible.validator';

/* ---------- Tipos del formulario (Typed Forms) ---------- */

/** Un paquete dentro del FormArray. pesoKg es number | null: asignarle un string no compila. */
export interface PaqueteForm {
  descripcion: FormControl<string>;
  pesoKg: FormControl<number | null>;
}

export interface EnvioAvanzadoForm {
  numeroTracking: FormControl<string>;
  destinatario: FormControl<string>;
  direccionDestino: FormControl<string>;
  montoFlete: FormControl<number | null>;
  fechaDespacho: FormControl<string>;
  fechaEntregaEstimada: FormControl<string>;
  vehiculoId: FormControl<number | null>;
  conductorId: FormControl<number | null>;
  paquetes: FormArray<FormGroup<PaqueteForm>>;
}

@Component({
  imports: [ReactiveFormsModule, DecimalPipe],
  selector: 'app-envio-avanzado-form',
  styleUrl: './envio-avanzado-form.component.css',
  templateUrl: './envio-avanzado-form.component.html',
})
export class EnvioAvanzadoFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly envioService = inject(EnvioService);
  private readonly router = inject(Router);

  readonly vehiculos = signal<Vehiculo[]>([]);
  readonly conductores = signal<Conductor[]>([]);
  readonly guardando = signal(false);
  readonly exito = signal('');
  readonly error = signal('');

  /** FormGroup fuertemente tipado: su tipo es FormGroup<EnvioAvanzadoForm>. */
  readonly form: FormGroup<EnvioAvanzadoForm> = this.fb.group(
    {
      numeroTracking: this.fb.control('', {
        validators: [Validators.required, Validators.pattern(/^EXP-\d{4}$/)],
        asyncValidators: [trackingDisponibleValidator(this.envioService)],
      }),
      destinatario: this.fb.control('', [Validators.required, Validators.minLength(3), Validators.maxLength(150)]),
      direccionDestino: this.fb.control('', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]),
      montoFlete: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
      fechaDespacho: this.fb.control('', Validators.required),
      fechaEntregaEstimada: this.fb.control('', Validators.required),
      vehiculoId: this.fb.control<number | null>(null, Validators.required),
      conductorId: this.fb.control<number | null>(null, Validators.required),
      paquetes: this.fb.array([this.crearPaquete()], Validators.minLength(1)),
    },
    // Validador cruzado aplicado al FormGroup completo (no a un control individual).
    { validators: fechaEntregaPosteriorValidator('fechaDespacho', 'fechaEntregaEstimada') },
  );

  ngOnInit(): void {
    this.envioService.obtenerVehiculos().subscribe({
      next: (lista) => this.vehiculos.set(lista),
      error: () => this.error.set('No se pudieron cargar los vehículos. Verifique que el backend esté en ejecución.'),
    });
    this.envioService.obtenerConductores().subscribe({
      next: (lista) => this.conductores.set(lista),
      error: () => this.error.set('No se pudieron cargar los conductores. Verifique que el backend esté en ejecución.'),
    });
  }

  /* ---------- FormArray de paquetes ---------- */

  get paquetes(): FormArray<FormGroup<PaqueteForm>> {
    return this.form.controls.paquetes;
  }

  private crearPaquete(): FormGroup<PaqueteForm> {
    return this.fb.group({
      descripcion: this.fb.control('', [Validators.required, Validators.maxLength(255)]),
      pesoKg: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01), Validators.max(999.99)]),
    });
  }

  agregarPaquete(): void {
    this.paquetes.push(this.crearPaquete());
  }

  eliminarPaquete(indice: number): void {
    // Regla del negocio: siempre debe quedar al menos un paquete.
    if (this.paquetes.length > 1) {
      this.paquetes.removeAt(indice);
    }
  }

  /** Suma en vivo de los pesos capturados (los vacíos cuentan como 0). */
  get pesoTotal(): number {
    return this.paquetes.controls.reduce((total, p) => total + (p.controls.pesoKg.value ?? 0), 0);
  }

  get vehiculoSeleccionado(): Vehiculo | undefined {
    return this.vehiculos().find((v) => v.id === this.form.controls.vehiculoId.value);
  }

  /* ---------- Envío ---------- */

  registrar(): void {
    if (this.form.invalid || this.form.pending) {
      this.form.markAllAsTouched();
      return;
    }

    // getRawValue() conserva los tipos: v.paquetes es { descripcion: string; pesoKg: number | null }[]
    const v = this.form.getRawValue();
    const payload: EnvioRegistroPayload = {
      numeroTracking: v.numeroTracking.trim(),
      destinatario: v.destinatario.trim(),
      direccionDestino: v.direccionDestino.trim(),
      montoFlete: v.montoFlete!,
      fechaDespacho: v.fechaDespacho,
      fechaEntregaEstimada: v.fechaEntregaEstimada,
      vehiculoId: v.vehiculoId!,
      conductorId: v.conductorId!,
      paquetes: v.paquetes.map((p) => ({ descripcion: p.descripcion.trim(), pesoKg: p.pesoKg! })),
    };

    this.guardando.set(true);
    this.exito.set('');
    this.error.set('');

    this.envioService.crearEnvio(payload).subscribe({
      next: (envio) => {
        this.guardando.set(false);
        this.exito.set(
          `Envío ${envio.codigoRastreo} registrado con ${envio.paquetes.length} paquete(s) y ${envio.pesoKg} kg en total.`,
        );
        this.limpiar();
      },
      error: (err: HttpErrorResponse) => {
        this.guardando.set(false);
        const mensaje = err.error?.mensaje as string | undefined;
        const detalles = (err.error?.detalles as string[] | undefined) ?? [];
        if (err.status === 0) {
          this.error.set('No se pudo registrar el envío. Verifique que el backend esté en ejecución.');
        } else {
          this.error.set([mensaje ?? 'No se pudo registrar el envío.', ...detalles].join(' '));
        }
        if (err.status === 409) {
          this.form.controls.numeroTracking.setErrors({ trackingTomado: true });
        }
      },
    });
  }

  limpiar(): void {
    this.paquetes.clear();
    this.paquetes.push(this.crearPaquete());
    this.form.reset();
  }

  verEnvios(): void {
    this.router.navigate(['/envios']);
  }
}
