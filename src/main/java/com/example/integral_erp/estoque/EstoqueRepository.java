package com.example.integral_erp.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByProdutoIdAndCentroId(
            Long produtoId,
            Long centroId
    );

    List<Estoque> findByCentroId(
        Long centroId
    );
}
