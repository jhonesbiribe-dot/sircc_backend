package tfg.sircc_backend.dto.response;

// dto/response/ReporteDelitosResponse.java


import lombok.Data;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@Data
public class ReporteDelitosResponse {
    private Periodo periodo;
    private List<DelitoEstadistica> delitos;
    private Map<String, Long> delitosPorCategoria;
    private Map<String, Double> delitosPorHora;
    private Map<String, Long> delitosPorDiaSemana;

    @Data
    public static class Periodo {
        private LocalDate inicio;
        private LocalDate fin;
        private Long totalDias;
    }

    @Data
    public static class DelitoEstadistica {
        private String codigo;
        private String nombre;
        private String categoria;
        private String gravedad;
        private Long cantidad;
        private Double porcentaje;
        private Double variacionMensual;
    }
}