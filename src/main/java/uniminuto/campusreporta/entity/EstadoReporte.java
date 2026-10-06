package uniminuto.campusreporta.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoReporte {
    PENDIENTE("Pendiente"),
    EN_PROCESO("En Proceso"),
    RESUELTO("Resuelto");

    private final String valor;

    EstadoReporte(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static EstadoReporte desdeValor(String valor) {
        for (EstadoReporte estado : values()) {
            if (estado.valor.equalsIgnoreCase(valor) || estado.name().equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado no válido: " + valor);
    }
}
