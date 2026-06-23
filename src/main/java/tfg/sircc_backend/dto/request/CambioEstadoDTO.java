package tfg.sircc_backend.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CambioEstadoDTO {
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
    private String observacion;
}

