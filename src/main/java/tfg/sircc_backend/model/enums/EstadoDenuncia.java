package tfg.sircc_backend.model.enums;

public enum EstadoDenuncia {
    PENDIENTE,      // Recién creada por ciudadano
    VALIDADA,       // Revisada y aprobada por administrativo
    INVESTIGACION,  // Asignada a investigador
    ARCHIVADA,      // Rechazada o cerrada sin acción
    RESUELTA        // Caso cerrado exitosamente
}
