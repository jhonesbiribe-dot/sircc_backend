package tfg.sircc_backend.dto.response;

// dto/response/MapaCalorResponse.java

import lombok.Data;
import java.util.List;

@Data
public class MapaCalorResponse {
    private List<PuntoCalor> puntos;
    private List<ZonaCalor> zonas;
    private ConfiguracionMapa configuracion;

    @Data
    public static class PuntoCalor {
        private Double latitud;
        private Double longitud;
        private Long intensidad;      // Número de delitos en ese punto
        private String tipoDelito;
        private String direccion;
    }

    @Data
    public static class ZonaCalor {
        private String nombreZona;
        private Double latitudCentro;
        private Double longitudCentro;
        private Double radio;
        private Long totalDelitos;
        private List<String> delitosPredominantes;
    }

    @Data
    public static class ConfiguracionMapa {
        private Double latitudCentro;
        private Double longitudCentro;
        private Integer zoom;
        private String[] coloresCalor;
    }
}