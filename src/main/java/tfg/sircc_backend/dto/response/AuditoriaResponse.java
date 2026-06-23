// src/main/java/tfg/sircc_backend/dto/response/AuditoriaResponse.java
package tfg.sircc_backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditoriaResponse {
    private Long id;
    private LocalDateTime fechaHora;
    private String operacion;
    private String tablaAfectada;
    private Long registroId;
    private String detalle;
    private String ipOrigen;
    private UsuarioInfo usuario;

    @Data
    public static class UsuarioInfo {
        private Long id;
        private String email;
        private String nombreCompleto;
    }
}