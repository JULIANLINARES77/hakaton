package uniminuto.campusreporta.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uniminuto.campusreporta.dto.CambioEstadoRequestDTO;
import uniminuto.campusreporta.dto.IndicadoresDTO;
import uniminuto.campusreporta.dto.HistorialResponseDTO;
import uniminuto.campusreporta.dto.ReporteRequestDTO;
import uniminuto.campusreporta.dto.ReporteResponseDTO;
import uniminuto.campusreporta.entity.EstadoReporte;

public interface ReporteService {
    ReporteResponseDTO crear(ReporteRequestDTO request);

    Page<ReporteResponseDTO> listar(EstadoReporte estado, LocalDate desde, LocalDate hasta, Long usuarioId, Long ubicacionId, Pageable pageable);

    ReporteResponseDTO obtener(Long id);

    List<HistorialResponseDTO> historial(Long id);

    ReporteResponseDTO cambiarEstado(Long id, CambioEstadoRequestDTO request);

    IndicadoresDTO obtenerIndicadores();

    byte[] generarPdf(EstadoReporte estado, LocalDate desde, LocalDate hasta, Long usuarioId, Long ubicacionId);
}
