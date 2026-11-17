
package pe.edu.upeu.PPPBACKEND.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import pe.edu.upeu.PPPBACKEND.dto.LoginRequestDTO;
import pe.edu.upeu.PPPBACKEND.dto.LoginResponseDTO;
import pe.edu.upeu.PPPBACKEND.dto.RegistroEstudianteRequestDTO;

import pe.edu.upeu.PPPBACKEND.service.service.AuthService;
import pe.edu.upeu.PPPBACKEND.service.service.RegistroEstudianteService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final RegistroEstudianteService registroEstudianteService;

    // ==========================================
    // INICIAR SESIÓN CON JWT
    // ==========================================

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {

        LoginResponseDTO respuesta = authService.login(request);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(respuesta);
    }

    // ==========================================
    // REGISTRAR ESTUDIANTE
    // ==========================================

    @PostMapping("/registro-estudiante")
    public ResponseEntity<Map<String, String>> registrarEstudiante(
            @Valid @RequestBody RegistroEstudianteRequestDTO request) {

        registroEstudianteService.registrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(Map.of(
                        "mensaje",
                        "Cuenta registrada correctamente. " +
                                "Pendiente de verificacion institucional."
                ));
    }
}
