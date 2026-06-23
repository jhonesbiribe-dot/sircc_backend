// src/main/java/tfg/sircc_backend/controller/CasoController.java
package tfg.sircc_backend.controller;

import tfg.sircc_backend.dto.request.CasoRequest;
import tfg.sircc_backend.dto.response.CasoResponse;
import tfg.sircc_backend.model.AvanceInvestigacion;
import tfg.sircc_backend.model.Evidencia;
import tfg.sircc_backend.model.Usuario;
import tfg.sircc_backend.repository.AvanceInvestigacionRepository;
import tfg.sircc_backend.repository.UsuarioRepository;
import tfg.sircc_backend.service.CasoService;
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

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:4200", "https://sircc.vercel.app"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/casos")
public class CasoController {

    @Autowired
    private CasoService casoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AvanceInvestigacionRepository avanceInvestigacionRepository;

    private Usuario getUsuarioFromSession(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // =====================================================
    // CRUD Básico
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<CasoResponse> crearCaso(@Valid @RequestBody CasoRequest request, HttpSession session) {
        Usuario usuario = getUsuarioFromSession(session);
        CasoResponse response = casoService.crearCaso(request, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ANALISTA', 'ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<CasoResponse> obtenerCaso(@PathVariable Long id, HttpSession session) {
        getUsuarioFromSession(session);
        CasoResponse caso = casoService.obtenerCaso(id);
        return ResponseEntity.ok(caso);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ANALISTA', 'ADMIN')")
    public ResponseEntity<Page<CasoResponse>> listarTodos(
            @PageableDefault(size = 10, sort = "fechaApertura", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {
        getUsuarioFromSession(session);
        Page<CasoResponse> casos = casoService.listarTodos(pageable);
        return ResponseEntity.ok(casos);
    }

    // =====================================================
    // INVESTIGADOR: Mis casos
    // =====================================================
    @GetMapping("/mis-casos")
    @PreAuthorize("hasRole('INVESTIGADOR')")
    public ResponseEntity<Page<CasoResponse>> listarMisCasos(
            @PageableDefault(size = 10, sort = "fechaApertura", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {
        Usuario usuario = getUsuarioFromSession(session);
        Page<CasoResponse> casos = casoService.listarCasosPorInvestigador(usuario.getId(), pageable);
        return ResponseEntity.ok(casos);
    }

    // =====================================================
    // VINCULAR DENUNCIAS
    // =====================================================
    @PostMapping("/{casoId}/denuncias/{denunciaId}")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<CasoResponse> vincularDenuncia(
            @PathVariable Long casoId,
            @PathVariable Long denunciaId,
            HttpSession session) {
        getUsuarioFromSession(session);
        CasoResponse response = casoService.vincularDenuncia(casoId, denunciaId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{casoId}/denuncias/{denunciaId}")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<CasoResponse> desvincularDenuncia(
            @PathVariable Long casoId,
            @PathVariable Long denunciaId,
            HttpSession session) {
        getUsuarioFromSession(session);
        CasoResponse response = casoService.desvincularDenuncia(casoId, denunciaId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // VINCULAR PERSONAS
    // =====================================================
    @PostMapping("/{casoId}/personas/{personaId}")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<CasoResponse> vincularPersona(
            @PathVariable Long casoId,
            @PathVariable Long personaId,
            @RequestParam String rol,
            HttpSession session) {
        Usuario usuario = getUsuarioFromSession(session);
        CasoResponse response = casoService.vincularPersona(casoId, personaId, rol, usuario.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{casoId}/personas/{personaId}")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<CasoResponse> desvincularPersona(
            @PathVariable Long casoId,
            @PathVariable Long personaId,
            HttpSession session) {
        getUsuarioFromSession(session);
        CasoResponse response = casoService.desvincularPersona(casoId, personaId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // EVIDENCIAS (Caso)
    // =====================================================
    @PostMapping("/{casoId}/evidencias")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<Evidencia> agregarEvidencia(
            @PathVariable Long casoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String descripcion,
            HttpSession session) {
        Usuario usuario = getUsuarioFromSession(session);
        Evidencia evidencia = casoService.agregarEvidenciaCaso(casoId, file, usuario.getId(), descripcion);
        return ResponseEntity.status(HttpStatus.CREATED).body(evidencia);
    }

    @GetMapping("/{casoId}/evidencias")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ANALISTA', 'ADMIN')")
    public ResponseEntity<List<Evidencia>> listarEvidencias(@PathVariable Long casoId, HttpSession session) {
        getUsuarioFromSession(session);
        List<Evidencia> evidencias = casoService.listarEvidenciasCaso(casoId);
        return ResponseEntity.ok(evidencias);
    }

    @DeleteMapping("/evidencias/{evidenciaId}")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<Void> eliminarEvidencia(@PathVariable Long evidenciaId, HttpSession session) {
        Usuario usuario = getUsuarioFromSession(session);
        casoService.eliminarEvidenciaCaso(evidenciaId, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // AVANCES
    // =====================================================
    @PostMapping("/{casoId}/avances")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<CasoResponse> registrarAvance(
            @PathVariable Long casoId,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        Usuario usuario = getUsuarioFromSession(session);
        String descripcion = request.get("descripcion");
        CasoResponse response = casoService.registrarAvance(casoId, descripcion, usuario.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{casoId}/avances")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ANALISTA', 'ADMIN')")
    public ResponseEntity<List<AvanceInvestigacion>> listarAvances(@PathVariable Long casoId, HttpSession session) {
        getUsuarioFromSession(session);
        List<AvanceInvestigacion> avances = casoService.listarAvances(casoId);
        return ResponseEntity.ok(avances);
    }

    // =====================================================
    // CERRAR CASO
    // =====================================================
    @PatchMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<CasoResponse> cerrarCaso(
            @PathVariable Long id,
            @RequestParam String resolucion,
            HttpSession session) {
        Usuario usuario = getUsuarioFromSession(session);
        CasoResponse response = casoService.cerrarCaso(id, resolucion, usuario.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ANALISTA', 'ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<Page<CasoResponse>> listarPorEstado(
            @PathVariable String estado,
            @PageableDefault(size = 10, sort = "fechaApertura", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {
        getUsuarioFromSession(session);
        Page<CasoResponse> casos = casoService.listarCasosPorEstado(estado, pageable);
        return ResponseEntity.ok(casos);
    }
}