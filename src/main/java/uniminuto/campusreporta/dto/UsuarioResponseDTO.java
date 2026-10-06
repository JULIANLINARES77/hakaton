package uniminuto.campusreporta.dto;

import uniminuto.campusreporta.entity.RolUsuario;

public record UsuarioResponseDTO(Long id, String nombreCompleto, String correo, RolUsuario rol) {
}
