// src/main/java/tfg/sircc_backend/dto/response/UsuarioDetailResponse.java
package tfg.sircc_backend.dto.response;

import tfg.sircc_backend.model.enums.RolUsuario;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsuarioDetailResponse {
    private Long id;
    private String email;
    private RolUsuario rol;
    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String nombreCompleto;
    private String documento;
    private String telefono;
    private Long personaId;
}