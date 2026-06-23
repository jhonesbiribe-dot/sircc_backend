// src/main/java/tfg/sircc_backend/service/DenunciaService.java
package tfg.sircc_backend.service;

import tfg.sircc_backend.dto.request.DenunciaRequest;
import tfg.sircc_backend.dto.response.DenunciaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import tfg.sircc_backend.model.Evidencia;
import tfg.sircc_backend.model.Usuario;

import java.util.List;

public interface DenunciaService {

    // CRUD básico
    DenunciaResponse crearDenuncia(DenunciaRequest request, Long usuarioId);
    DenunciaResponse obtenerDenuncia(Long id);
    Page<DenunciaResponse> listarDenunciasPorEstado(String estado, Pageable pageable);
    Page<DenunciaResponse> listarDenunciasPorUsuario(Long usuarioId, Pageable pageable);
    DenunciaResponse actualizarEstado(Long id, String nuevoEstado, Long usuarioId, String observacion);
    Page<DenunciaResponse> listarDenunciasPendientes(Pageable pageable);
    boolean denunciaPerteneceAUsuario(Long denunciaId, Long usuarioId);

    // ✅ RF-06: Validación y clasificación
    DenunciaResponse validarDenuncia(Long denunciaId, Long usuarioId, String observacion);
    DenunciaResponse rechazarDenuncia(Long denunciaId, Long usuarioId, String motivo);
    DenunciaResponse clasificarDelitos(Long denunciaId, List<Long> delitosIds, Long usuarioId);

    // ✅ RF-07: Asignación de investigadores
    List<Usuario> listarInvestigadoresDisponibles();
    DenunciaResponse asignarInvestigador(Long denunciaId, Long investigadorId, Long usuarioId, String observacion);
    Page<DenunciaResponse> listarDenunciasPorInvestigador(Long investigadorId, Pageable pageable);

    // ✅ RF-09: Evidencias
    Evidencia agregarEvidencia(Long denunciaId, MultipartFile file, Long usuarioId, String descripcion);
    List<Evidencia> listarEvidenciasPorDenuncia(Long denunciaId);
    void eliminarEvidencia(Long evidenciaId, Long usuarioId);


    // src/main/java/tfg/sircc_backend/service/DenunciaService.java (interfaz)
// Agregar estos métodos

    // Resolver denuncia
    DenunciaResponse resolverDenuncia(Long denunciaId, Long usuarioId, String resolucion);

    // Archivar denuncia (investigador)
    DenunciaResponse archivarDenuncia(Long denunciaId, Long usuarioId, String motivo);

    // Listar denuncias por investigador (con paginación y filtros)
    Page<DenunciaResponse> listarDenunciasAsignadas(Long investigadorId, String estado, Pageable pageable);
}