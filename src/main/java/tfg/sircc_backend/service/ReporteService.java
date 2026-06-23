package tfg.sircc_backend.service;
// service/ReporteService.java


import tfg.sircc_backend.dto.request.ReporteRequest;
import tfg.sircc_backend.dto.response.EstadisticasGeneralesResponse;
import tfg.sircc_backend.dto.response.MapaCalorResponse;
import tfg.sircc_backend.dto.response.PatronDelictivoResponse;
import tfg.sircc_backend.dto.response.ReporteDelitosResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReporteService {

    // RF-10: Generación de reportes estadísticos
    EstadisticasGeneralesResponse obtenerEstadisticasGenerales(LocalDate fechaInicio, LocalDate fechaFin);
    ReporteDelitosResponse obtenerReporteDelitos(ReporteRequest request);
    byte[] exportarReporte(ReporteRequest request);  // Exportar a PDF/Excel/CSV

    // RF-11: Visualización de mapas de calor
    MapaCalorResponse obtenerMapaCalor(LocalDate fechaInicio, LocalDate fechaFin, String tipoDelito);
    MapaCalorResponse obtenerMapaCalorPorCuadrante(LocalDate fechaInicio, LocalDate fechaFin, String cuadrante);

    // RF-12: Identificación de patrones y modus operandi
    Page<PatronDelictivoResponse> identificarPatrones(Pageable pageable);
    PatronDelictivoResponse analizarModusOperandi(String modusOperandi);
    Page<PatronDelictivoResponse> buscarPorSimilitud(String descripcionMO, Pageable pageable);

    // Análisis adicionales
    byte[] generarReporteDiario();  // Reporte diario automático (CU-A-08)
}