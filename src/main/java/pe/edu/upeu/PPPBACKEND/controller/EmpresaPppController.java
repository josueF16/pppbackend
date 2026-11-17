package pe.edu.upeu.PPPBACKEND.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.EmpresaPpp;
import pe.edu.upeu.PPPBACKEND.service.service.EmpresaPppService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaPppController {

    private final EmpresaPppService service;

    public EmpresaPppController(EmpresaPppService service) {
        this.service = service;
    }

    private String identidad(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED
            );
        }
        return auth.getName();
    }

    @GetMapping
    public List<EmpresaPpp> listar() {
        return service.listar();
    }

    @GetMapping("/recomendadas")
    public List<EmpresaPpp> recomendadas() {
        return service.recomendadas();
    }

    @GetMapping("/archivadas")
    public List<EmpresaPpp> archivadas(Authentication auth) {
        return service.archivadas(identidad(auth));
    }

    @GetMapping("/{id}")
    public EmpresaPpp obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping("/{id}/archivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archivar(
            @PathVariable Long id,
            Authentication auth) {
        service.archivar(identidad(auth), id);
    }

    @DeleteMapping("/{id}/archivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desarchivar(
            @PathVariable Long id,
            Authentication auth) {
        service.desarchivar(identidad(auth), id);
    }
}
