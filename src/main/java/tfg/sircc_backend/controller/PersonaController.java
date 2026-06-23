package tfg.sircc_backend.controller;

import tfg.sircc_backend.dto.request.PersonaRequest;
import tfg.sircc_backend.dto.response.PersonaResponse;
import tfg.sircc_backend.model.Usuario;
import tfg.sircc_backend.repository.UsuarioRepository;
import tfg.sircc_backend.service.PersonaService;
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

@CrossOrigin(origins = {"http://localhost:4200", "https://sircc.vercel.app"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/personas")
public class PersonaController {

    @Autowired
    private PersonaService personaService;

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

    @GetMapping("/documento/{documento}")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ANALISTA', 'ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<PersonaResponse> buscarPorDocumento(@PathVariable String documento, HttpSession session) {
        getUsuarioFromSession(session);
        PersonaResponse persona = personaService.buscarPorDocumento(documento);
        return ResponseEntity.ok(persona);
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ANALISTA', 'ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<Page<PersonaResponse>> buscarPorNombre(
            @RequestParam String nombre,
            @PageableDefault(size = 10, sort = "apellidos", direction = Sort.Direction.ASC) Pageable pageable,
            HttpSession session) {

        getUsuarioFromSession(session);
        Page<PersonaResponse> personas = personaService.buscarPorNombre(nombre, pageable);
        return ResponseEntity.ok(personas);
    }

    @GetMapping("/alias/{alias}")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ANALISTA', 'ADMIN')")
    public ResponseEntity<Page<PersonaResponse>> buscarPorAlias(
            @PathVariable String alias,
            @PageableDefault(size = 10, sort = "apellidos") Pageable pageable,
            HttpSession session) {

        getUsuarioFromSession(session);
        Page<PersonaResponse> personas = personaService.buscarPorAlias(alias, pageable);
        return ResponseEntity.ok(personas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ANALISTA', 'ADMINISTRATIVO', 'ADMIN')")
    public ResponseEntity<PersonaResponse> obtenerPersona(@PathVariable Long id, HttpSession session) {
        getUsuarioFromSession(session);
        PersonaResponse persona = personaService.obtenerPersona(id);
        return ResponseEntity.ok(persona);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<PersonaResponse> crearPersona(@Valid @RequestBody PersonaRequest request, HttpSession session) {
        getUsuarioFromSession(session);
        PersonaResponse response = personaService.crearPersona(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'INVESTIGADOR', 'ADMIN')")
    public ResponseEntity<PersonaResponse> actualizarPersona(
            @PathVariable Long id,
            @Valid @RequestBody PersonaRequest request,
            HttpSession session) {

        getUsuarioFromSession(session);
        PersonaResponse response = personaService.actualizarPersona(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ANALISTA', 'ADMIN')")
    public ResponseEntity<Page<PersonaResponse>> listarPersonas(
            @PageableDefault(size = 10, sort = "apellidos", direction = Sort.Direction.ASC) Pageable pageable,
            HttpSession session) {

        getUsuarioFromSession(session);
        Page<PersonaResponse> personas = personaService.listarTodas(pageable);
        return ResponseEntity.ok(personas);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarPersona(@PathVariable Long id, HttpSession session) {
        getUsuarioFromSession(session);
        personaService.eliminarPersona(id);
        return ResponseEntity.noContent().build();
    }
}