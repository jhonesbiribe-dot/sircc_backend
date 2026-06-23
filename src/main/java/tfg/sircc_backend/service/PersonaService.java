package tfg.sircc_backend.service;
// service/PersonaService.java


import tfg.sircc_backend.dto.request.PersonaRequest;
import tfg.sircc_backend.dto.response.PersonaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PersonaService {
    PersonaResponse crearPersona(PersonaRequest request);
    PersonaResponse actualizarPersona(Long id, PersonaRequest request);
    PersonaResponse obtenerPersona(Long id);
    PersonaResponse buscarPorDocumento(String documento);
    Page<PersonaResponse> buscarPorNombre(String nombre, Pageable pageable);
    Page<PersonaResponse> buscarPorAlias(String alias, Pageable pageable);
    Page<PersonaResponse> listarTodas(Pageable pageable);
    void eliminarPersona(Long id);
    PersonaResponse vincularACaso(Long personaId, Long casoId, String rolEnCaso);
}