// src/main/java/tfg/sircc_backend/controller/DenunciaController.java
package tfg.sircc_backend.controller;


// Agregar estos imports al inicio del archivo
import tfg.sircc_backend.model.Evidencia;
import tfg.sircc_backend.model.enums.EstadoCaso;
import java.util.stream.Collectors;
import tfg.sircc_backend.dto.request.DenunciaRequest;
import tfg.sircc_backend.dto.response.DenunciaResponse;
import tfg.sircc_backend.model.Usuario;
import tfg.sircc_backend.model.enums.EstadoDenuncia;
import tfg.sircc_backend.model.enums.RolUsuario;
import tfg.sircc_backend.repository.UsuarioRepository;
import tfg.sircc_backend.repository.DenunciaRepository;
import tfg.sircc_backend.repository.CasoRepository;
import tfg.sircc_backend.service.DenunciaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:4200", "https://sircc.vercel.app"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/denuncias")
public class DenunciaController {

    @Autowired
    private DenunciaService denunciaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario getUsuarioFromSession(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // =====================================================
    // RF-01: Crear denuncia
    // =====================================================
    @PostMapping
    @PreAuthorize("hasAnyRole('CIUDADANO', 'ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<DenunciaResponse> crearDenuncia(
            @Valid @RequestBody DenunciaRequest request,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        DenunciaResponse response = denunciaService.crearDenuncia(request, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =====================================================
    // RF-09: Crear denuncia con evidencias (multipart/form-data)
    // =====================================================
    @PostMapping("/con-evidencias")
    @PreAuthorize("hasAnyRole('CIUDADANO', 'ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<DenunciaResponse> crearDenunciaConEvidencias(
            @RequestPart("denuncia") String denunciaJson,
            @RequestPart(value = "evidencias", required = false) List<MultipartFile> evidencias,
            HttpSession session) throws Exception {

        Usuario usuario = getUsuarioFromSession(session);

        // Parsear el JSON de la denuncia
        ObjectMapper mapper = new ObjectMapper();
        DenunciaRequest request = mapper.readValue(denunciaJson, DenunciaRequest.class);

        DenunciaResponse response = denunciaService.crearDenuncia(request, usuario.getId());

        // Guardar evidencias
        if (evidencias != null && !evidencias.isEmpty()) {
            for (MultipartFile file : evidencias) {
                denunciaService.agregarEvidencia(response.getId(), file, usuario.getId(), null);
            }
            // Recargar la denuncia para incluir las evidencias
            response = denunciaService.obtenerDenuncia(response.getId());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =====================================================
    // RF-06: Listar denuncias pendientes (para validación)
    // =====================================================
    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<Page<DenunciaResponse>> listarDenunciasPendientes(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {

        getUsuarioFromSession(session);
        Page<DenunciaResponse> denuncias = denunciaService.listarDenunciasPendientes(pageable);
        return ResponseEntity.ok(denuncias);
    }

    // =====================================================
    // RF-06: Validar denuncia
    // =====================================================
    @PatchMapping("/{id}/validar")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<DenunciaResponse> validarDenuncia(
            @PathVariable Long id,
            @RequestParam(required = false) String observacion,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        DenunciaResponse response = denunciaService.validarDenuncia(
                id,
                usuario.getId(),
                observacion != null ? observacion : "Denuncia validada por administrativo"
        );
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // RF-06: Rechazar denuncia
    // =====================================================
    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<DenunciaResponse> rechazarDenuncia(
            @PathVariable Long id,
            @RequestParam String motivo,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        DenunciaResponse response = denunciaService.rechazarDenuncia(
                id,
                usuario.getId(),
                motivo
        );
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // RF-06: Clasificar delitos
    // =====================================================
    @PatchMapping("/{id}/clasificar")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<DenunciaResponse> clasificarDelitos(
            @PathVariable Long id,
            @RequestBody List<Long> delitosIds,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        DenunciaResponse response = denunciaService.clasificarDelitos(id, delitosIds, usuario.getId());
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // RF-07: Listar investigadores disponibles
    // =====================================================


    // =====================================================
    // RF-07: Asignar denuncia a investigador
    // =====================================================
    @PatchMapping("/{id}/asignar")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<DenunciaResponse> asignarInvestigador(
            @PathVariable Long id,
            @RequestParam Long investigadorId,
            @RequestParam(required = false) String observacion,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        DenunciaResponse response = denunciaService.asignarInvestigador(
                id,
                investigadorId,
                usuario.getId(),
                observacion != null ? observacion : "Denuncia asignada a investigador"
        );
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // RF-07: Listar denuncias asignadas a un investigador
    // =====================================================
    @GetMapping("/investigador/{investigadorId}")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<Page<DenunciaResponse>> listarDenunciasPorInvestigador(
            @PathVariable Long investigadorId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {

        getUsuarioFromSession(session);
        Page<DenunciaResponse> denuncias = denunciaService.listarDenunciasPorInvestigador(investigadorId, pageable);
        return ResponseEntity.ok(denuncias);
    }

    // =====================================================
    // RF-09: Agregar evidencia a una denuncia
    // =====================================================
    @PostMapping("/{id}/evidencias")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<?> agregarEvidencia(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String descripcion,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        Evidencia evidencia = denunciaService.agregarEvidencia(id, file, usuario.getId(), descripcion);
        return ResponseEntity.status(HttpStatus.CREATED).body(evidencia);
    }

    // =====================================================
    // RF-09: Listar evidencias de una denuncia
    // =====================================================
    @GetMapping("/{id}/evidencias")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'INVESTIGADOR', 'ANALISTA', 'ADMIN')")
    public ResponseEntity<List<Evidencia>> listarEvidencias(
            @PathVariable Long id,
            HttpSession session) {

        getUsuarioFromSession(session);
        List<Evidencia> evidencias = denunciaService.listarEvidenciasPorDenuncia(id);
        return ResponseEntity.ok(evidencias);
    }

    // =====================================================
    // RF-09: Eliminar evidencia
    // =====================================================
    @DeleteMapping("/evidencias/{evidenciaId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<Void> eliminarEvidencia(
            @PathVariable Long evidenciaId,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        denunciaService.eliminarEvidencia(evidenciaId, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // Métodos básicos CRUD
    // =====================================================

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'INVESTIGADOR', 'ANALISTA', 'ADMIN')")
    public ResponseEntity<Page<DenunciaResponse>> listarDenunciasPorEstado(
            @PathVariable String estado,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {

        getUsuarioFromSession(session);
        Page<DenunciaResponse> denuncias = denunciaService.listarDenunciasPorEstado(estado, pageable);
        return ResponseEntity.ok(denuncias);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CIUDADANO', 'ADMINISTRATIVO', 'INVESTIGADOR', 'ANALISTA', 'ADMIN')")
    public ResponseEntity<DenunciaResponse> obtenerDenuncia(
            @PathVariable Long id,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        DenunciaResponse denuncia = denunciaService.obtenerDenuncia(id);

        // Verificar permisos para ciudadanos
        if (usuario.getRol() == RolUsuario.CIUDADANO) {
            if (!denunciaService.denunciaPerteneceAUsuario(id, usuario.getId())) {
                throw new RuntimeException("No tiene permisos para ver esta denuncia");
            }
        }

        return ResponseEntity.ok(denuncia);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<DenunciaResponse> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado,
            @RequestParam(required = false) String observacion,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        DenunciaResponse response = denunciaService.actualizarEstado(id, nuevoEstado, usuario.getId(), observacion);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-denuncias")
    @PreAuthorize("hasRole('CIUDADANO')")
    public ResponseEntity<Page<DenunciaResponse>> listarMisDenuncias(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        Page<DenunciaResponse> denuncias = denunciaService.listarDenunciasPorUsuario(usuario.getId(), pageable);
        return ResponseEntity.ok(denuncias);
    }

    // src/main/java/tfg/sircc_backend/controller/DenunciaController.java

    @Autowired
    private CasoRepository casoRepository;  // ✅ Agregar esta dependencia

    // =====================================================
// RF-07: Listar investigadores disponibles
// =====================================================
    @GetMapping("/investigadores")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> listarInvestigadores(HttpSession session) {
        getUsuarioFromSession(session);

        List<Usuario> investigadores = denunciaService.listarInvestigadoresDisponibles();

        List<Map<String, Object>> response = investigadores.stream()
                .map(inv -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", inv.getId());
                    map.put("email", inv.getEmail());
                    if (inv.getPersona() != null) {
                        map.put("nombreCompleto",
                                inv.getPersona().getNombres() + " " + inv.getPersona().getApellidos());
                    }

                    // ✅ Contar casos activos usando el repositorio
                    long casosActivos = casoRepository.countByInvestigadorPrincipalAndEstadoNot(inv, EstadoCaso.CERRADO);
                    map.put("casosActivos", casosActivos);

                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }


    // src/main/java/tfg/sircc_backend/controller/DenunciaController.java
// Agregar estos endpoints

    // =====================================================
// INVESTIGADOR: Listar mis denuncias asignadas
// =====================================================
    @GetMapping("/mis-denuncias-investigador")
    @PreAuthorize("hasRole('INVESTIGADOR')")
    public ResponseEntity<Page<DenunciaResponse>> listarMisDenunciasInvestigador(
            @RequestParam(required = false) String estado,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        Page<DenunciaResponse> denuncias = denunciaService.listarDenunciasAsignadas(usuario.getId(), estado, pageable);
        return ResponseEntity.ok(denuncias);
    }

    // =====================================================
// INVESTIGADOR: Resolver denuncia
// =====================================================
    @PatchMapping("/{id}/resolver")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<DenunciaResponse> resolverDenuncia(
            @PathVariable Long id,
            @RequestParam(required = false) String resolucion,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        DenunciaResponse response = denunciaService.resolverDenuncia(id, usuario.getId(), resolucion);
        return ResponseEntity.ok(response);
    }

    // =====================================================
// INVESTIGADOR: Archivar denuncia
// =====================================================
    @PatchMapping("/{id}/archivar-investigador")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<DenunciaResponse> archivarDenunciaInvestigador(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo,
            HttpSession session) {

        Usuario usuario = getUsuarioFromSession(session);
        DenunciaResponse response = denunciaService.archivarDenuncia(id, usuario.getId(), motivo);
        return ResponseEntity.ok(response);
    }

    @Autowired
    private DenunciaRepository denunciaRepository;

    // =====================================================
// INVESTIGADOR: Obtener resumen de mis casos
// =====================================================
    @GetMapping("/resumen-investigador")
    @PreAuthorize("hasRole('INVESTIGADOR')")
    public ResponseEntity<Map<String, Object>> obtenerResumenInvestigador(HttpSession session) {
        Usuario usuario = getUsuarioFromSession(session);

        long denunciasInvestigacion = denunciaRepository.countByInvestigadorIdAndEstado(usuario.getId(), EstadoDenuncia.INVESTIGACION);
        long denunciasResueltas = denunciaRepository.countByInvestigadorIdAndEstado(usuario.getId(), EstadoDenuncia.RESUELTA);
        long denunciasArchivadas = denunciaRepository.countByInvestigadorIdAndEstado(usuario.getId(), EstadoDenuncia.ARCHIVADA);

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalAsignadas", denunciasInvestigacion + denunciasResueltas + denunciasArchivadas);
        resumen.put("enInvestigacion", denunciasInvestigacion);
        resumen.put("resueltas", denunciasResueltas);
        resumen.put("archivadas", denunciasArchivadas);

        return ResponseEntity.ok(resumen);
    }
}