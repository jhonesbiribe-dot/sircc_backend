package tfg.sircc_backend.dto.response;


// dto/response/CasoResponse.java

import tfg.sircc_backend.model.enums.EstadoCaso;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CasoResponse {
    private Long id;
    private String numeroCaso;
    private String nombre;
    private String descripcion;
    private EstadoCaso estado;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private InvestigadorInfo investigadorPrincipal;
    private InvestigadorInfo supervisor;
    private List<DenunciaBasicaResponse> denuncias;
    private List<PersonaBasicaResponse> personas;
    private Integer totalDenuncias;
    private LocalDateTime createdAt;

    @Data
    public static class InvestigadorInfo {
        private Long id;
        private String nombreCompleto;
        private String email;
    }

    @Data
    public static class DenunciaBasicaResponse {
        private Long id;
        private String numeroDenuncia;
        private String estado;
        private LocalDateTime fechaHora;
    }

    @Data
    public static class PersonaBasicaResponse {
        private Long id;
        private String nombreCompleto;
        private String documento;
        private String rolEnCaso;
    }
}