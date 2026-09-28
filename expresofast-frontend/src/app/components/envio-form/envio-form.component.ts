import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EnvioService } from '../../services/envio.service';
import { CrearEnvioPayload } from '../../models/envio.model';

@Component({
  imports: [FormsModule],
  selector: 'app-envio-form',
  styleUrl: './envio-form.component.css',
  templateUrl: './envio-form.component.html',
})
export class EnvioFormComponent {
  private readonly envioService = inject(EnvioService);
  private readonly router = inject(Router);

  destinatario = '';
  direccionDestino = '';
  montoFlete: number | null = null;

  readonly guardando = signal(false);
  readonly exito = signal('');
  readonly error = signal('');

  guardar(): void {
    if (this.montoFlete === null) {
      return;
    }
    const payload: CrearEnvioPayload = {
      destinatario: this.destinatario.trim(),
      direccionDestino: this.direccionDestino.trim(),
      montoFlete: this.montoFlete,
    };

    this.guardando.set(true);
    this.exito.set('');
    this.error.set('');

    this.envioService.crearEnvio(payload).subscribe({
      next: (envio) => {
        this.guardando.set(false);
        this.exito.set('Envío registrado con el código ' + envio.codigoRastreo + '.');
        this.limpiar();
      },
      error: () => {
        this.guardando.set(false);
        this.error.set('No se pudo registrar el envío. Verifique que el backend esté en ejecución.');
      },
    });
  }

  limpiar(): void {
    this.destinatario = '';
    this.direccionDestino = '';
    this.montoFlete = null;
  }

  verEnvios(): void {
    this.router.navigate(['/envios']);
  }
}
