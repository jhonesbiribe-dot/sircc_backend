package tfg.sircc_backend.model;

// Evidencia.java

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evidencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipo;  // IMAGEN, VIDEO, AUDIO, DOCUMENTO, OTRO

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 255)
    private String nombreArchivo;

    private Long tamanoBytes;

    @Column(length = 20)
    private String formato;

    @Column(length = 255)
    private String descripcion;

    // Relación con Denuncia (opcional)
    @ManyToOne
    @JoinColumn(name = "id_denuncia")
    private Denuncia denuncia;

    // Relación con Caso (opcional)
    @ManyToOne
    @JoinColumn(name = "id_caso")
    private Caso caso;

    @ManyToOne
    @JoinColumn(name = "id_usuario_subio", nullable = false)
    private Usuario usuarioSubio;

    @CreationTimestamp
    private LocalDateTime fechaSubida;
}
