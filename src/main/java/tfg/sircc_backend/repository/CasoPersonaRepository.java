// src/main/java/tfg/sircc_backend/repository/CasoPersonaRepository.java
package tfg.sircc_backend.repository;

import tfg.sircc_backend.model.CasoPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CasoPersonaRepository extends JpaRepository<CasoPersona, Long> {
    List<CasoPersona> findByCasoId(Long casoId);
    List<CasoPersona> findByPersonaId(Long personaId);
    Optional<CasoPersona> findByCasoIdAndPersonaId(Long casoId, Long personaId);
    boolean existsByCasoIdAndPersonaId(Long casoId, Long personaId);
}