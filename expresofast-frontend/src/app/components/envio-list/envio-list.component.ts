import { Component, OnInit, inject, signal } from '@angular/core';
import { EnvioService } from '../../services/envio.service';
import { Envio, EstadoEnvio } from '../../models/envio.model';

@Component({
  imports: [],
  selector: 'app-envio-list',
  styleUrl: './envio-list.component.css',
  templateUrl: './envio-list.component.html',
})
export class EnvioListComponent implements OnInit {
  private readonly envioService = inject(EnvioService);

  readonly estados: EstadoEnvio[] = ['PENDIENTE', 'EN_TRANSITO', 'ENTREGADO', 'CANCELADO'];
  readonly envios = signal<Envio[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.envioService.obtenerEnvios().subscribe({
      next: (lista) => {
        this.envios.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de envíos. Verifique que el backend esté en ejecución.');
        this.cargando.set(false);
      },
    });
  }

  cambiarEstado(envio: Envio, evento: Event): void {
    const nuevoEstado = (evento.target as HTMLSelectElement).value as EstadoEnvio;
    this.error.set('');
    this.envioService.actualizarEstado(envio.id, nuevoEstado).subscribe({
      next: (actualizado) => {
        this.envios.update((lista) => lista.map((e) => (e.id === actualizado.id ? actualizado : e)));
      },
      error: () => {
        this.error.set('No se pudo actualizar el estado del envío ' + envio.codigoRastreo + '.');
        this.envios.update((lista) => [...lista]);
      },
    });
  }

  claseEstado(estado: EstadoEnvio): string {
    return 'insignia ' + estado.toLowerCase();
  }
}
