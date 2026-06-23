// src/main/java/tfg/sircc_backend/model/Denuncia.java
package tfg.sircc_backend.model;

import tfg.sircc_backend.model.enums.EstadoDenuncia;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "denuncias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String numeroDenuncia;

    private LocalDateTime fechaHora;

    @Column(length = 255)
    private String ubicacionTexto;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcionHechos;

    @Column(columnDefinition = "TEXT")
    private String modusOperandi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDenuncia estado = EstadoDenuncia.PENDIENTE;

    private LocalDateTime fechaHechosInicio;
    private LocalDateTime fechaHechosFin;

    @Column(nullable = false)
    private Boolean esAnonima = false;

    // ✅ Relaciones
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_persona_denunciante")
    private Persona denunciante;

    @ManyToOne
    @JoinColumn(name = "id_caso")
    private Caso caso;

    // ✅ RF-07: Investigador asignado
    @ManyToOne
    @JoinColumn(name = "id_investigador")
    private Usuario investigador;

    // ✅ RF-06: Delitos asociados
    @ManyToMany
    @JoinTable(
            name = "denuncia_delito",
            joinColumns = @JoinColumn(name = "id_denuncia"),
            inverseJoinColumns = @JoinColumn(name = "id_delito")
    )
    private List<Delito> delitos = new ArrayList<>();

    // ✅ Evidencias
    @OneToMany(mappedBy = "denuncia", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Evidencia> evidencias = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}