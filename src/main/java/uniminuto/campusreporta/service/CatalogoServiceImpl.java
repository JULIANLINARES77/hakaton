package uniminuto.campusreporta.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uniminuto.campusreporta.dto.UbicacionRequestDTO;
import uniminuto.campusreporta.dto.UbicacionResponseDTO;
import uniminuto.campusreporta.dto.UsuarioRequestDTO;
import uniminuto.campusreporta.dto.UsuarioResponseDTO;
import uniminuto.campusreporta.entity.Ubicacion;
import uniminuto.campusreporta.entity.Usuario;
import uniminuto.campusreporta.exception.BusinessException;
import uniminuto.campusreporta.mapper.UbicacionMapper;
import uniminuto.campusreporta.mapper.UsuarioMapper;
import uniminuto.campusreporta.repository.UbicacionRepository;
import uniminuto.campusreporta.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class CatalogoServiceImpl implements CatalogoService {
    private final UsuarioRepository usuarioRepository;
    private final UbicacionRepository ubicacionRepository;
    private final UsuarioMapper usuarioMapper;
    private final UbicacionMapper ubicacionMapper;

    public CatalogoServiceImpl(
            UsuarioRepository usuarioRepository,
            UbicacionRepository ubicacionRepository,
            UsuarioMapper usuarioMapper,
            UbicacionMapper ubicacionMapper) {
        this.usuarioRepository = usuarioRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.usuarioMapper = usuarioMapper;
        this.ubicacionMapper = ubicacionMapper;
    }

    @Override
    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioMapper.toDtoList(usuarioRepository.findAllByOrderByNombreCompletoAsc());
    }

    @Override
    @Transactional
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request) {
        String correo = request.correo().trim();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new BusinessException("Ya existe un usuario registrado con ese correo.");
        }
        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setCorreo(correo);
        return usuarioMapper.toDto(usuarioRepository.save(usuario));
    }

    @Override
    public List<UbicacionResponseDTO> listarUbicaciones() {
        return ubicacionMapper.toDtoList(ubicacionRepository.findAllByOrderByBloqueAscSalonAsc());
    }

    @Override
    @Transactional
    public UbicacionResponseDTO crearUbicacion(UbicacionRequestDTO request) {
        if (ubicacionRepository.existsByBloqueIgnoreCaseAndSalonIgnoreCase(request.bloque().trim(), request.salon().trim())) {
            throw new BusinessException("Ya existe una ubicación con ese bloque y salón.");
        }
        Ubicacion ubicacion = ubicacionMapper.toEntity(request);
        ubicacion.setBloque(request.bloque().trim());
        ubicacion.setSalon(request.salon().trim());
        ubicacion.setDescripcion(request.descripcion() == null ? null : request.descripcion().trim());
        return ubicacionMapper.toDto(ubicacionRepository.save(ubicacion));
    }
}
