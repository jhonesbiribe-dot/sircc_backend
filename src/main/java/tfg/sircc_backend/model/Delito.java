package tfg.sircc_backend.model;

// Delito.java

import tfg.sircc_backend.model.enums.GravedadDelito;
import tfg.sircc_backend.model.enums.CategoriaDelito;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "delitos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GravedadDelito gravedad;

    @Enumerated(EnumType.STRING)
    private CategoriaDelito categoria;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}