import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Conductor,
  DisponibilidadTracking,
  Envio,
  EnvioRegistroPayload,
  EstadoEnvio,
  Vehiculo,
} from '../models/envio.model';

@Injectable({ providedIn: 'root' })
export class EnvioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.API_URL + 'envios';

  obtenerEnvios(): Observable<Envio[]> {
    return this.http.get<Envio[]>(this.baseUrl);
  }

  obtenerPorRastreo(codigo: string): Observable<Envio> {
    return this.http.get<Envio>(this.baseUrl + '/rastreo/' + encodeURIComponent(codigo));
  }

  /** Consulta GET /api/envios/check-tracking/{n} y devuelve true si el número ya existe. */
  verificarTracking(numeroTracking: string): Observable<boolean> {
    return this.http
      .get<DisponibilidadTracking>(this.baseUrl + '/check-tracking/' + encodeURIComponent(numeroTracking))
      .pipe(map((respuesta) => respuesta.existe));
  }

  crearEnvio(payload: EnvioRegistroPayload): Observable<Envio> {
    return this.http.post<Envio>(this.baseUrl, payload);
  }

  actualizarEstado(id: number, nuevoEstado: EstadoEnvio): Observable<Envio> {
    return this.http.patch<Envio>(this.baseUrl + '/' + id + '/estado', { estado: nuevoEstado });
  }

  obtenerVehiculos(): Observable<Vehiculo[]> {
    return this.http.get<Vehiculo[]>(environment.API_URL + 'vehiculos');
  }

  obtenerConductores(): Observable<Conductor[]> {
    return this.http.get<Conductor[]>(environment.API_URL + 'conductores');
  }
}
