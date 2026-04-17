package com.example.integral_erp.movimentacaoestoque;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.example.integral_erp.enums.TipoMovimentacao;

import jakarta.persistence.criteria.Predicate;

public class MovimentacaoSpecification {

    public static Specification<MovimentacaoEstoque> filtro(
            TipoMovimentacao tipo,
            Long produtoId,
            Long centroId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }

            if (produtoId != null) {
                predicates.add(cb.equal(root.get("produto").get("id"), produtoId));
            }

            if (centroId != null) {
                predicates.add(cb.equal(root.get("centro").get("id"), centroId));
            }

            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dataInicio));
            }

            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dataFim));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
