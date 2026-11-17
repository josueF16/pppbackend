
package pe.edu.upeu.PPPBACKEND.service.service;

import pe.edu.upeu.PPPBACKEND.entity.RequisitoPpp;
import pe.edu.upeu.PPPBACKEND.repository.RequisitoPppRepository;

import java.util.List;

public interface RequisitoPppService {

    List<RequisitoPpp> listarRequisitos();

    List<RequisitoPppRepository.EstadoRequisito>
    listarPorEstudiante(Long idUsuario);
}
