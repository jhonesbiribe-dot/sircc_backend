// src/main/java/tfg/sircc_backend/repository/UsuarioRepository.java
package tfg.sircc_backend.repository;

import tfg.sircc_backend.model.Usuario;
import tfg.sircc_backend.model.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByRolAndEstado(RolUsuario rol, Boolean estado);
}