package uniminuto.campusreporta.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uniminuto.campusreporta.entity.HistorialEstado;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    @Query("""
            SELECT h FROM HistorialEstado h
            JOIN FETCH h.usuarioCambio
            WHERE h.reporte.id = :reporteId
            ORDER BY h.fechaCambio DESC, h.id DESC
            """)
    List<HistorialEstado> buscarPorReporteOrdenado(@Param("reporteId") Long reporteId);
}
