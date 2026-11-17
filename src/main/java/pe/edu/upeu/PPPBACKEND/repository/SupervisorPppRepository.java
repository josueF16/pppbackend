
package pe.edu.upeu.PPPBACKEND.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.PPPBACKEND.entity.SupervisorPpp;

public interface SupervisorPppRepository
        extends JpaRepository<SupervisorPpp, Long> {

    boolean existsByCorreoIgnoreCase(String correo);
}
