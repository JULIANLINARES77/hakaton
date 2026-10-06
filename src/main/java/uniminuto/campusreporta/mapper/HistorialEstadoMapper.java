package uniminuto.campusreporta.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uniminuto.campusreporta.dto.HistorialResponseDTO;
import uniminuto.campusreporta.entity.HistorialEstado;

@Mapper(componentModel = "spring")
public interface HistorialEstadoMapper{
    @Mapping(target = "reporteId", source = "reporte.id")
    @Mapping(target = "usuarioCambioId", source = "usuarioCambio.id")
    @Mapping(target = "nombreUsuarioCambio", source = "usuarioCambio.nombreCompleto")
    HistorialResponseDTO toDto(HistorialEstado historialEstado);

    List<HistorialResponseDTO> toDtoList(List<HistorialEstado> historial);
}
