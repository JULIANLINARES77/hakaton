package uniminuto.campusreporta.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RolUsuario {
    ESTUDIANTE("Estudiante"),
    DOCENTE("Docente"),
    MANTENIMIENTO("Mantenimiento");

    private final String valor;

    RolUsuario(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static RolUsuario desdeValor(String valor) {
        for (RolUsuario rol : values()) {
            if (rol.valor.equalsIgnoreCase(valor)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Rol no válido: " + valor);
    }
}
