package com.example.integral_erp.produto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findByCodigoBarras(String codigoBarras);

    //List<Produto> findByCentro_IdAndCategoria_Id(Long centroId, Long categoriaId);

    List<Produto> findByCategoria_Id(Long categoriaId);

    List<Produto> findByCategoria_IdAndAtivoTrue(Long categoriaId);

    //List<Produto> findByCentro_Id(Long centroId);

    //List<Produto> findByCentro_IdAndAtivoTrue(Long centroId);

    //List<Produto> findByCentro_IdAndCategoria_IdAndAtivoTrue(Long centroId, Long categoriaId);

    List<Produto> findByAtivoTrue();
}
