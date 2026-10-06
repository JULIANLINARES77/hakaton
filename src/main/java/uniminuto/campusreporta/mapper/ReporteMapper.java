package uniminuto.campusreporta.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uniminuto.campusreporta.dto.ReporteResponseDTO;
import uniminuto.campusreporta.entity.Reporte;

@Mapper(componentModel = "spring")
public interface ReporteMapper{
    @Mapping(target = "ubicacionId", source = "ubicacion.id")
    @Mapping(target = "bloque", source = "ubicacion.bloque")
    @Mapping(target = "salon", source = "ubicacion.salon")
    @Mapping(target = "usuarioReportaId", source = "usuarioReporta.id")
    @Mapping(target = "nombreUsuarioReporta", source = "usuarioReporta.nombreCompleto")
    @Mapping(target = "correoUsuarioReporta", source = "usuarioReporta.correo")
    ReporteResponseDTO toDto(Reporte reporte);

    List<ReporteResponseDTO> toDtoList(List<Reporte> reportes);
}
