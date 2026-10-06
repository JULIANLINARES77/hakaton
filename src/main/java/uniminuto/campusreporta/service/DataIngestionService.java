package uniminuto.campusreporta.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uniminuto.campusreporta.dto.DocumentoUsadoDTO;
import uniminuto.campusreporta.entity.EstadoReporte;
import uniminuto.campusreporta.entity.Reporte;
import uniminuto.campusreporta.repository.EstadoReporteCount;
import uniminuto.campusreporta.repository.ReporteRepository;

@Service
@Transactional(readOnly = true)
public class DataIngestionService {
    private static final int CANDIDATE_LIMIT = 100;
    private static final int DOCUMENT_LIMIT = 6;
    private static final Pattern WORDS = Pattern.compile("[\\p{L}\\p{N}]{3,}");
    private static final Set<String> STOP_WORDS = Set.of(
            "que", "cual", "cuales", "como", "cuando", "donde", "para", "con", "por",
            "del", "las", "los", "una", "uno", "unos", "unas", "hay", "mis", "sus",
            "sobre", "dime", "muestra", "informacion", "reporte", "reportes");

    private final ReporteRepository reporteRepository;

    public DataIngestionService(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    public ContextData retrieve(String question) {
        List<Reporte> candidates = reporteRepository.buscarRecientes(Pageable.ofSize(CANDIDATE_LIMIT));
        Map<EstadoReporte, Long> counts = reporteRepository.contarPorEstado().stream()
                .collect(java.util.stream.Collectors.toMap(EstadoReporteCount::getEstado, EstadoReporteCount::getCantidad));
        String summary = "Totales actuales: " + EstadoReporte.PENDIENTE.getValor() + "="
                + counts.getOrDefault(EstadoReporte.PENDIENTE, 0L) + ", "
                + EstadoReporte.EN_PROCESO.getValor() + "=" + counts.getOrDefault(EstadoReporte.EN_PROCESO, 0L)
                + ", " + EstadoReporte.RESUELTO.getValor() + "="
                + counts.getOrDefault(EstadoReporte.RESUELTO, 0L) + ", todos="
                + counts.values().stream().mapToLong(Long::longValue).sum() + ".";

        List<String> terms = terms(question);
        List<ScoredReport> ranked = candidates.stream()
                .map(report -> new ScoredReport(report, score(report, terms)))
                .sorted(Comparator.comparingInt(ScoredReport::score).reversed()
                        .thenComparing(item -> item.report().getFechaReporte(), Comparator.reverseOrder()))
                .toList();
        List<ScoredReport> selected = ranked.stream()
                .filter(item -> item.score() > 0)
                .limit(DOCUMENT_LIMIT)
                .toList();
        if (selected.isEmpty()) {
            selected = ranked.stream().limit(Math.min(3, ranked.size())).toList();
        }

        List<DocumentoUsadoDTO> documents = new ArrayList<>();
        documents.add(new DocumentoUsadoDTO(
                "indicadores",
                "reportes-estado",
                Map.of("origen", "PostgreSQL", "descripcion", "Conteo actual de reportes por estado")));
        StringBuilder context = new StringBuilder(summary);
        for (ScoredReport item : selected) {
            Reporte report = item.report();
            String id = String.valueOf(report.getId());
            String description = report.getDescripcionDano().replaceAll("[\\r\\n]+", " ").trim();
            String location = report.getUbicacion().getBloque() + " / " + report.getUbicacion().getSalon();
            String status = report.getEstado().getValor();
            String date = report.getFechaReporte().toString();
            context.append("\nReporte ").append(id)
                    .append(": estado=").append(status)
                    .append("; ubicación=").append(location)
                    .append("; fecha=").append(date)
                    .append("; descripción=").append(description);
            documents.add(new DocumentoUsadoDTO("reporte", id, Map.of(
                    "estado", status,
                    "ubicacion", location,
                    "fechaReporte", date)));
        }
        if (selected.isEmpty()) {
            context.append("\nNo hay reportes registrados en la base de datos.");
        }
        return new ContextData(context.toString(), List.copyOf(documents));
    }

    private List<String> terms(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return WORDS.matcher(normalized).results()
                .map(match -> match.group())
                .filter(word -> !STOP_WORDS.contains(word))
                .distinct()
                .toList();
    }

    private int score(Reporte report, List<String> terms) {
        if (terms.isEmpty()) {
            return 0;
        }
        String searchable = normalize(report.getDescripcionDano() + " " + report.getEstado().getValor()
                + " " + report.getUbicacion().getBloque() + " " + report.getUbicacion().getSalon());
        int score = 0;
        for (String term : terms) {
            if (searchable.contains(term)) {
                score += term.length() > 5 ? 2 : 1;
            }
        }
        return score;
    }

    private String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private record ScoredReport(Reporte report, int score) {
    }

    public record ContextData(String text, List<DocumentoUsadoDTO> documents) {
    }
}
