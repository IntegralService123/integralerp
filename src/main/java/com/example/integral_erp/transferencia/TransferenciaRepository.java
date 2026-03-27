package com.example.integral_erp.transferencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {
    
    Optional<Transferencia> findByCodigo(String codigo);

    List<Transferencia> findByDestinoId(Long destinoId);
}
