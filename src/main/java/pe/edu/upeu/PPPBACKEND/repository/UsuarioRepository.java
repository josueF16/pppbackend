
package pe.edu.upeu.PPPBACKEND.repository;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.upeu.PPPBACKEND.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    // Buscar usuario por correo
    Optional<Usuario> findByCorreo(String correo);

    // Verificar correo duplicado
    boolean existsByCorreo(String correo);

    // Verificar DNI duplicado
    boolean existsByDni(String dni);

    // Listar estudiantes segun rol y estado
    List<Usuario> findByRolAndEstadoOrderByIdUsuarioAsc(
            String rol,
            String estado
    );

    // Bloquear un usuario durante su aprobacion o rechazo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT u
            FROM Usuario u
            WHERE u.idUsuario = :idUsuario
            """)
    Optional<Usuario> findByIdForUpdate(
            @Param("idUsuario") Long idUsuario
    );
}
