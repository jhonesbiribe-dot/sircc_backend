package tfg.sircc_backend.repository;

// repository/CasoRepository.java


import tfg.sircc_backend.model.Caso;
import tfg.sircc_backend.model.Usuario;
import tfg.sircc_backend.model.enums.EstadoCaso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CasoRepository extends JpaRepository<Caso, Long> {
    Page<Caso> findByEstado(EstadoCaso estado, Pageable pageable);
    Page<Caso> findByInvestigadorPrincipal(Usuario investigador, Pageable pageable);
    Page<Caso> findByEstadoAndInvestigadorPrincipal(EstadoCaso estado, Usuario investigador, Pageable pageable);
    boolean existsByNumeroCaso(String numeroCaso);

    long countByInvestigadorPrincipalAndEstadoNot(Usuario investigador, EstadoCaso estado);
}
