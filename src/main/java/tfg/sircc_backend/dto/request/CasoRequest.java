package tfg.sircc_backend.dto.request;


// dto/request/CasoRequest.java

import tfg.sircc_backend.model.enums.EstadoCaso;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CasoRequest {

    @NotBlank(message = "El nombre del caso es obligatorio")
    private String nombre;

    private String descripcion;

    private EstadoCaso estado = EstadoCaso.ABIERTO;

    private Long investigadorPrincipalId;

    private Long supervisorId;

    private List<Long> denunciasIds;

    private List<Long> personasIds;
}