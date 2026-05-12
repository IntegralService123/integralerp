package com.example.integral_erp.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.integral_erp.produto.dto.ProdutoResponseAdminDTO;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findByCodigoBarras(String codigoBarras);

    List<Produto> findByCategoria_Id(Long categoriaId);

    List<Produto> findByCategoria_IdAndAtivoTrue(Long categoriaId);

    List<Produto> findByAtivoTrue();

    @Query("""
        SELECT p FROM Produto p
        WHERE p.ativo = true
        AND (:categoria IS NULL OR p.categoria.id = :categoria)
        AND (:q IS NULL OR p.nome ILIKE :q)
    """)
    List<Produto> buscarCatalogo(
        @Param("categoria") Long categoria,
        @Param("q") String q
    );
}
