package tfg.sircc_backend.service.impl;

// service/impl/ReporteServiceImpl.java


import tfg.sircc_backend.dto.request.ReporteRequest;
import tfg.sircc_backend.dto.response.*;
import tfg.sircc_backend.model.*;
import tfg.sircc_backend.model.enums.EstadoCaso;
import tfg.sircc_backend.model.enums.EstadoDenuncia;
import tfg.sircc_backend.repository.*;
import tfg.sircc_backend.service.ReporteService;
import tfg.sircc_backend.service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteServiceImpl implements ReporteService {

    @Autowired
    private DenunciaRepository denunciaRepository;

    @Autowired
    private CasoRepository casoRepository;

    @Autowired
    private DelitoRepository delitoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    private static final DateTimeFormatter MES_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public EstadisticasGeneralesResponse obtenerEstadisticasGenerales(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDateTime.now().minusMonths(12);
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : LocalDateTime.now();

        List<Denuncia> denuncias = denunciaRepository.findAll().stream()
                .filter(d -> d.getCreatedAt() != null &&
                        !d.getCreatedAt().isBefore(inicio) &&
                        !d.getCreatedAt().isAfter(fin))
                .collect(Collectors.toList());

        List<Caso> casos = casoRepository.findAll().stream()
                .filter(c -> c.getCreatedAt() != null &&
                        !c.getCreatedAt().isBefore(inicio) &&
                        !c.getCreatedAt().isAfter(fin))
                .collect(Collectors.toList());

        EstadisticasGeneralesResponse response = new EstadisticasGeneralesResponse();

        // Resumen general
        EstadisticasGeneralesResponse.ResumenGeneral resumen = new EstadisticasGeneralesResponse.ResumenGeneral();
        resumen.setTotalDenuncias((long) denuncias.size());
        resumen.setDenunciasPendientes(denuncias.stream().filter(d -> d.getEstado() == EstadoDenuncia.PENDIENTE).count());
        resumen.setDenunciasEnInvestigacion(denuncias.stream().filter(d -> d.getEstado() == EstadoDenuncia.INVESTIGACION).count());
        resumen.setDenunciasResueltas(denuncias.stream().filter(d -> d.getEstado() == EstadoDenuncia.RESUELTA).count());
        resumen.setCasosAbiertos((long) casos.stream().filter(c -> c.getEstado() != EstadoCaso.CERRADO).count());
        resumen.setTotalPersonasRegistradas(personaRepository.count());

        double tasaResolucion = resumen.getTotalDenuncias() > 0
                ? (resumen.getDenunciasResueltas() * 100.0 / resumen.getTotalDenuncias())
                : 0.0;
        resumen.setTasaResolucion(Math.round(tasaResolucion * 10.0) / 10.0);
        response.setResumen(resumen);

        // Denuncias por estado
        Map<String, Long> denunciasPorEstado = new LinkedHashMap<>();
        for (EstadoDenuncia estado : EstadoDenuncia.values()) {
            long count = denuncias.stream().filter(d -> d.getEstado() == estado).count();
            if (count > 0) {
                denunciasPorEstado.put(estado.name(), count);
            }
        }
        response.setDenunciasPorEstado(denunciasPorEstado);

        // Delitos más comunes
        Map<String, Long> delitosCount = new HashMap<>();
        for (Denuncia d : denuncias) {
            if (d.getDelitos() != null) {
                for (Delito delito : d.getDelitos()) {
                    delitosCount.merge(delito.getNombre(), 1L, Long::sum);
                }
            }
        }
        Map<String, Long> delitosMasComunes = delitosCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        response.setDelitosMasComunes(delitosMasComunes);

        // Tendencia mensual
        Map<String, Long> denunciasPorMes = new LinkedHashMap<>();
        Map<String, Double> variacionPorcentual = new LinkedHashMap<>();

        for (int i = 11; i >= 0; i--) {
            LocalDateTime mesInicio = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
            LocalDateTime mesFin = mesInicio.plusMonths(1).minusSeconds(1);
            String mesKey = mesInicio.format(MES_FORMATTER);

            long count = denuncias.stream()
                    .filter(d -> d.getCreatedAt() != null &&
                            !d.getCreatedAt().isBefore(mesInicio) &&
                            !d.getCreatedAt().isAfter(mesFin))
                    .count();
            denunciasPorMes.put(mesKey, count);
        }

        // Calcular variación porcentual
        List<Long> valores = new ArrayList<>(denunciasPorMes.values());
        for (int i = 1; i < valores.size(); i++) {
            String mesKey = new ArrayList<>(denunciasPorMes.keySet()).get(i);
            long anterior = valores.get(i - 1);
            long actual = valores.get(i);
            double variacion = anterior > 0 ? ((actual - anterior) * 100.0 / anterior) : (actual > 0 ? 100.0 : 0.0);
            variacionPorcentual.put(mesKey, Math.round(variacion * 10.0) / 10.0);
        }

        EstadisticasGeneralesResponse.TendenciaMensual tendencia = new EstadisticasGeneralesResponse.TendenciaMensual();
        tendencia.setDenunciasPorMes(denunciasPorMes);
        tendencia.setVariacionPorcentual(variacionPorcentual);
        response.setTendenciaUltimosMeses(tendencia);

        response.setFechaGeneracion(LocalDateTime.now());

        return response;
    }

    @Override
    public ReporteDelitosResponse obtenerReporteDelitos(ReporteRequest request) {
        LocalDateTime inicio = request.getFechaInicio() != null
                ? request.getFechaInicio().atStartOfDay()
                : LocalDateTime.now().minusMonths(6);
        LocalDateTime fin = request.getFechaFin() != null
                ? request.getFechaFin().atTime(23, 59, 59)
                : LocalDateTime.now();

        List<Denuncia> denuncias = denunciaRepository.findAll().stream()
                .filter(d -> d.getCreatedAt() != null &&
                        !d.getCreatedAt().isBefore(inicio) &&
                        !d.getCreatedAt().isAfter(fin))
                .collect(Collectors.toList());

        // Filtrar por tipo de delito si se especifica
        if (request.getTipoDelito() != null && !request.getTipoDelito().isEmpty()) {
            denuncias = denuncias.stream()
                    .filter(d -> d.getDelitos() != null &&
                            d.getDelitos().stream().anyMatch(del -> del.getNombre().toLowerCase().contains(request.getTipoDelito().toLowerCase())))
                    .collect(Collectors.toList());
        }

        ReporteDelitosResponse response = new ReporteDelitosResponse();

        // Período
        ReporteDelitosResponse.Periodo periodo = new ReporteDelitosResponse.Periodo();
        periodo.setInicio(request.getFechaInicio() != null ? request.getFechaInicio() : LocalDate.now().minusMonths(6));
        periodo.setFin(request.getFechaFin() != null ? request.getFechaFin() : LocalDate.now());
        periodo.setTotalDias(ChronoUnit.DAYS.between(periodo.getInicio(), periodo.getFin()) + 1);
        response.setPeriodo(periodo);

        // Estadísticas por delito
        Map<String, Long> delitosCount = new HashMap<>();
        Map<String, Long> delitosPorCategoria = new HashMap<>();

        for (Denuncia d : denuncias) {
            if (d.getDelitos() != null) {
                for (Delito delito : d.getDelitos()) {
                    delitosCount.merge(delito.getNombre(), 1L, Long::sum);
                    if (delito.getCategoria() != null) {
                        delitosPorCategoria.merge(delito.getCategoria().name(), 1L, Long::sum);
                    }
                }
            }
        }

        long totalDelitos = delitosCount.values().stream().mapToLong(Long::longValue).sum();

        List<ReporteDelitosResponse.DelitoEstadistica> delitosEstadistica = new ArrayList<>();
        for (Map.Entry<String, Long> entry : delitosCount.entrySet()) {
            ReporteDelitosResponse.DelitoEstadistica est = new ReporteDelitosResponse.DelitoEstadistica();
            est.setNombre(entry.getKey());
            est.setCantidad(entry.getValue());
            est.setPorcentaje(totalDelitos > 0 ? (entry.getValue() * 100.0 / totalDelitos) : 0.0);

            // Buscar delito en BD para obtener más datos
            delitoRepository.findAll().stream()
                    .filter(d -> d.getNombre().equals(entry.getKey()))
                    .findFirst()
                    .ifPresent(d -> {
                        est.setCodigo(d.getCodigo());
                        est.setCategoria(d.getCategoria() != null ? d.getCategoria().name() : null);
                        est.setGravedad(d.getGravedad().name());
                    });

            delitosEstadistica.add(est);
        }

        delitosEstadistica.sort((a, b) -> Long.compare(b.getCantidad(), a.getCantidad()));
        response.setDelitos(delitosEstadistica);
        response.setDelitosPorCategoria(delitosPorCategoria);

        // Distribución por hora del día
        Map<String, Double> delitosPorHora = new LinkedHashMap<>();
        for (int hora = 0; hora < 24; hora++) {
            final int horaFinal = hora;
            long count = denuncias.stream()
                    .filter(d -> d.getFechaHora() != null && d.getFechaHora().getHour() == horaFinal)
                    .count();
            double porcentaje = totalDelitos > 0 ? (count * 100.0 / totalDelitos) : 0.0;
            delitosPorHora.put(String.format("%02d:00", hora), Math.round(porcentaje * 10.0) / 10.0);
        }
        response.setDelitosPorHora(delitosPorHora);

        // Distribución por día de semana
        Map<String, Long> delitosPorDiaSemana = new LinkedHashMap<>();
        String[] dias = {"LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO"};
        for (String dia : dias) {
            delitosPorDiaSemana.put(dia, 0L);
        }

        for (Denuncia d : denuncias) {
            if (d.getFechaHora() != null) {
                int diaSemana = d.getFechaHora().getDayOfWeek().getValue();
                String diaNombre = dias[diaSemana - 1];
                delitosPorDiaSemana.merge(diaNombre, 1L, Long::sum);
            }
        }
        response.setDelitosPorDiaSemana(delitosPorDiaSemana);

        return response;
    }

    @Override
    public byte[] exportarReporte(ReporteRequest request) {
        ReporteDelitosResponse reporte = obtenerReporteDelitos(request);
        try {
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(reporte);
            return json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Error al serializar reporte: " + e.getMessage(), e);
        }
    }

    @Override
    public MapaCalorResponse obtenerMapaCalor(LocalDate fechaInicio, LocalDate fechaFin, String tipoDelito) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDateTime.now().minusMonths(12);
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : LocalDateTime.now();

        List<Denuncia> denuncias = denunciaRepository.findAll().stream()
                .filter(d -> d.getCreatedAt() != null &&
                        !d.getCreatedAt().isBefore(inicio) &&
                        !d.getCreatedAt().isAfter(fin) &&
                        d.getLatitud() != null && d.getLongitud() != null)
                .collect(Collectors.toList());

        // Filtrar por tipo de delito
        if (tipoDelito != null && !tipoDelito.isEmpty()) {
            denuncias = denuncias.stream()
                    .filter(d -> d.getDelitos() != null &&
                            d.getDelitos().stream().anyMatch(del -> del.getNombre().toLowerCase().contains(tipoDelito.toLowerCase())))
                    .collect(Collectors.toList());
        }

        MapaCalorResponse response = new MapaCalorResponse();

        // Agrupar por coordenadas cercanas (redondeando a 3 decimales ~ 100m)
        Map<String, MapaCalorResponse.PuntoCalor> puntosMap = new HashMap<>();

        for (Denuncia d : denuncias) {
            String key = String.format("%.3f,%.3f", d.getLatitud(), d.getLongitud());
            MapaCalorResponse.PuntoCalor punto = puntosMap.get(key);
            if (punto == null) {
                punto = new MapaCalorResponse.PuntoCalor();
                punto.setLatitud(d.getLatitud().doubleValue());
                punto.setLongitud(d.getLongitud().doubleValue());
                punto.setIntensidad(0L);
                punto.setDireccion(d.getUbicacionTexto());
                puntosMap.put(key, punto);
            }
            punto.setIntensidad(punto.getIntensidad() + 1);
            if (d.getDelitos() != null && !d.getDelitos().isEmpty()) {
                punto.setTipoDelito(d.getDelitos().get(0).getNombre());
            }
        }

        response.setPuntos(new ArrayList<>(puntosMap.values()));

        // Configuración del mapa
        MapaCalorResponse.ConfiguracionMapa config = new MapaCalorResponse.ConfiguracionMapa();
        config.setLatitudCentro(1.6508);  // Centro aproximado de Guinea Ecuatorial
        config.setLongitudCentro(10.2679);
        config.setZoom(10);
        config.setColoresCalor(new String[]{"#00FF00", "#FFFF00", "#FF0000"});
        response.setConfiguracion(config);

        return response;
    }

    @Override
    public MapaCalorResponse obtenerMapaCalorPorCuadrante(LocalDate fechaInicio, LocalDate fechaFin, String cuadrante) {
        // Implementación similar a obtenerMapaCalor pero agregando filtro por cuadrante
        // En producción se definirían cuadrantes geográficos predefinidos
        return obtenerMapaCalor(fechaInicio, fechaFin, null);
    }

    @Override
    public Page<PatronDelictivoResponse> identificarPatrones(Pageable pageable) {
        // Identificar denuncias con modus operandi similar
        List<Denuncia> denunciasConMO = denunciaRepository.findAll().stream()
                .filter(d -> d.getModusOperandi() != null && !d.getModusOperandi().isEmpty())
                .collect(Collectors.toList());

        // Agrupar por similitud de modus operandi (simplificado)
        Map<String, List<Denuncia>> patronesMap = new HashMap<>();

        for (Denuncia d : denunciasConMO) {
            String moNormalizado = normalizarModusOperandi(d.getModusOperandi());
            patronesMap.computeIfAbsent(moNormalizado, k -> new ArrayList<>()).add(d);
        }

        List<PatronDelictivoResponse> patrones = new ArrayList<>();
        long id = 1;

        for (Map.Entry<String, List<Denuncia>> entry : patronesMap.entrySet()) {
            if (entry.getValue().size() >= 2) {  // Al menos 2 casos similares
                PatronDelictivoResponse patron = new PatronDelictivoResponse();
                patron.setId(id++);
                patron.setNombrePatron("Patrón " + id);
                patron.setModusOperandi(entry.getKey());
                patron.setNivelConfianza(Math.min(85.0 + entry.getValue().size() * 2, 99.0));
                patron.setFechaIdentificacion(LocalDateTime.now());

                List<PatronDelictivoResponse.CasoVinculado> casosVinculados = new ArrayList<>();
                for (Denuncia d : entry.getValue()) {
                    if (d.getCaso() != null) {
                        PatronDelictivoResponse.CasoVinculado cv = new PatronDelictivoResponse.CasoVinculado();
                        cv.setCasoId(d.getCaso().getId());
                        cv.setNumeroCaso(d.getCaso().getNumeroCaso());
                        cv.setNombre(d.getCaso().getNombre());
                        cv.setFechaApertura(d.getCaso().getFechaApertura());
                        cv.setSimilitud(85.0 + entry.getValue().size() * 5);
                        casosVinculados.add(cv);
                    }
                }
                patron.setCasosVinculados(casosVinculados);

                // Perfil sospechoso inferido
                PatronDelictivoResponse.PerfilSospechoso perfil = new PatronDelictivoResponse.PerfilSospechoso();
                perfil.setModusOperandiComunes(List.of(entry.getKey()));
                perfil.setZonasFrecuentes(entry.getValue().stream()
                        .map(Denuncia::getUbicacionTexto)
                        .filter(Objects::nonNull)
                        .distinct()
                        .limit(3)
                        .collect(Collectors.toList()));
                patron.setPerfilSospechoso(perfil);

                patrones.add(patron);
            }
        }

        // Paginación manual
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), patrones.size());
        List<PatronDelictivoResponse> paginatedList = patrones.subList(start, end);

        return new PageImpl<>(paginatedList, pageable, patrones.size());
    }

    @Override
    public PatronDelictivoResponse analizarModusOperandi(String modusOperandi) {
        PatronDelictivoResponse response = new PatronDelictivoResponse();
        response.setModusOperandi(modusOperandi);
        response.setNivelConfianza(0.0);

        // Buscar casos similares
        List<Denuncia> casosSimilares = denunciaRepository.findAll().stream()
                .filter(d -> d.getModusOperandi() != null &&
                        calcularSimilitud(d.getModusOperandi(), modusOperandi) > 0.5)
                .collect(Collectors.toList());

        List<PatronDelictivoResponse.CasoVinculado> vinculados = new ArrayList<>();
        for (Denuncia d : casosSimilares) {
            if (d.getCaso() != null) {
                PatronDelictivoResponse.CasoVinculado cv = new PatronDelictivoResponse.CasoVinculado();
                cv.setCasoId(d.getCaso().getId());
                cv.setNumeroCaso(d.getCaso().getNumeroCaso());
                cv.setNombre(d.getCaso().getNombre());
                cv.setSimilitud(calcularSimilitud(d.getModusOperandi(), modusOperandi) * 100);
                vinculados.add(cv);
            }
        }
        response.setCasosVinculados(vinculados);

        if (!vinculados.isEmpty()) {
            response.setNivelConfianza(vinculados.stream().mapToDouble(PatronDelictivoResponse.CasoVinculado::getSimilitud).average().orElse(0.0));
        }

        return response;
    }

    @Override
    public Page<PatronDelictivoResponse> buscarPorSimilitud(String descripcionMO, Pageable pageable) {
        List<Denuncia> denuncias = denunciaRepository.findAll().stream()
                .filter(d -> d.getModusOperandi() != null &&
                        calcularSimilitud(d.getModusOperandi().toLowerCase(), descripcionMO.toLowerCase()) > 0.3)
                .collect(Collectors.toList());

        List<PatronDelictivoResponse> resultados = new ArrayList<>();
        for (Denuncia d : denuncias) {
            if (d.getCaso() != null) {
                PatronDelictivoResponse patron = new PatronDelictivoResponse();
                patron.setModusOperandi(d.getModusOperandi());
                patron.setNivelConfianza(calcularSimilitud(d.getModusOperandi(), descripcionMO) * 100);

                PatronDelictivoResponse.CasoVinculado cv = new PatronDelictivoResponse.CasoVinculado();
                cv.setCasoId(d.getCaso().getId());
                cv.setNumeroCaso(d.getCaso().getNumeroCaso());
                cv.setNombre(d.getCaso().getNombre());
                patron.setCasosVinculados(List.of(cv));
                resultados.add(patron);
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), resultados.size());
        List<PatronDelictivoResponse> paginatedList = resultados.subList(start, end);

        return new PageImpl<>(paginatedList, pageable, resultados.size());
    }

    @Override
    public byte[] generarReporteDiario() {
        // Reporte diario automático para CU-A-08
        LocalDate hoy = LocalDate.now();
        ReporteRequest request = new ReporteRequest();
        request.setFechaInicio(hoy);
        request.setFechaFin(hoy);
        request.setFormato("PDF");

        return exportarReporte(request);
    }

    private String normalizarModusOperandi(String mo) {
        // Normalizar para agrupar modus operandi similares
        return mo.toLowerCase()
                .replaceAll("[^a-záéíóúñ\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private double calcularSimilitud(String texto1, String texto2) {
        if (texto1 == null || texto2 == null) return 0.0;

        String t1 = normalizarModusOperandi(texto1);
        String t2 = normalizarModusOperandi(texto2);

        // Similitud simple basada en palabras comunes
        Set<String> palabras1 = new HashSet<>(Arrays.asList(t1.split(" ")));
        Set<String> palabras2 = new HashSet<>(Arrays.asList(t2.split(" ")));

        if (palabras1.isEmpty() || palabras2.isEmpty()) return 0.0;

        Set<String> interseccion = new HashSet<>(palabras1);
        interseccion.retainAll(palabras2);

        Set<String> union = new HashSet<>(palabras1);
        union.addAll(palabras2);

        return (double) interseccion.size() / union.size();
    }
}