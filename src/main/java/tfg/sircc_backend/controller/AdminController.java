package tfg.sircc_backend.controller;

import tfg.sircc_backend.dto.request.UsuarioRequest;
import tfg.sircc_backend.dto.response.UsuarioDetailResponse;
import tfg.sircc_backend.dto.response.UsuarioListResponse;
import tfg.sircc_backend.model.Persona;
import tfg.sircc_backend.model.Usuario;
import tfg.sircc_backend.repository.PersonaRepository;
import tfg.sircc_backend.repository.UsuarioRepository;
import tfg.sircc_backend.service.AuditoriaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:4200", "https://sircc.vercel.app"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditoriaService auditoriaService;

    private Usuario getUsuarioFromSession(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // ✅ CREAR USUARIO - Usa DTO
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDetailResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request, HttpSession session) {
        Usuario admin = getUsuarioFromSession(session);

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + request.getEmail());
        }

        Persona persona = new Persona();
        persona.setNombres(request.getNombres());
        persona.setApellidos(request.getApellidos());
        persona.setDocumento(request.getDocumento());
        persona.setTelefono(request.getTelefono());
        persona = personaRepository.save(persona);

        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());
        usuario.setEstado(true);
        usuario.setPersona(persona);

        Usuario savedUsuario = usuarioRepository.save(usuario);

        auditoriaService.registrar(admin, "INSERT", "usuarios", savedUsuario.getId(),
                "Usuario creado con rol: " + request.getRol());

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDetailResponse(savedUsuario));
    }

    // ✅ LISTAR USUARIOS - Usa DTO
    @GetMapping("/usuarios")
    public ResponseEntity<Map<String, Object>> listarUsuarios(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {

        getUsuarioFromSession(session);

        Page<Usuario> page = usuarioRepository.findAll(pageable);

        List<UsuarioListResponse> usuariosDTO = page.getContent().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", usuariosDTO);
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("size", page.getSize());
        response.put("number", page.getNumber());

        return ResponseEntity.ok(response);
    }

    // ✅ OBTENER USUARIO POR ID - Usa DTO
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDetailResponse> obtenerUsuario(@PathVariable Long id, HttpSession session) {
        getUsuarioFromSession(session);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(convertToDetailResponse(usuario));
    }

    // ✅ ACTUALIZAR USUARIO - Usa DTO
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDetailResponse> actualizarUsuario(
            @PathVariable Long id,
            @RequestBody UsuarioRequest request,
            HttpSession session) {

        Usuario admin = getUsuarioFromSession(session);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRol() != null) {
            usuario.setRol(request.getRol());
        }

        if (usuario.getPersona() != null) {
            Persona persona = usuario.getPersona();
            persona.setNombres(request.getNombres());
            persona.setApellidos(request.getApellidos());
            persona.setDocumento(request.getDocumento());
            persona.setTelefono(request.getTelefono());
            personaRepository.save(persona);
        }

        Usuario updatedUsuario = usuarioRepository.save(usuario);

        auditoriaService.registrar(admin, "UPDATE", "usuarios", updatedUsuario.getId(),
                "Usuario actualizado. Nuevo rol: " + request.getRol());

        return ResponseEntity.ok(convertToDetailResponse(updatedUsuario));
    }

    // ✅ DESHABILITAR USUARIO
    @PatchMapping("/usuarios/{id}/deshabilitar")
    public ResponseEntity<Void> deshabilitarUsuario(@PathVariable Long id, HttpSession session) {
        Usuario admin = getUsuarioFromSession(session);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setEstado(false);
        usuarioRepository.save(usuario);

        auditoriaService.registrar(admin, "UPDATE", "usuarios", usuario.getId(), "Usuario deshabilitado");

        return ResponseEntity.noContent().build();
    }

    // ✅ HABILITAR USUARIO
    @PatchMapping("/usuarios/{id}/habilitar")
    public ResponseEntity<Void> habilitarUsuario(@PathVariable Long id, HttpSession session) {
        Usuario admin = getUsuarioFromSession(session);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setEstado(true);
        usuarioRepository.save(usuario);

        auditoriaService.registrar(admin, "UPDATE", "usuarios", usuario.getId(), "Usuario habilitado");

        return ResponseEntity.noContent().build();
    }

    // ✅ ELIMINAR USUARIO
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id, HttpSession session) {
        Usuario admin = getUsuarioFromSession(session);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getDenuncias().isEmpty()) {
            throw new RuntimeException("No se puede eliminar usuario con denuncias asociadas");
        }

        usuarioRepository.delete(usuario);

        auditoriaService.registrar(admin, "DELETE", "usuarios", id, "Usuario eliminado");

        return ResponseEntity.noContent().build();
    }

    // ✅ MÉTODOS DE CONVERSIÓN A DTO

    private UsuarioListResponse convertToListResponse(Usuario usuario) {
        UsuarioListResponse response = new UsuarioListResponse();
        response.setId(usuario.getId());
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol());
        response.setEstado(usuario.getEstado());
        response.setCreatedAt(usuario.getCreatedAt());
        response.setUpdatedAt(usuario.getUpdatedAt());

        if (usuario.getPersona() != null) {
            response.setNombreCompleto(usuario.getPersona().getNombres() + " " + usuario.getPersona().getApellidos());
            response.setDocumento(usuario.getPersona().getDocumento());
            response.setTelefono(usuario.getPersona().getTelefono());
        }

        return response;
    }

    private UsuarioDetailResponse convertToDetailResponse(Usuario usuario) {
        UsuarioDetailResponse response = new UsuarioDetailResponse();
        response.setId(usuario.getId());
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol());
        response.setEstado(usuario.getEstado());
        response.setCreatedAt(usuario.getCreatedAt());
        response.setUpdatedAt(usuario.getUpdatedAt());

        if (usuario.getPersona() != null) {
            response.setNombreCompleto(usuario.getPersona().getNombres() + " " + usuario.getPersona().getApellidos());
            response.setDocumento(usuario.getPersona().getDocumento());
            response.setTelefono(usuario.getPersona().getTelefono());
            response.setPersonaId(usuario.getPersona().getId());
        }

        return response;
    }
}