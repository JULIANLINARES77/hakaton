package uniminuto.campusreporta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uniminuto.campusreporta.entity.RolUsuario;

public record UsuarioRequestDTO(
        @NotBlank(message = "El nombre completo es obligatorio.")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
        String nombreCompleto,
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo debe tener un formato válido.")
        @Size(max = 254, message = "El correo no puede superar 254 caracteres.")
        String correo,
        @NotNull(message = "El rol es obligatorio.")
        RolUsuario rol) {
}
