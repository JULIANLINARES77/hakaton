package uniminuto.campusreporta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "historial_estados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HistorialEstado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reporte", nullable = false)
    @ToString.Exclude
    private Reporte reporte;

    @Convert(converter = EstadoReporteConverter.class)
    @Column(name = "estado_anterior", length = 20)
    private EstadoReporte estadoAnterior;

    @Convert(converter = EstadoReporteConverter.class)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoReporte estadoNuevo;

    @Column(name = "fecha_cambio", nullable = false)
    private OffsetDateTime fechaCambio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_cambio", nullable = false)
    @ToString.Exclude
    private Usuario usuarioCambio;

    @Column(name = "observacion", columnDefinition = "text")
    private String observacion;

    @PrePersist
    void onCreate() {
        if (fechaCambio == null) {
            fechaCambio = OffsetDateTime.now();
        }
    }
}
