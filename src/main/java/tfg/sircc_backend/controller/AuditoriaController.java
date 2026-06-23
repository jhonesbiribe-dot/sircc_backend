package tfg.sircc_backend.controller;

import tfg.sircc_backend.dto.response.AuditoriaResponse;
import tfg.sircc_backend.model.Auditoria;
import tfg.sircc_backend.model.Usuario;
import tfg.sircc_backend.repository.UsuarioRepository;
import tfg.sircc_backend.service.AuditoriaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:4200", "https://sircc.vercel.app"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasRole('ADMIN')")
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

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

    // ✅ LISTAR TODOS - Usa DTO
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarAuditorias(
            @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {

        getUsuarioFromSession(session);

        Page<Auditoria> page = auditoriaService.listarAuditorias(pageable);

        List<AuditoriaResponse> logsDTO = page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", logsDTO);
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("size", page.getSize());
        response.put("number", page.getNumber());

        return ResponseEntity.ok(response);
    }

    // ✅ LISTAR POR USUARIO - Usa DTO
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> listarPorUsuario(
            @PathVariable Long usuarioId,
            @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {

        getUsuarioFromSession(session);
        Page<Auditoria> page = auditoriaService.listarPorUsuario(usuarioId, pageable);

        List<AuditoriaResponse> logsDTO = page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", logsDTO);
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("size", page.getSize());
        response.put("number", page.getNumber());

        return ResponseEntity.ok(response);
    }

    // ✅ LISTAR POR OPERACIÓN - Usa DTO
    @GetMapping("/operacion/{operacion}")
    public ResponseEntity<Map<String, Object>> listarPorOperacion(
            @PathVariable String operacion,
            @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {

        getUsuarioFromSession(session);
        Page<Auditoria> page = auditoriaService.listarPorOperacion(operacion, pageable);

        List<AuditoriaResponse> logsDTO = page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", logsDTO);
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("size", page.getSize());
        response.put("number", page.getNumber());

        return ResponseEntity.ok(response);
    }

    // ✅ CONVERSIÓN A DTO
    private AuditoriaResponse convertToResponse(Auditoria auditoria) {
        AuditoriaResponse response = new AuditoriaResponse();
        response.setId(auditoria.getId());
        response.setFechaHora(auditoria.getFechaHora());
        response.setOperacion(auditoria.getOperacion());
        response.setTablaAfectada(auditoria.getTablaAfectada());
        response.setRegistroId(auditoria.getRegistroId());
        response.setDetalle(auditoria.getDetalle());
        response.setIpOrigen(auditoria.getIpOrigen());

        // ✅ Mapear el usuario correctamente
        if (auditoria.getUsuario() != null) {
            AuditoriaResponse.UsuarioInfo usuarioInfo = new AuditoriaResponse.UsuarioInfo();
            usuarioInfo.setId(auditoria.getUsuario().getId());
            usuarioInfo.setEmail(auditoria.getUsuario().getEmail());

            if (auditoria.getUsuario().getPersona() != null) {
                usuarioInfo.setNombreCompleto(
                        auditoria.getUsuario().getPersona().getNombres() + " " +
                                auditoria.getUsuario().getPersona().getApellidos()
                );
            }
            response.setUsuario(usuarioInfo);
        }

        return response;
    }
}