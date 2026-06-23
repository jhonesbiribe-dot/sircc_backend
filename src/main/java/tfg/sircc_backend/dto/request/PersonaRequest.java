package tfg.sircc_backend.dto.request;

import tfg.sircc_backend.model.enums.TipoPersona;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PersonaRequest {

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    // ✅ Hacer la fecha opcional - aceptar null
    @Past(message = "La fecha de nacimiento debe ser pasada")
    private LocalDate fechaNacimiento;

    private String direccion;

    @Pattern(regexp = "^[A-Za-z0-9-]{5,20}$", message = "Documento inválido")
    private String documento;

    private String alias;

    // ✅ Hacer teléfono opcional
    @Pattern(regexp = "^[0-9]{9,15}$", message = "Teléfono inválido")
    private String telefono;

    private TipoPersona tipoPersona;
}