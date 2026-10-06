package uniminuto.campusreporta.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uniminuto.campusreporta.dto.UbicacionRequestDTO;
import uniminuto.campusreporta.dto.UbicacionResponseDTO;
import uniminuto.campusreporta.dto.UsuarioRequestDTO;
import uniminuto.campusreporta.dto.UsuarioResponseDTO;
import uniminuto.campusreporta.service.CatalogoService;

@RestController
@RequestMapping("/api/v1/campus")
public class CatalogoController {
    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(catalogoService.listarUsuarios());
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponseDTO> crearUsuario(@Valid @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.status(201).body(catalogoService.crearUsuario(request));
    }

    @GetMapping("/ubicaciones")
    public ResponseEntity<List<UbicacionResponseDTO>> listarUbicaciones() {
        return ResponseEntity.ok(catalogoService.listarUbicaciones());
    }

    @PostMapping("/ubicaciones")
    public ResponseEntity<UbicacionResponseDTO> crearUbicacion(@Valid @RequestBody UbicacionRequestDTO request) {
        return ResponseEntity.status(201).body(catalogoService.crearUbicacion(request));
    }
}
