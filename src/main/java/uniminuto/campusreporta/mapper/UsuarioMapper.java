package uniminuto.campusreporta.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uniminuto.campusreporta.dto.UsuarioRequestDTO;
import uniminuto.campusreporta.dto.UsuarioResponseDTO;
import uniminuto.campusreporta.entity.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper{
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Usuario toEntity(UsuarioRequestDTO request);

    UsuarioResponseDTO toDto(Usuario usuario);

    List<UsuarioResponseDTO> toDtoList(List<Usuario> usuarios);
}
