package tfg.sircc_backend.model;

// model/CasoPersona.java (Tabla 51 de tu diccionario)


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "caso_personas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoPersona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_caso", nullable = false)
    private Caso caso;

    @ManyToOne
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    @Column(nullable = false)
    private String rolEnCaso;  // VICTIMA, IMPUTADO, TESTIGO, DENUNCIANTE, PERITO, OTRO

    @Column(nullable = false)
    private LocalDateTime fechaAsociacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}