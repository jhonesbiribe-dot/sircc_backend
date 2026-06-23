package tfg.sircc_backend.model;

// model/SeguimientoCaso.java (Tabla 53 de tu diccionario)

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seguimiento_casos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoCaso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_caso", nullable = false)
    private Caso caso;

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
