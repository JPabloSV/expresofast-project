package cr.ac.ucr.c5k023.lab10.config;

import cr.ac.ucr.c5k023.lab10.model.Envio;
import cr.ac.ucr.c5k023.lab10.repository.EnvioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner cargarEnvios(EnvioRepository repositorio) {
        return args -> {
            if (repositorio.count() > 0) {
                return;
            }
            LocalDateTime ahora = LocalDateTime.now();
            repositorio.saveAll(List.of(
                    crear("EXP-2026-1001", "Jorge Castro Mena", "Cartago, Paraíso, 200 m sur del parque", 8500.0, "PENDIENTE", ahora.minusHours(3)),
                    crear("EXP-2026-1002", "Silvia Vargas Leitón", "San José, Curridabat", 11200.0, "EN_TRANSITO", ahora.minusDays(1)),
                    crear("EXP-2026-1003", "Andrés Solís Umaña", "Heredia, San Rafael", 6500.0, "ENTREGADO", ahora.minusDays(3)),
                    crear("EXP-2026-1004", "Mariana Solís Vega", "Cartago, Tres Ríos", 4200.0, "CANCELADO", ahora.minusDays(2)),
                    crear("EXP-2026-1005", "Kevin Mora Delgado", "Alajuela, Grecia", 18500.0, "EN_TRANSITO", ahora.minusHours(20)),
                    crear("EXP-2026-1006", "Paola Chinchilla", "San José, Desamparados", 9000.0, "PENDIENTE", ahora.minusHours(6)),
                    crear("EXP-2026-1007", "Diego Fernández", "Cartago, El Guarco", 14300.0, "EN_TRANSITO", ahora.minusDays(1).minusHours(4)),
                    crear("EXP-2026-1008", "Gabriela Solano", "Heredia, Barva", 7100.0, "ENTREGADO", ahora.minusDays(4)),
                    crear("EXP-2026-1009", "Luis Carlos Mata", "San José, Escazú", 10800.0, "CANCELADO", ahora.minusDays(5)),
                    crear("EXP-2026-1010", "Ana Lucía Rodríguez", "Alajuela, San Ramón", 13900.0, "PENDIENTE", ahora.minusHours(1))
            ));
        };
    }

    private static Envio crear(String codigo, String destinatario, String direccion,
                               Double monto, String estado, LocalDateTime fecha) {
        Envio envio = new Envio();
        envio.setCodigoRastreo(codigo);
        envio.setDestinatario(destinatario);
        envio.setDireccionDestino(direccion);
        envio.setMontoFlete(monto);
        envio.setEstado(estado);
        envio.setFechaCreacion(fecha);
        return envio;
    }
}