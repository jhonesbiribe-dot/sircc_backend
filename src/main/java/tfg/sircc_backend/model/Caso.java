// src/main/java/tfg/sircc_backend/model/Caso.java
package tfg.sircc_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tfg.sircc_backend.model.enums.EstadoCaso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "casos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Caso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String numeroCaso;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCaso estado = EstadoCaso.ABIERTO;

    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    // ✅ Investigador principal
    @ManyToOne
    @JoinColumn(name = "id_investigador_principal")
    private Usuario investigadorPrincipal;

    @ManyToOne
    @JoinColumn(name = "id_supervisor")
    private Usuario supervisor;

    // ✅ Denuncias asociadas
    @OneToMany(mappedBy = "caso", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Denuncia> denuncias = new ArrayList<>();

    // ✅ Personas asociadas (víctimas, imputados, testigos)
    @ManyToMany
    @JoinTable(
            name = "caso_personas",
            joinColumns = @JoinColumn(name = "id_caso"),
            inverseJoinColumns = @JoinColumn(name = "id_persona")
    )
    @JsonIgnore
    private List<Persona> personas = new ArrayList<>();

    // ✅ Evidencias asociadas
    @OneToMany(mappedBy = "caso", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Evidencia> evidencias = new ArrayList<>();

    // ✅ Avances de investigación
    @OneToMany(mappedBy = "caso", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<AvanceInvestigacion> avances = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}