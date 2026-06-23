// src/main/java/tfg/sircc_backend/repository/EvidenciaRepository.java
package tfg.sircc_backend.repository;

import tfg.sircc_backend.model.Evidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {

    // ✅ Buscar evidencias por ID de denuncia
    List<Evidencia> findByDenunciaId(Long denunciaId);

    // ✅ Buscar evidencias por ID de caso
    List<Evidencia> findByCasoId(Long casoId);

    // ✅ Contar evidencias por denuncia
    long countByDenunciaId(Long denunciaId);
}