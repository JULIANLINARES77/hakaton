package uniminuto.campusreporta.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uniminuto.campusreporta.entity.RolUsuario;
import uniminuto.campusreporta.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreoIgnoreCase(String correo);

    List<Usuario> findAllByOrderByNombreCompletoAsc();

    List<Usuario> findByRolOrderByNombreCompletoAsc(RolUsuario rol);
}
