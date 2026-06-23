package tfg.sircc_backend.model;

// model/Auditoria.java (Tabla 54 de tu diccionario)


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 50)
    private String operacion;  // INSERT, UPDATE, DELETE, SELECT, LOGIN, LOGOUT, EXPORT

    @Column(length = 50)
    private String tablaAfectada;

    private Long registroId;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(length = 45)
    private String ipOrigen;
}
