package tfg.sircc_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import tfg.sircc_backend.dto.request.LoginRequest;
import tfg.sircc_backend.dto.request.RegisterRequest;
import tfg.sircc_backend.dto.response.JwtResponse;
import tfg.sircc_backend.model.Persona;
import tfg.sircc_backend.model.Usuario;
import tfg.sircc_backend.model.enums.RolUsuario;
import tfg.sircc_backend.model.enums.TipoPersona;
import tfg.sircc_backend.repository.PersonaRepository;
import tfg.sircc_backend.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:4200", "https://sircc.vercel.app"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        // ✅ Guardar el ID como Long, NO el email
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioEmail", usuario.getEmail());
        session.setAttribute("usuarioRol", usuario.getRol().name());

        System.out.println("✅ Login exitoso - Sesión ID: " + session.getId());
        System.out.println("✅ Usuario ID: " + usuario.getId() + " - Email: " + usuario.getEmail());

        return ResponseEntity.ok(new JwtResponse(null, usuario.getId(), usuario.getEmail(), usuario.getRol().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Sesión cerrada correctamente");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autenticado - Usuario no encontrado");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(401).body("No autenticado - Usuario no existe");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", usuario.getId());
        response.put("email", usuario.getEmail());
        response.put("rol", usuario.getRol().name());
        response.put("estado", usuario.getEstado());
        response.put("sessionId", session.getId());

        return ResponseEntity.ok(response);
    }

    // src/main/java/tfg/sircc_backend/controller/AuthController.java
// Agregar este endpoint

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El email ya está registrado"));
        }

        // Crear persona
        Persona persona = new Persona();
        persona.setNombres(request.getNombres());
        persona.setApellidos(request.getApellidos());
        persona.setDocumento(request.getDocumento());
        persona.setTelefono(request.getTelefono());
        persona.setTipoPersona(TipoPersona.DENUNCIANTE);
        persona = personaRepository.save(persona);

        // Crear usuario con rol CIUDADANO
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(RolUsuario.CIUDADANO);
        usuario.setEstado(true);
        usuario.setPersona(persona);

        Usuario savedUsuario = usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "id", savedUsuario.getId(),
                        "email", savedUsuario.getEmail(),
                        "rol", savedUsuario.getRol().name()
                ));
    }
}