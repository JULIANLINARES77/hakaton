package uniminuto.campusreporta.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uniminuto.campusreporta.dto.UbicacionRequestDTO;
import uniminuto.campusreporta.dto.UbicacionResponseDTO;
import uniminuto.campusreporta.entity.Ubicacion;

@Mapper(componentModel = "spring")
public interface UbicacionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Ubicacion toEntity(UbicacionRequestDTO request);

    UbicacionResponseDTO toDto(Ubicacion ubicacion);

    List<UbicacionResponseDTO> toDtoList(List<Ubicacion> ubicaciones);
}
