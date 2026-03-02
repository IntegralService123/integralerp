package com.example.integral_erp.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByProdutoIdAndCentro(
            Long produtoId,
            Long centroId
    );
}
