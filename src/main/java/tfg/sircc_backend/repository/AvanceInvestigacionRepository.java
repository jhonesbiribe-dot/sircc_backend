// src/main/java/tfg/sircc_backend/repository/AvanceInvestigacionRepository.java
package tfg.sircc_backend.repository;

import tfg.sircc_backend.model.AvanceInvestigacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvanceInvestigacionRepository extends JpaRepository<AvanceInvestigacion, Long> {
    List<AvanceInvestigacion> findByCasoIdOrderByFechaAvanceDesc(Long casoId);
}