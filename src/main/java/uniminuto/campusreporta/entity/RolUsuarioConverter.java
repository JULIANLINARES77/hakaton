package uniminuto.campusreporta.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RolUsuarioConverter implements AttributeConverter<RolUsuario, String> {
    @Override
    public String convertToDatabaseColumn(RolUsuario attribute) {
        return attribute == null ? null : attribute.getValor();
    }

    @Override
    public RolUsuario convertToEntityAttribute(String dbData) {
        return dbData == null ? null : RolUsuario.desdeValor(dbData);
    }
}
