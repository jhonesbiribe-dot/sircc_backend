// src/main/java/tfg/sircc_backend/service/impl/CasoServiceImpl.java
package tfg.sircc_backend.service.impl;

import tfg.sircc_backend.dto.request.CasoRequest;
import tfg.sircc_backend.dto.response.CasoResponse;
import tfg.sircc_backend.model.*;
import tfg.sircc_backend.model.enums.EstadoCaso;
import tfg.sircc_backend.model.enums.EstadoDenuncia;
import tfg.sircc_backend.model.enums.RolUsuario;
import tfg.sircc_backend.repository.*;
import tfg.sircc_backend.service.CasoService;
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
public class CasoServiceImpl implements CasoService {

    @Autowired
    private CasoRepository casoRepository;

    @Autowired
    private DenunciaRepository denunciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private EvidenciaRepository evidenciaRepository;

    @Autowired
    private AvanceInvestigacionRepository avanceRepository;

    @Autowired
    private CasoPersonaRepository casoPersonaRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String UPLOAD_DIR = "uploads/evidencias/";

    @Override
    @Transactional
    public CasoResponse crearCaso(CasoRequest request, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Caso caso = new Caso();
        caso.setNombre(request.getNombre());
        caso.setDescripcion(request.getDescripcion());
        caso.setEstado(request.getEstado() != null ? request.getEstado() : EstadoCaso.ABIERTO);
        caso.setFechaApertura(LocalDateTime.now());
        caso.setNumeroCaso(generarNumeroCaso());

        if (request.getInvestigadorPrincipalId() != null) {
            Usuario investigador = usuarioRepository.findById(request.getInvestigadorPrincipalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Investigador no encontrado"));
            caso.setInvestigadorPrincipal(investigador);
        } else {
            // Si no se especifica, asignar al usuario actual si es investigador
            if (usuario.getRol() == RolUsuario.INVESTIGADOR) {
                caso.setInvestigadorPrincipal(usuario);
            }
        }

        Caso savedCaso = casoRepository.save(caso);

        // Vincular denuncias
        if (request.getDenunciasIds() != null && !request.getDenunciasIds().isEmpty()) {
            for (Long denunciaId : request.getDenunciasIds()) {
                vincularDenuncia(savedCaso.getId(), denunciaId);
            }
        }

        // Vincular personas
        if (request.getPersonasIds() != null && !request.getPersonasIds().isEmpty()) {
            for (Long personaId : request.getPersonasIds()) {
                vincularPersona(savedCaso.getId(), personaId, "VINCULADO", usuarioId);
            }
        }

        return convertToResponse(savedCaso);
    }

    @Override
    public CasoResponse obtenerCaso(Long id) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));
        return convertToResponse(caso);
    }

    @Override
    @Transactional
    public CasoResponse actualizarCaso(Long id, CasoRequest request) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));

        caso.setNombre(request.getNombre());
        caso.setDescripcion(request.getDescripcion());

        if (request.getEstado() != null) {
            caso.setEstado(request.getEstado());
        }

        if (request.getInvestigadorPrincipalId() != null) {
            Usuario investigador = usuarioRepository.findById(request.getInvestigadorPrincipalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Investigador no encontrado"));
            caso.setInvestigadorPrincipal(investigador);
        }

        Caso updatedCaso = casoRepository.save(caso);
        return convertToResponse(updatedCaso);
    }

    @Override
    public Page<CasoResponse> listarCasosPorEstado(String estado, Pageable pageable) {
        EstadoCaso estadoEnum = EstadoCaso.valueOf(estado.toUpperCase());
        Page<Caso> casos = casoRepository.findByEstado(estadoEnum, pageable);
        return casos.map(this::convertToResponse);
    }

    @Override
    public Page<CasoResponse> listarCasosPorInvestigador(Long investigadorId, Pageable pageable) {
        Usuario investigador = usuarioRepository.findById(investigadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigador no encontrado"));
        Page<Caso> casos = casoRepository.findByInvestigadorPrincipal(investigador, pageable);
        return casos.map(this::convertToResponse);
    }

    @Override
    public Page<CasoResponse> listarTodos(Pageable pageable) {
        Page<Caso> casos = casoRepository.findAll(pageable);
        return casos.map(this::convertToResponse);
    }

    @Override
    @Transactional
    public CasoResponse vincularDenuncia(Long casoId, Long denunciaId) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));

        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada"));

        denuncia.setCaso(caso);
        denunciaRepository.save(denuncia);

        return convertToResponse(caso);
    }

    @Override
    @Transactional
    public CasoResponse desvincularDenuncia(Long casoId, Long denunciaId) {
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada"));

        denuncia.setCaso(null);
        denunciaRepository.save(denuncia);

        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));
        return convertToResponse(caso);
    }

    @Override
    @Transactional
    public CasoResponse vincularPersona(Long casoId, Long personaId, String rol, Long usuarioId) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));

        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada"));

        CasoPersona casoPersona = new CasoPersona();
        casoPersona.setCaso(caso);
        casoPersona.setPersona(persona);
        casoPersona.setRolEnCaso(rol);
        casoPersona.setFechaAsociacion(LocalDateTime.now());
        casoPersonaRepository.save(casoPersona);

        return convertToResponse(caso);
    }

    @Override
    @Transactional
    public CasoResponse desvincularPersona(Long casoId, Long personaId) {
        CasoPersona casoPersona = casoPersonaRepository.findByCasoIdAndPersonaId(casoId, personaId)
                .orElseThrow(() -> new ResourceNotFoundException("Relación no encontrada"));
        casoPersonaRepository.delete(casoPersona);

        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));
        return convertToResponse(caso);
    }

    @Override
    @Transactional
    public Evidencia agregarEvidenciaCaso(Long casoId, MultipartFile file, Long usuarioId, String descripcion) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            Evidencia evidencia = new Evidencia();
            evidencia.setTipo(determinarTipoEvidencia(file.getContentType()));
            evidencia.setUrl("/uploads/evidencias/" + filename);
            evidencia.setNombreArchivo(originalFilename);
            evidencia.setTamanoBytes(file.getSize());
            evidencia.setFormato(extension.replace(".", ""));
            evidencia.setDescripcion(descripcion);
            evidencia.setCaso(caso);
            evidencia.setUsuarioSubio(usuario);

            return evidenciaRepository.save(evidencia);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la evidencia: " + e.getMessage());
        }
    }

    @Override
    public List<Evidencia> listarEvidenciasCaso(Long casoId) {
        return evidenciaRepository.findByCasoId(casoId);
    }

    @Override
    @Transactional
    public void eliminarEvidenciaCaso(Long evidenciaId, Long usuarioId) {
        Evidencia evidencia = evidenciaRepository.findById(evidenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidencia no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!evidencia.getUsuarioSubio().getId().equals(usuarioId) && usuario.getRol() != RolUsuario.ADMIN) {
            throw new UnauthorizedException("No tiene permisos para eliminar esta evidencia");
        }

        try {
            Path filePath = Paths.get(evidencia.getUrl().replace("/uploads/", "uploads/"));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error al eliminar archivo: " + e.getMessage());
        }

        evidenciaRepository.delete(evidencia);
    }

    @Override
    @Transactional
    public CasoResponse registrarAvance(Long casoId, String descripcion, Long usuarioId) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        AvanceInvestigacion avance = new AvanceInvestigacion();
        avance.setCaso(caso);
        avance.setDescripcion(descripcion);
        avance.setUsuario(usuario);
        avanceRepository.save(avance);

        return convertToResponse(caso);
    }

    @Override
    public List<AvanceInvestigacion> listarAvances(Long casoId) {
        return avanceRepository.findByCasoIdOrderByFechaAvanceDesc(casoId);
    }

    @Override
    @Transactional
    public CasoResponse cerrarCaso(Long id, String resolucion, Long usuarioId) {
        Caso caso = casoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (caso.getInvestigadorPrincipal() == null || !caso.getInvestigadorPrincipal().getId().equals(usuarioId)) {
            if (usuario.getRol() != RolUsuario.ADMIN) {
                throw new UnauthorizedException("No tiene permisos para cerrar este caso");
            }
        }

        caso.setEstado(EstadoCaso.CERRADO);
        caso.setFechaCierre(LocalDateTime.now());
        Caso closedCaso = casoRepository.save(caso);

        // Cerrar denuncias asociadas
        if (caso.getDenuncias() != null) {
            for (Denuncia denuncia : caso.getDenuncias()) {
                denuncia.setEstado(EstadoDenuncia.RESUELTA);
                denunciaRepository.save(denuncia);
            }
        }

        // Registrar avance de cierre
        AvanceInvestigacion avance = new AvanceInvestigacion();
        avance.setCaso(caso);
        avance.setDescripcion("CASO CERRADO: " + resolucion);
        avance.setUsuario(usuario);
        avanceRepository.save(avance);

        return convertToResponse(closedCaso);
    }

    @Override
    @Transactional
    public void eliminarCaso(Long id) {
        if (!casoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Caso no encontrado");
        }
        casoRepository.deleteById(id);
    }

    // =====================================================
    // MÉTODOS PRIVADOS
    // =====================================================

    private String generarNumeroCaso() {
        String fecha = LocalDateTime.now().format(DATE_FORMATTER);
        long count = casoRepository.count() + 1;
        return String.format("CASO-%s-%05d", fecha, count);
    }

    private String determinarTipoEvidencia(String contentType) {
        if (contentType == null) return "OTRO";
        if (contentType.startsWith("image/")) return "IMAGEN";
        if (contentType.startsWith("video/")) return "VIDEO";
        if (contentType.startsWith("audio/")) return "AUDIO";
        if (contentType.startsWith("application/pdf")) return "DOCUMENTO";
        return "OTRO";
    }

    private CasoResponse convertToResponse(Caso caso) {
        CasoResponse response = new CasoResponse();
        response.setId(caso.getId());
        response.setNumeroCaso(caso.getNumeroCaso());
        response.setNombre(caso.getNombre());
        response.setDescripcion(caso.getDescripcion());
        response.setEstado(caso.getEstado());
        response.setFechaApertura(caso.getFechaApertura());
        response.setFechaCierre(caso.getFechaCierre());
        response.setCreatedAt(caso.getCreatedAt());

        // Investigador
        if (caso.getInvestigadorPrincipal() != null) {
            CasoResponse.InvestigadorInfo invInfo = new CasoResponse.InvestigadorInfo();
            invInfo.setId(caso.getInvestigadorPrincipal().getId());
            if (caso.getInvestigadorPrincipal().getPersona() != null) {
                invInfo.setNombreCompleto(
                        caso.getInvestigadorPrincipal().getPersona().getNombres() + " " +
                                caso.getInvestigadorPrincipal().getPersona().getApellidos()
                );
            }
            invInfo.setEmail(caso.getInvestigadorPrincipal().getEmail());
            response.setInvestigadorPrincipal(invInfo);
        }

        // Denuncias
        if (caso.getDenuncias() != null && !caso.getDenuncias().isEmpty()) {
            List<CasoResponse.DenunciaBasicaResponse> denuncias = caso.getDenuncias().stream()
                    .map(d -> {
                        CasoResponse.DenunciaBasicaResponse dResp = new CasoResponse.DenunciaBasicaResponse();
                        dResp.setId(d.getId());
                        dResp.setNumeroDenuncia(d.getNumeroDenuncia());
                        dResp.setEstado(d.getEstado().name());
                        dResp.setFechaHora(d.getFechaHora());
                        return dResp;
                    })
                    .collect(Collectors.toList());
            response.setDenuncias(denuncias);
            response.setTotalDenuncias(denuncias.size());
        }

        // Personas
        if (caso.getPersonas() != null && !caso.getPersonas().isEmpty()) {
            List<CasoResponse.PersonaBasicaResponse> personas = caso.getPersonas().stream()
                    .map(p -> {
                        CasoResponse.PersonaBasicaResponse pResp = new CasoResponse.PersonaBasicaResponse();
                        pResp.setId(p.getId());
                        pResp.setNombreCompleto(p.getNombres() + " " + p.getApellidos());
                        pResp.setDocumento(p.getDocumento());
                        casoPersonaRepository.findByCasoIdAndPersonaId(caso.getId(), p.getId())
                                .ifPresent(cp -> pResp.setRolEnCaso(cp.getRolEnCaso()));
                        return pResp;
                    })
                    .collect(Collectors.toList());
            response.setPersonas(personas);
        }

        return response;
    }
}