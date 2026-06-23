package tfg.sircc_backend.dto.response;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ReporteResponseDTO {

    private String periodo;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Long totalDenuncias;
    private Map<String, Long> distribucionPorDelito;
    private Map<String, Long> distribucionPorZona;
    private LocalDateTime generadoEn;
}