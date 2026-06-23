package tfg.sircc_backend.dto.response;

// dto/response/PatronDelictivoResponse.java


import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PatronDelictivoResponse {
    private Long id;
    private String nombrePatron;
    private String descripcion;
    private String modusOperandi;
    private List<CasoVinculado> casosVinculados;
    private PerfilSospechoso perfilSospechoso;
    private LocalDateTime fechaIdentificacion;
    private Double nivelConfianza;

    @Data
    public static class CasoVinculado {
        private Long casoId;
        private String numeroCaso;
        private String nombre;
        private LocalDateTime fechaApertura;
        private Double similitud;  // Porcentaje de similitud con el patrón
    }

    @Data
    public static class PerfilSospechoso {
        private String rangoEdad;
        private String generoPredominante;
        private List<String> modusOperandiComunes;
        private List<String> zonasFrecuentes;
        private List<String> horasPredilectas;
    }
}