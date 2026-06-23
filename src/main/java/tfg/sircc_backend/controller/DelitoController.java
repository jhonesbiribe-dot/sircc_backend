// src/main/java/tfg/sircc_backend/controller/DelitoController.java
package tfg.sircc_backend.controller;

import tfg.sircc_backend.model.Delito;
import tfg.sircc_backend.repository.DelitoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delitos")
@CrossOrigin(origins = {"http://localhost:4200", "https://sircc.vercel.app"}, allowCredentials = "true")
public class DelitoController {

    @Autowired
    private DelitoRepository delitoRepository;

    // Listar todos los delitos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'INVESTIGADOR', 'ANALISTA', 'ADMIN')")
    public ResponseEntity<List<Delito>> listarTodos() {
        List<Delito> delitos = delitoRepository.findAllByOrderByNombreAsc();
        return ResponseEntity.ok(delitos);
    }

    // Obtener delitos por IDs
    @PostMapping("/ids")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'INVESTIGADOR', 'ANALISTA', 'ADMIN')")
    public ResponseEntity<List<Delito>> obtenerPorIds(@RequestBody List<Long> ids) {
        List<Delito> delitos = delitoRepository.findAllById(ids);
        return ResponseEntity.ok(delitos);
    }
}