// src/main/java/tfg/sircc_backend/service/impl/DenunciaServiceImpl.java
package tfg.sircc_backend.service.impl;

import tfg.sircc_backend.dto.request.DenunciaRequest;
import tfg.sircc_backend.dto.response.DenunciaResponse;
import tfg.sircc_backend.model.*;
import tfg.sircc_backend.model.enums.EstadoDenuncia;
import tfg.sircc_backend.model.enums.RolUsuario;
import tfg.sircc_backend.repository.*;
import tfg.sircc_backend.service.DenunciaService;
import tfg.sircc_backend.exception.ResourceNotFoundException;
import tfg.sircc_backend.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DenunciaServiceImpl implements DenunciaService {

    @Autowired
    private DenunciaRepository denunciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private DelitoRepository delitoRepository;

    @Autowired
    private EvidenciaRepository evidenciaRepository;

    @Autowired
    private SeguimientoDenunciaRepository seguimientoRepository;

    @Autowired
    private CasoRepository casoRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String UPLOAD_DIR = "uploads/evidencias/";

    // =====================================================
    // CRUD BÁSICO
    // =====================================================

    @Override
    @Transactional
    public DenunciaResponse crearDenuncia(DenunciaRequest request, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Denuncia denuncia = new Denuncia();
        denuncia.setDescripcionHechos(request.getDescripcionHechos());
        denuncia.setModusOperandi(request.getModusOperandi());

        // ✅ Convertir String a LocalDateTime
        if (request.getFechaHora() != null) {
            try {
                denuncia.setFechaHora(LocalDateTime.parse(request.getFechaHora()));
            } catch (Exception e) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    denuncia.setFechaHora(LocalDateTime.parse(request.getFechaHora(), formatter));
                } catch (Exception ex) {
                    denuncia.setFechaHora(LocalDateTime.now());
                    System.err.println("⚠️ Error parseando fecha: " + request.getFechaHora() + ", usando fecha actual");
                }
            }
        }

        denuncia.setUbicacionTexto(request.getUbicacionTexto());
        denuncia.setLatitud(request.getLatitud());
        denuncia.setLongitud(request.getLongitud());
        denuncia.setEsAnonima(request.getEsAnonima());
        denuncia.setUsuario(usuario);
        denuncia.setEstado(EstadoDenuncia.PENDIENTE);
        denuncia.setNumeroDenuncia(generarNumeroDenuncia());

        if (request.getDelitosIds() != null && !request.getDelitosIds().isEmpty()) {
            List<Delito> delitos = delitoRepository.findAllById(request.getDelitosIds());
            denuncia.setDelitos(delitos);
        }

        Denuncia savedDenuncia = denunciaRepository.save(denuncia);
        registrarSeguimiento(savedDenuncia, null, EstadoDenuncia.PENDIENTE, "Denuncia creada", usuario);

        return convertToResponse(savedDenuncia);
    }

    @Override
    public DenunciaResponse obtenerDenuncia(Long id) {
        Denuncia denuncia = denunciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada"));
        return convertToResponse(denuncia);
    }

    @Override
    public Page<DenunciaResponse> listarDenunciasPorEstado(String estado, Pageable pageable) {
        EstadoDenuncia estadoEnum = EstadoDenuncia.valueOf(estado.toUpperCase());
        Page<Denuncia> denuncias = denunciaRepository.findByEstado(estadoEnum, pageable);
        return denuncias.map(this::convertToResponse);
    }

    @Override
    public Page<DenunciaResponse> listarDenunciasPorUsuario(Long usuarioId, Pageable pageable) {
        Page<Denuncia> denuncias = denunciaRepository.findByUsuarioId(usuarioId, pageable);
        return denuncias.map(this::convertToResponse);
    }

    @Override
    public Page<DenunciaResponse> listarDenunciasPendientes(Pageable pageable) {
        Page<Denuncia> denuncias = denunciaRepository.findByEstado(EstadoDenuncia.PENDIENTE, pageable);
        return denuncias.map(this::convertToResponse);
    }

    @Override
    public boolean denunciaPerteneceAUsuario(Long denunciaId, Long usuarioId) {
        return denunciaRepository.existsByIdAndUsuarioId(denunciaId, usuarioId);
    }

    @Override
    @Transactional
    public DenunciaResponse actualizarEstado(Long id, String nuevoEstado, Long usuarioId, String observacion) {
        Denuncia denuncia = denunciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        EstadoDenuncia estadoActual = denuncia.getEstado();
        EstadoDenuncia nuevoEstadoEnum = EstadoDenuncia.valueOf(nuevoEstado.toUpperCase());

        validarTransicionEstado(estadoActual, nuevoEstadoEnum, usuario);

        denuncia.setEstado(nuevoEstadoEnum);
        Denuncia updatedDenuncia = denunciaRepository.save(denuncia);

        registrarSeguimiento(updatedDenuncia, estadoActual, nuevoEstadoEnum, observacion, usuario);

        return convertToResponse(updatedDenuncia);
    }

    // =====================================================
    // RF-06: VALIDACIÓN DE DENUNCIAS
    // =====================================================

    @Override
    @Transactional
    public DenunciaResponse validarDenuncia(Long denunciaId, Long usuarioId, String observacion) {
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() != RolUsuario.ADMINISTRATIVO && usuario.getRol() != RolUsuario.ADMIN) {
            throw new UnauthorizedException("Solo el personal administrativo puede validar denuncias");
        }

        if (denuncia.getEstado() != EstadoDenuncia.PENDIENTE) {
            throw new IllegalStateException("Solo las denuncias en estado PENDIENTE pueden ser validadas");
        }

        denuncia.setEstado(EstadoDenuncia.VALIDADA);
        Denuncia updatedDenuncia = denunciaRepository.save(denuncia);

        String obs = observacion != null ? observacion : "Denuncia validada por administrativo";
        registrarSeguimiento(updatedDenuncia, EstadoDenuncia.PENDIENTE, EstadoDenuncia.VALIDADA, obs, usuario);

        return convertToResponse(updatedDenuncia);
    }

    @Override
    @Transactional
    public DenunciaResponse rechazarDenuncia(Long denunciaId, Long usuarioId, String motivo) {
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() != RolUsuario.ADMINISTRATIVO && usuario.getRol() != RolUsuario.ADMIN) {
            throw new UnauthorizedException("Solo el personal administrativo puede rechazar denuncias");
        }

        if (denuncia.getEstado() != EstadoDenuncia.PENDIENTE) {
            throw new IllegalStateException("Solo las denuncias en estado PENDIENTE pueden ser rechazadas");
        }

        denuncia.setEstado(EstadoDenuncia.ARCHIVADA);
        Denuncia updatedDenuncia = denunciaRepository.save(denuncia);

        String obs = "Denuncia rechazada: " + (motivo != null ? motivo : "No especificado");
        registrarSeguimiento(updatedDenuncia, EstadoDenuncia.PENDIENTE, EstadoDenuncia.ARCHIVADA, obs, usuario);

        return convertToResponse(updatedDenuncia);
    }

    @Override
    @Transactional
    public DenunciaResponse clasificarDelitos(Long denunciaId, List<Long> delitosIds, Long usuarioId) {
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() != RolUsuario.ADMINISTRATIVO && usuario.getRol() != RolUsuario.ADMIN) {
            throw new UnauthorizedException("Solo el personal administrativo puede clasificar delitos");
        }

        if (denuncia.getEstado() != EstadoDenuncia.VALIDADA) {
            throw new IllegalStateException("La denuncia debe estar validada para clasificar delitos");
        }

        List<Delito> delitos = delitoRepository.findAllById(delitosIds);
        if (delitos.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un delito");
        }

        denuncia.setDelitos(delitos);
        Denuncia updatedDenuncia = denunciaRepository.save(denuncia);

        String delitosNombres = delitos.stream().map(Delito::getNombre).collect(Collectors.joining(", "));
        registrarSeguimiento(updatedDenuncia, EstadoDenuncia.VALIDADA, EstadoDenuncia.VALIDADA,
                "Delitos clasificados: " + delitosNombres, usuario);

        return convertToResponse(updatedDenuncia);
    }

    // =====================================================
    // RF-07: ASIGNACIÓN DE INVESTIGADORES
    // =====================================================

    @Override
    public List<Usuario> listarInvestigadoresDisponibles() {
        return usuarioRepository.findByRolAndEstado(RolUsuario.INVESTIGADOR, true);
    }

    @Override
    @Transactional
    public DenunciaResponse asignarInvestigador(Long denunciaId, Long investigadorId, Long usuarioId, String observacion) {
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Usuario investigador = usuarioRepository.findById(investigadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigador no encontrado"));

        if (usuario.getRol() != RolUsuario.ADMINISTRATIVO && usuario.getRol() != RolUsuario.ADMIN) {
            throw new UnauthorizedException("Solo el personal administrativo puede asignar denuncias");
        }

        if (denuncia.getEstado() != EstadoDenuncia.VALIDADA) {
            throw new IllegalStateException("La denuncia debe estar validada para asignar a un investigador");
        }

        if (!investigador.getEstado() || investigador.getRol() != RolUsuario.INVESTIGADOR) {
            throw new IllegalArgumentException("El usuario seleccionado no es un investigador activo");
        }

        denuncia.setInvestigador(investigador);
        denuncia.setEstado(EstadoDenuncia.INVESTIGACION);
        Denuncia updatedDenuncia = denunciaRepository.save(denuncia);

        String obs = observacion != null ? observacion :
                "Denuncia asignada al investigador: " + investigador.getEmail();
        registrarSeguimiento(updatedDenuncia, EstadoDenuncia.VALIDADA, EstadoDenuncia.INVESTIGACION, obs, usuario);

        return convertToResponse(updatedDenuncia);
    }

    @Override
    public Page<DenunciaResponse> listarDenunciasPorInvestigador(Long investigadorId, Pageable pageable) {
        Page<Denuncia> denuncias = denunciaRepository.findByInvestigadorId(investigadorId, pageable);
        return denuncias.map(this::convertToResponse);
    }

    // =====================================================
    // RF-09: EVIDENCIAS
    // =====================================================

    @Override
    @Transactional
    public Evidencia agregarEvidencia(Long denunciaId, MultipartFile file, Long usuarioId, String descripcion) {
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (denuncia.getEstado() == EstadoDenuncia.ARCHIVADA || denuncia.getEstado() == EstadoDenuncia.RESUELTA) {
            throw new IllegalStateException("No se pueden agregar evidencias a denuncias archivadas o resueltas");
        }

        try {
            // Crear directorio si no existe
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generar nombre único para el archivo
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // Guardar archivo
            Files.copy(file.getInputStream(), filePath);

            // Crear registro de evidencia
            Evidencia evidencia = new Evidencia();
            evidencia.setTipo(determinarTipoEvidencia(file.getContentType()));
            evidencia.setUrl("/uploads/evidencias/" + filename);
            evidencia.setNombreArchivo(originalFilename);
            evidencia.setTamanoBytes(file.getSize());
            evidencia.setFormato(extension.replace(".", ""));
            evidencia.setDescripcion(descripcion);
            evidencia.setDenuncia(denuncia);
            evidencia.setUsuarioSubio(usuario);

            return evidenciaRepository.save(evidencia);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la evidencia: " + e.getMessage());
        }
    }

    @Override
    public List<Evidencia> listarEvidenciasPorDenuncia(Long denunciaId) {
        return evidenciaRepository.findByDenunciaId(denunciaId);
    }

    @Override
    @Transactional
    public void eliminarEvidencia(Long evidenciaId, Long usuarioId) {
        Evidencia evidencia = evidenciaRepository.findById(evidenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidencia no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Verificar permisos (solo el que subió o ADMIN)
        if (!evidencia.getUsuarioSubio().getId().equals(usuarioId) && usuario.getRol() != RolUsuario.ADMIN) {
            throw new UnauthorizedException("No tiene permisos para eliminar esta evidencia");
        }

        // Eliminar archivo físico
        try {
            Path filePath = Paths.get(evidencia.getUrl().replace("/uploads/", "uploads/"));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error al eliminar archivo: " + e.getMessage());
        }

        evidenciaRepository.delete(evidencia);
    }

    // =====================================================
    // RF-09: RESOLVER Y ARCHIVAR DENUNCIA (INVESTIGADOR)
    // =====================================================

    @Override
    @Transactional
    public DenunciaResponse resolverDenuncia(Long denunciaId, Long usuarioId, String resolucion) {
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (denuncia.getInvestigador() == null || !denuncia.getInvestigador().getId().equals(usuarioId)) {
            if (usuario.getRol() != RolUsuario.ADMIN) {
                throw new UnauthorizedException("No tiene permisos para resolver esta denuncia");
            }
        }

        if (denuncia.getEstado() != EstadoDenuncia.INVESTIGACION) {
            throw new IllegalStateException("Solo las denuncias en INVESTIGACION pueden ser resueltas");
        }

        denuncia.setEstado(EstadoDenuncia.RESUELTA);
        Denuncia updatedDenuncia = denunciaRepository.save(denuncia);

        String obs = resolucion != null ? "Denuncia resuelta: " + resolucion : "Denuncia resuelta";
        registrarSeguimiento(updatedDenuncia, EstadoDenuncia.INVESTIGACION, EstadoDenuncia.RESUELTA, obs, usuario);

        return convertToResponse(updatedDenuncia);
    }

    @Override
    @Transactional
    public DenunciaResponse archivarDenuncia(Long denunciaId, Long usuarioId, String motivo) {
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (denuncia.getInvestigador() == null || !denuncia.getInvestigador().getId().equals(usuarioId)) {
            if (usuario.getRol() != RolUsuario.ADMIN) {
                throw new UnauthorizedException("No tiene permisos para archivar esta denuncia");
            }
        }

        if (denuncia.getEstado() != EstadoDenuncia.INVESTIGACION) {
            throw new IllegalStateException("Solo las denuncias en INVESTIGACION pueden ser archivadas");
        }

        denuncia.setEstado(EstadoDenuncia.ARCHIVADA);
        Denuncia updatedDenuncia = denunciaRepository.save(denuncia);

        String obs = motivo != null ? "Denuncia archivada: " + motivo : "Denuncia archivada sin resolución";
        registrarSeguimiento(updatedDenuncia, EstadoDenuncia.INVESTIGACION, EstadoDenuncia.ARCHIVADA, obs, usuario);

        return convertToResponse(updatedDenuncia);
    }

    @Override
    public Page<DenunciaResponse> listarDenunciasAsignadas(Long investigadorId, String estado, Pageable pageable) {
        Page<Denuncia> denuncias;
        if (estado != null && !estado.isEmpty()) {
            EstadoDenuncia estadoEnum = EstadoDenuncia.valueOf(estado.toUpperCase());
            denuncias = denunciaRepository.findByInvestigadorIdAndEstado(investigadorId, estadoEnum, pageable);
        } else {
            denuncias = denunciaRepository.findByInvestigadorId(investigadorId, pageable);
        }
        return denuncias.map(this::convertToResponse);
    }

    // =====================================================
    // MÉTODOS PRIVADOS
    // =====================================================

    private String generarNumeroDenuncia() {
        String fecha = LocalDateTime.now().format(DATE_FORMATTER);
        long count = denunciaRepository.count() + 1;
        return String.format("DEN-%s-%05d", fecha, count);
    }

    private void validarTransicionEstado(EstadoDenuncia actual, EstadoDenuncia nuevo, Usuario usuario) {
        RolUsuario rol = usuario.getRol();

        switch (actual) {
            case PENDIENTE:
                if (nuevo == EstadoDenuncia.VALIDADA || nuevo == EstadoDenuncia.ARCHIVADA) {
                    if (rol != RolUsuario.ADMINISTRATIVO && rol != RolUsuario.ADMIN) {
                        throw new UnauthorizedException("Solo personal administrativo puede validar o rechazar denuncias pendientes");
                    }
                }
                break;
            case VALIDADA:
                if (nuevo == EstadoDenuncia.INVESTIGACION) {
                    if (rol != RolUsuario.ADMINISTRATIVO && rol != RolUsuario.ADMIN) {
                        throw new UnauthorizedException("Solo personal administrativo puede asignar denuncias a investigación");
                    }
                }
                break;
            case INVESTIGACION:
                if (nuevo == EstadoDenuncia.RESUELTA || nuevo == EstadoDenuncia.ARCHIVADA) {
                    if (rol != RolUsuario.INVESTIGADOR && rol != RolUsuario.ANALISTA && rol != RolUsuario.ADMIN) {
                        throw new UnauthorizedException("Solo investigadores pueden resolver o archivar denuncias en investigación");
                    }
                }
                break;
            default:
                break;
        }
    }

    private void registrarSeguimiento(Denuncia denuncia, EstadoDenuncia estadoAnterior,
                                      EstadoDenuncia estadoNuevo, String observacion, Usuario usuario) {
        SeguimientoDenuncia seguimiento = new SeguimientoDenuncia();
        seguimiento.setDenuncia(denuncia);
        seguimiento.setEstadoAnterior(estadoAnterior != null ? estadoAnterior.name() : null);
        seguimiento.setEstadoNuevo(estadoNuevo.name());
        seguimiento.setObservacion(observacion);
        seguimiento.setUsuarioCambio(usuario);
        seguimiento.setFechaCambio(LocalDateTime.now());
        seguimientoRepository.save(seguimiento);
    }

    private String determinarTipoEvidencia(String contentType) {
        if (contentType == null) return "OTRO";
        if (contentType.startsWith("image/")) return "IMAGEN";
        if (contentType.startsWith("video/")) return "VIDEO";
        if (contentType.startsWith("audio/")) return "AUDIO";
        if (contentType.startsWith("application/pdf")) return "DOCUMENTO";
        return "OTRO";
    }

    private DenunciaResponse convertToResponse(Denuncia denuncia) {
        DenunciaResponse response = new DenunciaResponse();
        response.setId(denuncia.getId());
        response.setNumeroDenuncia(denuncia.getNumeroDenuncia());
        response.setDescripcionHechos(denuncia.getDescripcionHechos());
        response.setModusOperandi(denuncia.getModusOperandi());
        response.setEstado(denuncia.getEstado().name());
        response.setFechaHora(denuncia.getFechaHora());
        response.setUbicacionTexto(denuncia.getUbicacionTexto());
        if (denuncia.getLatitud() != null) {
            response.setLatitud(denuncia.getLatitud().doubleValue());
        }
        if (denuncia.getLongitud() != null) {
            response.setLongitud(denuncia.getLongitud().doubleValue());
        }
        response.setCreatedAt(denuncia.getCreatedAt());
        response.setEsAnonima(denuncia.getEsAnonima());

        if (denuncia.getDelitos() != null) {
            response.setDelitos(denuncia.getDelitos().stream()
                    .map(Delito::getNombre)
                    .collect(Collectors.toList()));
        }

        if (denuncia.getEvidencias() != null) {
            response.setEvidencias(denuncia.getEvidencias().stream()
                    .map(ev -> {
                        DenunciaResponse.EvidenciaResponse evResponse = new DenunciaResponse.EvidenciaResponse();
                        evResponse.setId(ev.getId());
                        evResponse.setTipo(ev.getTipo());
                        evResponse.setUrl(ev.getUrl());
                        evResponse.setNombreArchivo(ev.getNombreArchivo());
                        evResponse.setDescripcion(ev.getDescripcion());
                        evResponse.setFechaSubida(ev.getFechaSubida());
                        return evResponse;
                    })
                    .collect(Collectors.toList()));
        }

        if (denuncia.getInvestigador() != null) {
            response.setInvestigadorId(denuncia.getInvestigador().getId());
            response.setInvestigadorNombre(denuncia.getInvestigador().getEmail());
        }

        return response;
    }
}