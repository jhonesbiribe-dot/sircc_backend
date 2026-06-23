package tfg.sircc_backend.repository;

// repository/AuditoriaRepository.java


import tfg.sircc_backend.model.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    Page<Auditoria> findByUsuarioId(Long usuarioId, Pageable pageable);
    Page<Auditoria> findByOperacion(String operacion, Pageable pageable);
}
