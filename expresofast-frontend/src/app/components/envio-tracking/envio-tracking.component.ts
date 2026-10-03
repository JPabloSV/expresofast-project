import { Component, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EnvioService } from '../../services/envio.service';
import { Envio, EstadoEnvio } from '../../models/envio.model';

@Component({
  imports: [ReactiveFormsModule, DatePipe, DecimalPipe],
  selector: 'app-envio-tracking',
  styleUrl: './envio-tracking.component.css',
  templateUrl: './envio-tracking.component.html',
})
export class EnvioTrackingComponent {
  private readonly envioService = inject(EnvioService);

  readonly pasos: EstadoEnvio[] = ['PENDIENTE', 'EN_TRANSITO', 'ENTREGADO'];

  readonly busqueda = new FormGroup({
    codigo: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  readonly envio = signal<Envio | null>(null);
  readonly buscando = signal(false);
  readonly error = signal('');

  buscar(): void {
    const codigo = this.busqueda.controls.codigo.value.trim();
    if (!codigo) {
      return;
    }

    this.buscando.set(true);
    this.error.set('');
    this.envio.set(null);

    this.envioService.obtenerPorRastreo(codigo).subscribe({
      next: (envio) => {
        this.envio.set(envio);
        this.buscando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.buscando.set(false);
        if (err.status === 404) {
          this.error.set('No existe un envío con el código ' + codigo + '.');
        } else {
          this.error.set('No se pudo consultar el envío. Verifique que el backend esté en ejecución.');
        }
      },
    });
  }

  porcentaje(estado: EstadoEnvio): number {
    if (estado === 'CANCELADO') {
      return 0;
    }
    return Math.round(((this.pasos.indexOf(estado) + 1) / this.pasos.length) * 100);
  }

  alcanzado(paso: EstadoEnvio, estado: EstadoEnvio): boolean {
    if (estado === 'CANCELADO') {
      return false;
    }
    return this.pasos.indexOf(paso) <= this.pasos.indexOf(estado);
  }

  claseEstado(estado: EstadoEnvio): string {
    return 'insignia ' + estado.toLowerCase();
  }
}
