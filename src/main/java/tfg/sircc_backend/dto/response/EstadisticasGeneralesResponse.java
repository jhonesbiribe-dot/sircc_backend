package tfg.sircc_backend.dto.response;

// dto/response/EstadisticasGeneralesResponse.java


import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class EstadisticasGeneralesResponse {
    private ResumenGeneral resumen;
    private Map<String, Long> denunciasPorEstado;
    private Map<String, Long> delitosMasComunes;
    private Map<String, Double> distribucionPorZona;
    private TendenciaMensual tendenciaUltimosMeses;
    private LocalDateTime fechaGeneracion;

    @Data
    public static class ResumenGeneral {
        private Long totalDenuncias;
        private Long denunciasPendientes;
        private Long denunciasEnInvestigacion;
        private Long denunciasResueltas;
        private Long casosAbiertos;
        private Long totalPersonasRegistradas;
        private Double tasaResolucion;
    }

    @Data
    public static class TendenciaMensual {
        private Map<String, Long> denunciasPorMes;
        private Map<String, Double> variacionPorcentual;
    }
}