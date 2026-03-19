package com.example.integral_erp.pedido;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long>{

    List<Pedido> findByUsuarioId(Long usuarioId);

    Optional<Pedido> findByIdAndUsuarioId(Long id, Long usuarioId);
}
