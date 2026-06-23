// src/main/java/tfg/sircc_backend/service/CasoService.java
package tfg.sircc_backend.service;

import tfg.sircc_backend.dto.request.CasoRequest;
import tfg.sircc_backend.dto.response.CasoResponse;
import tfg.sircc_backend.model.AvanceInvestigacion;
import tfg.sircc_backend.model.Evidencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CasoService {

    // CRUD básico
    CasoResponse crearCaso(CasoRequest request, Long usuarioId);
    CasoResponse obtenerCaso(Long id);
    CasoResponse actualizarCaso(Long id, CasoRequest request);
    Page<CasoResponse> listarCasosPorEstado(String estado, Pageable pageable);
    Page<CasoResponse> listarCasosPorInvestigador(Long investigadorId, Pageable pageable);
    Page<CasoResponse> listarTodos(Pageable pageable);

    // Vincular denuncias y personas
    CasoResponse vincularDenuncia(Long casoId, Long denunciaId);
    CasoResponse desvincularDenuncia(Long casoId, Long denunciaId);
    CasoResponse vincularPersona(Long casoId, Long personaId, String rol, Long usuarioId);
    CasoResponse desvincularPersona(Long casoId, Long personaId);

    // Evidencias
    Evidencia agregarEvidenciaCaso(Long casoId, MultipartFile file, Long usuarioId, String descripcion);
    List<Evidencia> listarEvidenciasCaso(Long casoId);
    void eliminarEvidenciaCaso(Long evidenciaId, Long usuarioId);

    // Avances
    CasoResponse registrarAvance(Long casoId, String descripcion, Long usuarioId);
    List<AvanceInvestigacion> listarAvances(Long casoId);

    // Cerrar caso
    CasoResponse cerrarCaso(Long id, String resolucion, Long usuarioId);
    void eliminarCaso(Long id);
}