package com.example.integral_erp.movimentacaoestoque;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.integral_erp.enums.TipoMovimentacao;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long>{

    @Query("""
        SELECT m FROM MovimentacaoEstoque m
        WHERE (:tipo IS NULL OR m.tipo = :tipo)
        AND (:produtoId IS NULL OR m.produto.id = :produtoId)
    """)
    Page<MovimentacaoEstoque> buscarComFiltros(
        @Param("tipo") TipoMovimentacao tipo,
        @Param("produtoId") Long produtoId,
        Pageable pageable
    );

    @Query("""
        SELECT m FROM MovimentacaoEstoque m
        WHERE m.centro.id = :centroId
        AND (:tipo IS NULL OR m.tipo = :tipo)
        AND (:produtoId IS NULL OR m.produto.id = :produtoId)
    """)
    Page<MovimentacaoEstoque> buscarComFiltrosEPorCentro(
        @Param("tipo") TipoMovimentacao tipo,
        @Param("produtoId") Long produtoId,
        @Param("centroId") Long centroId,
        Pageable pageable
    );

    List<MovimentacaoEstoque> findByCentroId(Long centroId);
}
