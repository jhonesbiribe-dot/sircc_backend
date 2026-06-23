package tfg.sircc_backend.model;

// Usuario.java

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tfg.sircc_backend.model.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;  // Se almacenará con BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

    @Column(nullable = false)
    private Boolean estado = true;  // TRUE: activo, FALSE: inhabilitado

    // Relación con Persona (uno a uno)
    @OneToOne
    @JoinColumn(name = "id_persona")
    @JsonIgnore  // ✅ Agregar esto para evitar el anidamiento circular
    private Persona persona;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // En Usuario.java, agregar la relación con Denuncias
    @OneToMany(mappedBy = "usuario")
    private List<Denuncia> denuncias = new ArrayList<>();
}