package tfg.sircc_backend.model;

// model/SeguimientoDenuncia.java (Tabla 52 de tu diccionario)

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seguimiento_denuncias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoDenuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_denuncia", nullable = false)
    private Denuncia denuncia;

    @Column(length = 50)
    private String estadoAnterior;

    @Column(nullable = false, length = 50)
    private String estadoNuevo;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(nullable = false)
    private LocalDateTime fechaCambio;

    @ManyToOne
    @JoinColumn(name = "id_usuario_cambio", nullable = false)
    private Usuario usuarioCambio;
}
