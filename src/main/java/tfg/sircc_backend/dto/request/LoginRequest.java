package tfg.sircc_backend.dto.request;

// dto/request/LoginRequest.java


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
