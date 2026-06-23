package tfg.sircc_backend.controller;

// controller/ReporteController.java

import tfg.sircc_backend.dto.request.ReporteRequest;
import tfg.sircc_backend.dto.response.EstadisticasGeneralesResponse;
import tfg.sircc_backend.dto.response.MapaCalorResponse;
import tfg.sircc_backend.dto.response.PatronDelictivoResponse;
import tfg.sircc_backend.dto.response.ReporteDelitosResponse;
import tfg.sircc_backend.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = {"http://localhost:4200", "https://sircc.vercel.app"}, allowCredentials = "true") // ✅ Solo cambiado aquí
@Tag(name = "Reportes", description = "Endpoints para generación de reportes y análisis criminal")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    /**
     * RF-10: Estadísticas generales
     * GET /api/reportes/estadisticas
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ANALISTA', 'ADMINISTRATIVO', 'INVESTIGADOR', 'ADMIN')")
    @Operation(summary = "Obtener estadísticas generales delictivas")
    public ResponseEntity<EstadisticasGeneralesResponse> obtenerEstadisticas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        EstadisticasGeneralesResponse response = reporteService.obtenerEstadisticasGenerales(fechaInicio, fechaFin);
        return ResponseEntity.ok(response);
    }

    /**
     * RF-10: Reporte de delitos
     * POST /api/reportes/delitos
     */
    @PostMapping("/delitos")
    @PreAuthorize("hasAnyRole('ANALISTA', 'ADMIN')")
    @Operation(summary = "Generar reporte detallado de delitos")
    public ResponseEntity<ReporteDelitosResponse> generarReporteDelitos(@RequestBody ReporteRequest request) {
        ReporteDelitosResponse response = reporteService.obtenerReporteDelitos(request);
        return ResponseEntity.ok(response);
    }

    /**
     * RF-10: Exportar reporte (PDF/Excel/CSV)
     * POST /api/reportes/exportar
     */
    @PostMapping("/exportar")
    @PreAuthorize("hasAnyRole('ANALISTA', 'ADMIN')")
    @Operation(summary = "Exportar reporte en formato PDF, Excel o CSV")
    public ResponseEntity<byte[]> exportarReporte(@RequestBody ReporteRequest request) {
        byte[] contenido = reporteService.exportarReporte(request);

        String extension = request.getFormato() != null ? request.getFormato().toLowerCase() : "json";
        MediaType mediaType;
        String filename;

        switch (extension) {
            case "pdf":
                mediaType = MediaType.APPLICATION_PDF;
                filename = "reporte_delitos.pdf";
                break;
            case "csv":
                mediaType = MediaType.TEXT_PLAIN;
                filename = "reporte_delitos.csv";
                break;
            default:
                mediaType = MediaType.APPLICATION_JSON;
                filename = "reporte_delitos.json";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(mediaType)
                .body(contenido);
    }

    /**
     * RF-10: Reporte diario (para CU-A-08)
     * GET /api/reportes/diario
     */
    @GetMapping("/diario")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'ANALISTA', 'ADMIN')")
    @Operation(summary = "Generar reporte diario automático")
    public ResponseEntity<byte[]> generarReporteDiario() {
        byte[] contenido = reporteService.generarReporteDiario();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_diario.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    /**
     * RF-11: Mapa de calor
     * GET /api/reportes/mapa-calor
     */
    @GetMapping("/mapa-calor")
    @PreAuthorize("hasAnyRole('ANALISTA', 'INVESTIGADOR', 'ADMIN')")
    @Operation(summary = "Obtener datos para mapa de calor de delitos")
    public ResponseEntity<MapaCalorResponse> obtenerMapaCalor(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String tipoDelito) {

        MapaCalorResponse response = reporteService.obtenerMapaCalor(fechaInicio, fechaFin, tipoDelito);
        return ResponseEntity.ok(response);
    }

    /**
     * RF-11: Mapa de calor por cuadrante policial
     * GET /api/reportes/mapa-calor/cuadrante/{cuadrante}
     */
    @GetMapping("/mapa-calor/cuadrante/{cuadrante}")
    @PreAuthorize("hasAnyRole('ANALISTA', 'INVESTIGADOR', 'ADMIN')")
    @Operation(summary = "Obtener mapa de calor por cuadrante policial")
    public ResponseEntity<MapaCalorResponse> obtenerMapaCalorPorCuadrante(
            @PathVariable String cuadrante,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        MapaCalorResponse response = reporteService.obtenerMapaCalorPorCuadrante(fechaInicio, fechaFin, cuadrante);
        return ResponseEntity.ok(response);
    }

    /**
     * RF-12: Identificar patrones delictivos
     * GET /api/reportes/patrones
     */
    @GetMapping("/patrones")
    @PreAuthorize("hasAnyRole('ANALISTA', 'INVESTIGADOR', 'ADMIN')")
    @Operation(summary = "Identificar patrones delictivos basados en modus operandi")
    public ResponseEntity<Page<PatronDelictivoResponse>> identificarPatrones(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<PatronDelictivoResponse> response = reporteService.identificarPatrones(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * RF-12: Analizar modus operandi
     * POST /api/reportes/analizar-mo
     */
    @PostMapping("/analizar-mo")
    @PreAuthorize("hasAnyRole('ANALISTA', 'INVESTIGADOR', 'ADMIN')")
    @Operation(summary = "Analizar un modus operandi específico y buscar casos similares")
    public ResponseEntity<PatronDelictivoResponse> analizarModusOperandi(@RequestBody String modusOperandi) {
        PatronDelictivoResponse response = reporteService.analizarModusOperandi(modusOperandi);
        return ResponseEntity.ok(response);
    }

    /**
     * RF-08, RF-12: Buscar por similitud de modus operandi
     * GET /api/reportes/buscar-mo?descripcion=...
     */
    @GetMapping("/buscar-mo")
    @PreAuthorize("hasAnyRole('INVESTIGADOR', 'ANALISTA', 'ADMIN')")
    @Operation(summary = "Buscar casos por similitud en el modus operandi")
    public ResponseEntity<Page<PatronDelictivoResponse>> buscarPorSimilitud(
            @RequestParam String descripcion,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<PatronDelictivoResponse> response = reporteService.buscarPorSimilitud(descripcion, pageable);
        return ResponseEntity.ok(response);
    }
}