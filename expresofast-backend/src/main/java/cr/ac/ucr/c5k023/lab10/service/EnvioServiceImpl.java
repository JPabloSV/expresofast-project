package cr.ac.ucr.c5k023.lab10.service;

import cr.ac.ucr.c5k023.lab10.dto.ConductorDTO;
import cr.ac.ucr.c5k023.lab10.dto.EnvioDTO;
import cr.ac.ucr.c5k023.lab10.dto.EnvioRegistroDTO;
import cr.ac.ucr.c5k023.lab10.dto.PaqueteDTO;
import cr.ac.ucr.c5k023.lab10.dto.VehiculoDTO;
import cr.ac.ucr.c5k023.lab10.exception.EnvioNoEncontradoException;
import cr.ac.ucr.c5k023.lab10.exception.ReglaNegocioException;
import cr.ac.ucr.c5k023.lab10.exception.TrackingDuplicadoException;
import cr.ac.ucr.c5k023.lab10.model.Conductor;
import cr.ac.ucr.c5k023.lab10.model.Envio;
import cr.ac.ucr.c5k023.lab10.model.Paquete;
import cr.ac.ucr.c5k023.lab10.model.Vehiculo;
import cr.ac.ucr.c5k023.lab10.repository.ConductorRepository;
import cr.ac.ucr.c5k023.lab10.repository.EnvioRepository;
import cr.ac.ucr.c5k023.lab10.repository.VehiculoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class EnvioServiceImpl implements EnvioService {

    private static final String ESTADO_INICIAL = "PENDIENTE";
    private static final Sort MAS_RECIENTES = Sort.by(Sort.Direction.DESC, "id");

    private final EnvioRepository envioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;

    public EnvioServiceImpl(EnvioRepository envioRepository,
                            VehiculoRepository vehiculoRepository,
                            ConductorRepository conductorRepository) {
        this.envioRepository = envioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnvioDTO> obtenerTodos() {
        return envioRepository.findAllBy(MAS_RECIENTES).stream().map(this::aDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnvioDTO> obtenerPorEstado(String estado) {
        return envioRepository.findByEstado(estado, MAS_RECIENTES).stream().map(this::aDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EnvioDTO buscarPorCodigoRastreo(String codigo) {
        String normalizado = normalizar(codigo);
        return envioRepository.findByCodigoRastreo(normalizado)
                .map(this::aDTO)
                .orElseThrow(() -> new EnvioNoEncontradoException(
                        "No existe un envío con el código " + normalizado));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeTracking(String numeroTracking) {
        return envioRepository.existsByCodigoRastreo(normalizar(numeroTracking));
    }

    /**
     * Registra el envío y TODOS sus paquetes en una única transacción.
     * Si cualquier INSERT falla (por ejemplo, un paquete inválido), Spring hace
     * rollback y no queda en la base ni el envío ni ninguno de sus paquetes.
     */
    @Override
    @Transactional
    public EnvioDTO registrar(EnvioRegistroDTO dto) {
        String tracking = normalizar(dto.numeroTracking());

        // 1. Reglas de negocio (el frontend ya las valida, pero el backend nunca confía en el cliente)
        if (envioRepository.existsByCodigoRastreo(tracking)) {
            throw new TrackingDuplicadoException(tracking);
        }
        if (!dto.fechaEntregaEstimada().isAfter(dto.fechaDespacho())) {
            throw new ReglaNegocioException(
                    "La fecha de entrega estimada debe ser posterior a la fecha de despacho");
        }
        Vehiculo vehiculo = vehiculoRepository.findById(dto.vehiculoId())
                .orElseThrow(() -> new ReglaNegocioException("No existe el vehículo " + dto.vehiculoId()));
        Conductor conductor = conductorRepository.findById(dto.conductorId())
                .orElseThrow(() -> new ReglaNegocioException("No existe el conductor " + dto.conductorId()));
        if (!Boolean.TRUE.equals(conductor.getActivo())) {
            throw new ReglaNegocioException("El conductor seleccionado no está activo");
        }

        BigDecimal pesoTotal = dto.paquetes().stream()
                .map(PaqueteDTO::pesoKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (pesoTotal.compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new ReglaNegocioException("El peso total (" + pesoTotal + " kg) supera la capacidad del vehículo "
                    + vehiculo.getPlaca() + " (" + vehiculo.getCapacidadKg() + " kg)");
        }

        // 2. Construcción del agregado Envio -> Paquetes
        LocalDateTime ahora = LocalDateTime.now();
        Envio envio = new Envio();
        envio.setCodigoRastreo(tracking);
        envio.setDestinatario(dto.destinatario().trim());
        envio.setDireccionDestino(dto.direccionDestino().trim());
        envio.setCosto(dto.montoFlete());
        envio.setPesoKg(pesoTotal);
        envio.setEstado(ESTADO_INICIAL);
        envio.setVehiculo(vehiculo);
        envio.setConductor(conductor);
        envio.setFechaDespacho(dto.fechaDespacho());
        envio.setFechaEntregaEstimada(dto.fechaEntregaEstimada());
        envio.setFechaCreacion(ahora);
        envio.setFechaModificacion(ahora);

        for (PaqueteDTO p : dto.paquetes()) {
            envio.agregarPaquete(new Paquete(p.descripcion().trim(), p.pesoKg()));
        }

        // 3. Un solo save: CascadeType.ALL inserta el envío y luego cada paquete con su envio_id.
        //    saveAndFlush fuerza los INSERT aquí, dentro de la transacción, para que cualquier
        //    error de la base se produzca antes del commit y provoque el rollback completo.
        return aDTO(envioRepository.saveAndFlush(envio));
    }

    @Override
    @Transactional
    public EnvioDTO actualizarEstado(Integer id, String nuevoEstado) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new EnvioNoEncontradoException("No existe un envío con el id " + id));
        envio.setEstado(nuevoEstado);
        envio.setFechaModificacion(LocalDateTime.now());
        return aDTO(envio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoDTO> obtenerVehiculos() {
        return vehiculoRepository.findAll(Sort.by("placa")).stream()
                .map(v -> new VehiculoDTO(v.getId(), v.getPlaca(), v.getCapacidadKg(), v.getEstado()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConductorDTO> obtenerConductoresActivos() {
        return conductorRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(c -> new ConductorDTO(c.getId(), c.getNombreCompleto()))
                .toList();
    }

    private String normalizar(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private EnvioDTO aDTO(Envio e) {
        List<PaqueteDTO> paquetes = e.getPaquetes().stream()
                .map(p -> new PaqueteDTO(p.getId(), p.getDescripcion(), p.getPesoKg()))
                .toList();
        return new EnvioDTO(
                e.getId(),
                e.getCodigoRastreo(),
                e.getDestinatario(),
                e.getDireccionDestino(),
                e.getCosto(),
                e.getPesoKg(),
                e.getEstado(),
                e.getFechaCreacion(),
                e.getFechaDespacho(),
                e.getFechaEntregaEstimada(),
                e.getVehiculo().getId(),
                e.getVehiculo().getPlaca(),
                e.getConductor().getId(),
                e.getConductor().getNombreCompleto(),
                paquetes);
    }
}
