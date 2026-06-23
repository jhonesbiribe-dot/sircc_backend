package tfg.sircc_backend.repository;

// repository/PersonaRepository.java


import tfg.sircc_backend.model.Persona;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByDocumento(String documento);
    boolean existsByDocumento(String documento);

    Page<Persona> findByNombresContainingOrApellidosContaining(String nombres, String apellidos, Pageable pageable);
    Page<Persona> findByAliasContainingIgnoreCase(String alias, Pageable pageable);

    @Query("SELECT p FROM Persona p WHERE " +
            "LOWER(p.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.documento) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.alias) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Persona> buscarGlobal(@Param("search") String search, Pageable pageable);
}
