package tfg.sircc_backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {
    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String email;
    private String rol;
    private Long expiraEn;
}

