package tfg.sircc_backend.repository;

// repository/SeguimientoCasoRepository.java


import tfg.sircc_backend.model.SeguimientoCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeguimientoCasoRepository extends JpaRepository<SeguimientoCaso, Long> {
    List<SeguimientoCaso> findByCasoIdOrderByFechaCambioDesc(Long casoId);
}
