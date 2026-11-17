
package pe.edu.upeu.PPPBACKEND.controller;

import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PPPBACKEND.entity.RequisitoPpp;
import pe.edu.upeu.PPPBACKEND.repository.RequisitoPppRepository;
import pe.edu.upeu.PPPBACKEND.service.service.RequisitoPppService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/requisitos")
public class RequisitoPppController {

    private final RequisitoPppService service;

    public RequisitoPppController(RequisitoPppService service) {
        this.service = service;
    }

    @GetMapping
    public List<RequisitoPpp> listar() {
        return service.listarRequisitos();
    }

    @GetMapping("/estudiante/{idUsuario}")
    public List<RequisitoPppRepository.EstadoRequisito>
    listarPorEstudiante(
            @PathVariable Long idUsuario) {
        return service.listarPorEstudiante(idUsuario);
    }
}
