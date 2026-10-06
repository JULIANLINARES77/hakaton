package uniminuto.campusreporta.service;

import java.util.List;
import uniminuto.campusreporta.dto.UbicacionRequestDTO;
import uniminuto.campusreporta.dto.UbicacionResponseDTO;
import uniminuto.campusreporta.dto.UsuarioRequestDTO;
import uniminuto.campusreporta.dto.UsuarioResponseDTO;

public interface CatalogoService {
    List<UsuarioResponseDTO> listarUsuarios();

    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request);

    List<UbicacionResponseDTO> listarUbicaciones();

    UbicacionResponseDTO crearUbicacion(UbicacionRequestDTO request);
}
