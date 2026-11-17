
package pe.edu.upeu.PPPBACKEND.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.entity.EmpresaPpp;
import pe.edu.upeu.PPPBACKEND.repository.EmpresaPppRepository;
import pe.edu.upeu.PPPBACKEND.service.service.EmpresaPppService;

import java.util.List;

@Service
public class EmpresaPppServiceImpl
        implements EmpresaPppService {

    private final EmpresaPppRepository repository;

    public EmpresaPppServiceImpl(EmpresaPppRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<EmpresaPpp> listar() {
        return repository.findAllByOrderByNombreAsc();
    }

    @Override
    public List<EmpresaPpp> recomendadas() {
        return repository.findByRecomendadaOrderByNombreAsc("S");
    }

    @Override
    public EmpresaPpp obtener(Long idEmpresa) {
        return repository.findById(idEmpresa)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Empresa no encontrada"
                ));
    }

    private Long obtenerIdUsuario(String identidad) {
        if (identidad == null || identidad.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (identidad.matches("\\d+")) {
            return Long.valueOf(identidad);
        }

        Long id = repository.buscarIdUsuarioPorCorreo(identidad);

        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuario no identificado"
            );
        }

        return id;
    }

    @Override
    public List<EmpresaPpp> archivadas(String identidad) {
        return repository.listarArchivadas(
                obtenerIdUsuario(identidad)
        );
    }

    @Override
    @Transactional
    public void archivar(String identidad, Long idEmpresa) {
        Long idUsuario = obtenerIdUsuario(identidad);

        obtener(idEmpresa);

        if (repository.contarArchivada(idUsuario, idEmpresa) == 0) {
            repository.archivar(idUsuario, idEmpresa);
        }
    }

    @Override
    @Transactional
    public void desarchivar(String identidad, Long idEmpresa) {
        repository.desarchivar(
                obtenerIdUsuario(identidad),
                idEmpresa
        );
    }
}
