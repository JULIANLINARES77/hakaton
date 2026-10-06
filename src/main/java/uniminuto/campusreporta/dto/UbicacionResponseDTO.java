package uniminuto.campusreporta.dto;

public record UbicacionResponseDTO(Long id, String bloque, String salon, String descripcion) {
    public String etiqueta() {
        return bloque + " · " + salon;
    }
}
