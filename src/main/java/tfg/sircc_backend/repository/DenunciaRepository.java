// src/main/java/tfg/sircc_backend/repository/DenunciaRepository.java
package tfg.sircc_backend.repository;

import tfg.sircc_backend.model.Denuncia;
import tfg.sircc_backend.model.enums.EstadoDenuncia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {
    Page<Denuncia> findByEstado(EstadoDenuncia estado, Pageable pageable);
    Page<Denuncia> findByUsuarioId(Long usuarioId, Pageable pageable);
    Page<Denuncia> findByInvestigadorId(Long investigadorId, Pageable pageable);
    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByNumeroDenuncia(String numeroDenuncia);

    // ✅ También agregar este metodo para el contador
    long count();  // Ya existe en JpaRepository, pero es bueno tenerlo explícito

    // src/main/java/tfg/sircc_backend/repository/DenunciaRepository.java
// Agregar estos métodos

    Page<Denuncia> findByInvestigadorIdAndEstado(Long investigadorId, EstadoDenuncia estado, Pageable pageable);
    long countByInvestigadorIdAndEstado(Long investigadorId, EstadoDenuncia estado);
}