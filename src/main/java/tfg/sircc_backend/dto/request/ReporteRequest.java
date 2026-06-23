package tfg.sircc_backend.dto.request;

// dto/request/ReporteRequest.java


import lombok.Data;
import java.time.LocalDate;

@Data
public class ReporteRequest {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String tipoDelito;      // Opcional: filtrar por tipo de delito
    private String zona;             // Opcional: filtrar por zona geográfica
    private String formato;          // PDF, EXCEL, CSV
}
