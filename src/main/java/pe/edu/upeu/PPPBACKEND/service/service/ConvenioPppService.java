
package pe.edu.upeu.PPPBACKEND.service.service;

import pe.edu.upeu.PPPBACKEND.entity.ConvenioPpp;
import java.time.LocalDate;

public interface ConvenioPppService {

    ConvenioPpp registrar(
            Long idEstudiante,
            Long idSolicitud,
            String cargo,
            String horario,
            String pago,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    ConvenioPpp obtener(
            Long idEstudiante,
            Long idSolicitud
    );
}
