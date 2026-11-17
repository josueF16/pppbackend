
package pe.edu.upeu.PPPBACKEND.service.service;

import pe.edu.upeu.PPPBACKEND.entity.EmpresaPpp;
import java.util.List;

public interface EmpresaPppService {

    List<EmpresaPpp> listar();

    List<EmpresaPpp> recomendadas();

    EmpresaPpp obtener(Long idEmpresa);

    List<EmpresaPpp> archivadas(String identidad);

    void archivar(String identidad, Long idEmpresa);

    void desarchivar(String identidad, Long idEmpresa);
}
