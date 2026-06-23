package tfg.sircc_backend.service;
// service/AuditoriaService.java

import tfg.sircc_backend.model.Auditoria;
import tfg.sircc_backend.model.Usuario;
import tfg.sircc_backend.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    public void registrar(Usuario usuario, String operacion, String tablaAfectada,
                          Long registroId, String detalle) {
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setOperacion(operacion);
        auditoria.setTablaAfectada(tablaAfectada);
        auditoria.setRegistroId(registroId);
        auditoria.setDetalle(detalle);
        auditoria.setIpOrigen(obtenerIpCliente());

        auditoriaRepository.save(auditoria);
    }

    public Page<Auditoria> listarAuditorias(Pageable pageable) {
        return auditoriaRepository.findAll(pageable);
    }

    public Page<Auditoria> listarPorUsuario(Long usuarioId, Pageable pageable) {
        return auditoriaRepository.findByUsuarioId(usuarioId, pageable);
    }

    public Page<Auditoria> listarPorOperacion(String operacion, Pageable pageable) {
        return auditoriaRepository.findByOperacion(operacion, pageable);
    }

    private String obtenerIpCliente() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }
            return ip;
        }
        return "0.0.0.0";
    }
}