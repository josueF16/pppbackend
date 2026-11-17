package pe.edu.upeu.PPPBACKEND.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.PPPBACKEND.entity.EmpresaPpp;

import java.util.List;

public interface EmpresaPppRepository
        extends JpaRepository<EmpresaPpp, Long> {

    List<EmpresaPpp> findAllByOrderByNombreAsc();

    List<EmpresaPpp> findByRecomendadaOrderByNombreAsc(
            String recomendada
    );

    @Query(value = """
        SELECT e.*
        FROM EMPRESA_PPP e
        JOIN EMPRESA_ARCHIVADA a
          ON e.ID_EMPRESA = a.ID_EMPRESA
        WHERE a.ID_USUARIO = :idUsuario
        ORDER BY e.NOMBRE
        """, nativeQuery = true)
    List<EmpresaPpp> listarArchivadas(
            @Param("idUsuario") Long idUsuario
    );

    @Query(value = """
        SELECT COUNT(*)
        FROM EMPRESA_ARCHIVADA
        WHERE ID_USUARIO = :idUsuario
          AND ID_EMPRESA = :idEmpresa
        """, nativeQuery = true)
    long contarArchivada(
            @Param("idUsuario") Long idUsuario,
            @Param("idEmpresa") Long idEmpresa
    );

    @Modifying
    @Query(value = """
        INSERT INTO EMPRESA_ARCHIVADA
        (ID_USUARIO, ID_EMPRESA)
        VALUES (:idUsuario, :idEmpresa)
        """, nativeQuery = true)
    void archivar(
            @Param("idUsuario") Long idUsuario,
            @Param("idEmpresa") Long idEmpresa
    );

    @Modifying
    @Query(value = """
        DELETE FROM EMPRESA_ARCHIVADA
        WHERE ID_USUARIO = :idUsuario
          AND ID_EMPRESA = :idEmpresa
        """, nativeQuery = true)
    void desarchivar(
            @Param("idUsuario") Long idUsuario,
            @Param("idEmpresa") Long idEmpresa
    );

    @Query(value = """
        SELECT MAX(ID_USUARIO)
        FROM USUARIO
        WHERE CORREO = :correo
        """, nativeQuery = true)
    Long buscarIdUsuarioPorCorreo(
            @Param("correo") String correo
    );
}
