package com.example.integral_erp.movimentacaoestoque;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long>{

    List<MovimentacaoEstoque> findByCentroId(Long centroId);
}
