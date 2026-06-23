package tfg.sircc_backend.repository;

// repository/SeguimientoDenunciaRepository.java


import tfg.sircc_backend.model.SeguimientoDenuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguimientoDenunciaRepository extends JpaRepository<SeguimientoDenuncia, Long> {
}