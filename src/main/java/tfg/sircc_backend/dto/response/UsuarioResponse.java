package tfg.sircc_backend.dto.response;

// dto/response/UsuarioResponse.java


import tfg.sircc_backend.model.enums.RolUsuario;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsuarioResponse {
    private Long id;
    private String email;
    private String nombreCompleto;
    private String documento;
    private String telefono;
    private RolUsuario rol;
    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
