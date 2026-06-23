// src/main/java/tfg/sircc_backend/dto/request/DenunciaRequest.java
package tfg.sircc_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DenunciaRequest {

    @NotBlank(message = "La descripción de los hechos es obligatoria")
    private String descripcionHechos;

    private String modusOperandi;

    @NotNull(message = "La fecha y hora de los hechos es obligatoria")
    private String fechaHora;  // ✅ Cambiar de LocalDateTime a String

    private String ubicacionTexto;

    private BigDecimal latitud;

    private BigDecimal longitud;

    private Boolean esAnonima = false;

    private List<Long> delitosIds;

    private List<String> evidenciasBase64;
}