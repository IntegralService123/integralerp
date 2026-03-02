package com.example.integral_erp.centrodistribuicao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.integral_erp.enums.TipoCentro;

public interface CentroDistribuicaoRepository extends JpaRepository<CentroDistribuicao, Long> {

    List<CentroDistribuicao> findByTipo(TipoCentro tipo);
}

