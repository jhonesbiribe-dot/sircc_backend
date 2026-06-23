package tfg.sircc_backend.model;

// Persona.java

import com.fasterxml.jackson.annotation.JsonIgnore;
import tfg.sircc_backend.model.enums.TipoPersona;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "personas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    private LocalDate fechaNacimiento;

    @Column(length = 255)
    private String direccion;

    @Column(unique = true, length = 50)
    private String documento;

    @Column(length = 100)
    private String alias;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String huellasUrl;

    @Column(length = 255)
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    private TipoPersona tipoPersona;

    // Relación inversa: un usuario puede estar asociado a esta persona
    @OneToOne(mappedBy = "persona")
    @JsonIgnore  // ✅ Agregar esto para evitar el anidamiento circular
    private Usuario usuario;

    // Relación: una persona puede estar en muchos casos (a través de caso_personas)
    @ManyToMany
    @JoinTable(
            name = "caso_personas",
            joinColumns = @JoinColumn(name = "id_persona"),
            inverseJoinColumns = @JoinColumn(name = "id_caso")
    )
    private List<Caso> casos = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}