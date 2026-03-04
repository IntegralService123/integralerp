package com.example.integral_erp.venda;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findByCentroId (Long centroId);
}
