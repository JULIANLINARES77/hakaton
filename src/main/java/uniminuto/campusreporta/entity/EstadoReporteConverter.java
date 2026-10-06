package uniminuto.campusreporta.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EstadoReporteConverter implements AttributeConverter<EstadoReporte, String> {
    @Override
    public String convertToDatabaseColumn(EstadoReporte attribute) {
        return attribute == null ? null : attribute.getValor();
    }

    @Override
    public EstadoReporte convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EstadoReporte.desdeValor(dbData);
    }
}
