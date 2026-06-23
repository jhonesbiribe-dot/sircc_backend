package tfg.sircc_backend.dto.request;


import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class AsignacionDTO {
    @NotNull(message = "El ID del investigador es obligatorio")
    private Long idInvestigador;
    private String observacion;
}