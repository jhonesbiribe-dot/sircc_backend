package tfg.sircc_backend.dto.response;

// dto/response/PersonaResponse.java


import tfg.sircc_backend.model.enums.TipoPersona;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PersonaResponse {
    private Long id;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String documento;
    private String alias;
    private String telefono;
    private String fotoUrl;
    private TipoPersona tipoPersona;
    private LocalDateTime createdAt;
    private List<AntecedenteResponse> antecedentes;

    @Data
    public static class AntecedenteResponse {
        private Long casoId;
        private String numeroCaso;
        private String nombreCaso;
        private String rolEnCaso;
        private LocalDateTime fechaApertura;
    }
}