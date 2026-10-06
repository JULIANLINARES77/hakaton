package uniminuto.campusreporta.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uniminuto.campusreporta.dto.CambioEstadoRequestDTO;
import uniminuto.campusreporta.dto.HistorialResponseDTO;
import uniminuto.campusreporta.dto.IndicadoresDTO;
import uniminuto.campusreporta.dto.ReporteRequestDTO;
import uniminuto.campusreporta.dto.ReporteResponseDTO;
import uniminuto.campusreporta.entity.EstadoReporte;
import uniminuto.campusreporta.service.ReporteService;

@Validated
@RestController
@RequestMapping("/api/v1/campus")
public class CampusReportaController {
    private final ReporteService reporteService;

    public CampusReportaController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping("/reportes")
    public ResponseEntity<ReporteResponseDTO> crear(@Valid @RequestBody ReporteRequestDTO request) {
        return ResponseEntity.status(201).body(reporteService.crear(request));
    }

    @GetMapping("/reportes")
    public ResponseEntity<Page<ReporteResponseDTO>> listar(
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long ubicacionId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "La página no puede ser negativa.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "El tamaño debe ser al menos 1.")
            @Max(value = 100, message = "El tamaño máximo de página es 100.") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaReporte"));
        return ResponseEntity.ok(reporteService.listar(estado, desde, hasta, usuarioId, ubicacionId, pageable));
    }

    @GetMapping("/reportes/{id}")
    public ResponseEntity<ReporteResponseDTO> obtener(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(reporteService.obtener(id));
    }

    @GetMapping("/reportes/{id}/historial")
    public ResponseEntity<java.util.List<HistorialResponseDTO>> historial(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(reporteService.historial(id));
    }

    @PutMapping("/reportes/{id}/estado")
    public ResponseEntity<ReporteResponseDTO> cambiarEstado(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody CambioEstadoRequestDTO request) {
        return ResponseEntity.ok(reporteService.cambiarEstado(id, request));
    }

    @GetMapping("/indicadores")
    public ResponseEntity<IndicadoresDTO> indicadores() {
        return ResponseEntity.ok(reporteService.obtenerIndicadores());
    }

    @GetMapping(value = "/reportes/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generarPdf(
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long ubicacionId) {
        byte[] pdf = reporteService.generarPdf(estado, desde, hasta, usuarioId, ubicacionId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("campusreporta-reportes.pdf").build());
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
