package tfg.sircc_backend.dto.request;

// dto/request/UsuarioRequest.java


import tfg.sircc_backend.model.enums.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank
    private String nombres;

    @NotBlank
    private String apellidos;

    private String documento;

    private String telefono;

    private RolUsuario rol;
}
