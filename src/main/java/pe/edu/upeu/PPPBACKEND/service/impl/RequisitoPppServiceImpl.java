
package pe.edu.upeu.PPPBACKEND.service.impl;

import org.springframework.stereotype.Service;
import pe.edu.upeu.PPPBACKEND.entity.RequisitoPpp;
import pe.edu.upeu.PPPBACKEND.repository.RequisitoPppRepository;
import pe.edu.upeu.PPPBACKEND.service.service.RequisitoPppService;

import java.util.List;

@Service
public class RequisitoPppServiceImpl
        implements RequisitoPppService {

    private final RequisitoPppRepository repository;

    public RequisitoPppServiceImpl(
            RequisitoPppRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RequisitoPpp> listarRequisitos() {
        return repository.findAllByOrderByIdRequisitoAsc();
    }

    @Override
    public List<RequisitoPppRepository.EstadoRequisito>
    listarPorEstudiante(Long idUsuario) {
        return repository.listarPorEstudiante(idUsuario);
    }
}
