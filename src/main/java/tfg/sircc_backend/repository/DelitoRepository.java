// src/main/java/tfg/sircc_backend/repository/DelitoRepository.java
package tfg.sircc_backend.repository;

import tfg.sircc_backend.model.Delito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DelitoRepository extends JpaRepository<Delito, Long> {
    List<Delito> findAllByOrderByNombreAsc();
}