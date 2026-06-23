// src/main/java/tfg/sircc_backend/dto/response/DenunciaResponse.java
package tfg.sircc_backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DenunciaResponse {
    private Long id;
    private String numeroDenuncia;
    private String descripcionHechos;
    private String modusOperandi;
    private String estado;
    private LocalDateTime fechaHora;
    private String ubicacionTexto;
    private Double latitud;
    private Double longitud;
    private LocalDateTime createdAt;
    private Boolean esAnonima;
    private List<String> delitos;
    private List<EvidenciaResponse> evidencias;
    private Long investigadorId;
    private String investigadorNombre;

    @Data
    public static class EvidenciaResponse {
        private Long id;
        private String tipo;
        private String url;
        private String nombreArchivo;
        private String descripcion;
        private LocalDateTime fechaSubida;
    }
}