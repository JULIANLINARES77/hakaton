package uniminuto.campusreporta.repository;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uniminuto.campusreporta.entity.EstadoReporte;
import uniminuto.campusreporta.entity.Reporte;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    @Query(value = """
            SELECT r FROM Reporte r
            JOIN FETCH r.ubicacion
            JOIN FETCH r.usuarioReporta
            WHERE (:filtrarEstado = false OR r.estado = :estado)
              AND (:filtrarDesde = false OR r.fechaReporte >= :desde)
              AND (:filtrarHasta = false OR r.fechaReporte < :hastaExclusivo)
              AND (:filtrarUsuario = false OR r.usuarioReporta.id = :usuarioId)
              AND (:filtrarUbicacion = false OR r.ubicacion.id = :ubicacionId)
            ORDER BY r.fechaReporte DESC
              """,
              countQuery = """
                      SELECT COUNT(r) FROM Reporte r
                      WHERE (:filtrarEstado = false OR r.estado = :estado)
                        AND (:filtrarDesde = false OR r.fechaReporte >= :desde)
                        AND (:filtrarHasta = false OR r.fechaReporte < :hastaExclusivo)
                        AND (:filtrarUsuario = false OR r.usuarioReporta.id = :usuarioId)
                        AND (:filtrarUbicacion = false OR r.ubicacion.id = :ubicacionId)
                      """)
    Page<Reporte> buscarFiltrados(
            @Param("filtrarEstado") boolean filtrarEstado,
            @Param("estado") EstadoReporte estado,
            @Param("filtrarDesde") boolean filtrarDesde,
            @Param("desde") OffsetDateTime desde,
            @Param("filtrarHasta") boolean filtrarHasta,
            @Param("hastaExclusivo") OffsetDateTime hastaExclusivo,
            @Param("filtrarUsuario") boolean filtrarUsuario,
            @Param("usuarioId") Long usuarioId,
            @Param("filtrarUbicacion") boolean filtrarUbicacion,
            @Param("ubicacionId") Long ubicacionId,
            Pageable pageable);

    @Query("""
            SELECT r.estado AS estado, COUNT(r) AS cantidad
            FROM Reporte r
            GROUP BY r.estado
            """)
    List<EstadoReporteCount> contarPorEstado();

    @Query("""
            SELECT r FROM Reporte r
            JOIN FETCH r.ubicacion
            JOIN FETCH r.usuarioReporta
            ORDER BY r.fechaReporte DESC
            """)
    List<Reporte> buscarRecientes(Pageable pageable);
}
