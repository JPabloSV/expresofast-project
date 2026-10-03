package cr.ac.ucr.c5k023.lab10;

import cr.ac.ucr.c5k023.lab10.dto.EnvioRegistroDTO;
import cr.ac.ucr.c5k023.lab10.dto.PaqueteDTO;
import cr.ac.ucr.c5k023.lab10.service.EnvioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EnvioRegistroIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EnvioService envioService;

    @Autowired
    private JdbcTemplate jdbc;

    private static String cuerpo(String tracking, String despacho, String entrega, String paquetesJson) {
        return """
                {
                  "numeroTracking": "%s",
                  "destinatario": "Ana Rodriguez",
                  "direccionDestino": "Cartago, Paraiso centro",
                  "montoFlete": 9500.00,
                  "fechaDespacho": "%s",
                  "fechaEntregaEstimada": "%s",
                  "vehiculoId": 1,
                  "conductorId": 1,
                  "paquetes": %s
                }
                """.formatted(tracking, despacho, entrega, paquetesJson);
    }

    private static final String DOS_PAQUETES = """
            [ {"descripcion": "Caja de libros", "pesoKg": 12.50},
              {"descripcion": "Monitor", "pesoKg": 7.25} ]""";

    @Test
    void checkTrackingIndicaSiElNumeroExiste() throws Exception {
        mvc.perform(get("/api/envios/check-tracking/EXP-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existe").value(true));
        mvc.perform(get("/api/envios/check-tracking/exp-1001"))
                .andExpect(jsonPath("$.existe").value(true));
        mvc.perform(get("/api/envios/check-tracking/EXP-9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existe").value(false));
    }

    @Test
    void registraEnvioConSusPaquetesEnLaMismaOperacion() throws Exception {
        mvc.perform(post("/api/envios").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("EXP-2001", "2026-10-05", "2026-10-07", DOS_PAQUETES)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoRastreo").value("EXP-2001"))
                .andExpect(jsonPath("$.pesoKg").value(19.75))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.paquetes.length()").value(2));

        Integer paquetesEnBd = jdbc.queryForObject(
                "SELECT COUNT(*) FROM PAQUETES p JOIN Envio e ON e.envio_id = p.envio_id "
                        + "WHERE e.codigo_rastreo = 'EXP-2001'", Integer.class);
        assertThat(paquetesEnBd).isEqualTo(2);

        mvc.perform(get("/api/envios/check-tracking/EXP-2001"))
                .andExpect(jsonPath("$.existe").value(true));
    }

    @Test
    void rechazaTrackingDuplicadoCon409() throws Exception {
        mvc.perform(post("/api/envios").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("EXP-1001", "2026-10-05", "2026-10-07", DOS_PAQUETES)))
                .andExpect(status().isConflict());
    }

    @Test
    void rechazaEntregaQueNoEsPosteriorAlDespacho() throws Exception {
        mvc.perform(post("/api/envios").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("EXP-2002", "2026-10-05", "2026-10-05", DOS_PAQUETES)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaEnvioSinPaquetes() throws Exception {
        mvc.perform(post("/api/envios").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("EXP-2003", "2026-10-05", "2026-10-07", "[]")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void siUnPaqueteFallaNoSeGuardaNiElEnvioNiLosDemasPaquetes() {
        // Se invoca el servicio directamente (sin la validación del controlador) con un
        // paquete cuyo peso no cabe en DECIMAL(5,2): el INSERT de ese paquete falla.
        EnvioRegistroDTO dto = new EnvioRegistroDTO(
                "EXP-2004", "Ana Rodriguez", "Cartago, Paraiso centro", new BigDecimal("9500.00"),
                LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 7), 1, 1,
                List.of(new PaqueteDTO(null, "Paquete correcto", new BigDecimal("10.00")),
                        new PaqueteDTO(null, "Paquete invalido", new BigDecimal("12345.67"))));

        assertThatThrownBy(() -> envioService.registrar(dto)).isInstanceOf(RuntimeException.class);

        // Rollback completo: ni el envío ni el paquete correcto quedaron en la base.
        assertThat(envioService.existeTracking("EXP-2004")).isFalse();
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM PAQUETES WHERE descripcion = 'Paquete correcto'", Integer.class))
                .isZero();
    }

    @Test
    void rechazaPesoTotalMayorALaCapacidadDelVehiculo() throws Exception {
        String cuerpo = cuerpo("EXP-2005", "2026-10-05", "2026-10-07", DOS_PAQUETES)
                .replace("\"vehiculoId\": 1", "\"vehiculoId\": 2");
        mvc.perform(post("/api/envios").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }
}
