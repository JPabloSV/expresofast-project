export type EstadoEnvio = 'PENDIENTE' | 'EN_TRANSITO' | 'ENTREGADO' | 'CANCELADO';

export interface Paquete {
  id?: number | null;
  descripcion: string;
  pesoKg: number;
}

export interface Envio {
  id: number;
  codigoRastreo: string;
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
  pesoKg: number;
  estado: EstadoEnvio;
  fechaCreacion: string;
  fechaDespacho: string | null;
  fechaEntregaEstimada: string | null;
  vehiculoId: number;
  vehiculoPlaca: string;
  conductorId: number;
  conductorNombre: string;
  paquetes: Paquete[];
}

/** Cuerpo del POST /api/envios (coincide con EnvioRegistroDTO del backend). */
export interface EnvioRegistroPayload {
  numeroTracking: string;
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
  fechaDespacho: string;
  fechaEntregaEstimada: string;
  vehiculoId: number;
  conductorId: number;
  paquetes: Paquete[];
}

export interface DisponibilidadTracking {
  numeroTracking: string;
  existe: boolean;
}

export interface Vehiculo {
  id: number;
  placa: string;
  capacidadKg: number;
  estado: string;
}

export interface Conductor {
  id: number;
  nombreCompleto: string;
}
