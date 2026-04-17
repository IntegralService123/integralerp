package com.example.integral_erp.pagamento;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long>{

    Optional<Pagamento> findByPedidoId(Long pedidoId);

    Optional<Pagamento> findByTransacaoGatewayId(String transacaoId);
}
