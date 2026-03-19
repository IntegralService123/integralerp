package com.example.integral_erp.carrinho;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long>{

    Optional<Carrinho> findByUsuarioId(Long usuarioid);
} 
