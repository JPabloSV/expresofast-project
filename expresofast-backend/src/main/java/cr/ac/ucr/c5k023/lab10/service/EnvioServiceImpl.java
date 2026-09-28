package cr.ac.ucr.c5k023.lab10.service;

import cr.ac.ucr.c5k023.lab10.dto.CrearEnvioDTO;
import cr.ac.ucr.c5k023.lab10.dto.EnvioDTO;
import cr.ac.ucr.c5k023.lab10.exception.EnvioNoEncontradoException;
import cr.ac.ucr.c5k023.lab10.model.Envio;
import cr.ac.ucr.c5k023.lab10.repository.EnvioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class EnvioServiceImpl implements EnvioService {

    private static final String ESTADO_INICIAL = "PENDIENTE";

    private final EnvioRepository envioRepository;

    public EnvioServiceImpl(EnvioRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnvioDTO> obtenerTodos() {
        return envioRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::aDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnvioDTO> obtenerPorEstado(String estado) {
        return envioRepository.buscarPorEstado(estado)
                .stream()
                .map(this::aDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EnvioDTO buscarPorCodigoRastreo(String codigo) {
        String normalizado = codigo.trim().toUpperCase(Locale.ROOT);
        return envioRepository.findByCodigoRastreo(normalizado)
                .map(this::aDTO)
                .orElseThrow(() -> new EnvioNoEncontradoException(
                        "No existe un envío con el código " + normalizado));
    }

    @Override
    @Transactional
    public EnvioDTO registrar(CrearEnvioDTO dto) {
        Envio envio = new Envio();
        envio.setCodigoRastreo(generarCodigoRastreo());
        envio.setDestinatario(dto.destinatario().trim());
        envio.setDireccionDestino(dto.direccionDestino().trim());
        envio.setMontoFlete(dto.montoFlete());
        envio.setEstado(ESTADO_INICIAL);
        envio.setFechaCreacion(LocalDateTime.now());
        return aDTO(envioRepository.save(envio));
    }

    @Override
    @Transactional
    public EnvioDTO actualizarEstado(Long id, String nuevoEstado) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new EnvioNoEncontradoException(
                        "No existe un envío con el id " + id));
        envio.setEstado(nuevoEstado);
        return aDTO(envioRepository.save(envio));
    }

    private String generarCodigoRastreo() {
        String codigo;
        do {
            int numero = ThreadLocalRandom.current().nextInt(1000, 10000);
            codigo = "EXP-" + Year.now().getValue() + "-" + numero;
        } while (envioRepository.existsByCodigoRastreo(codigo));
        return codigo;
    }

    private EnvioDTO aDTO(Envio e) {
        return new EnvioDTO(
                e.getId(),
                e.getCodigoRastreo(),
                e.getDestinatario(),
                e.getDireccionDestino(),
                e.getMontoFlete(),
                e.getEstado(),
                e.getFechaCreacion());
    }
}