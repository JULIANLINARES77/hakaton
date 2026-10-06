package uniminuto.campusreporta.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uniminuto.campusreporta.entity.Ubicacion;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
    List<Ubicacion> findAllByOrderByBloqueAscSalonAsc();

    boolean existsByBloqueIgnoreCaseAndSalonIgnoreCase(String bloque, String salon);
}
