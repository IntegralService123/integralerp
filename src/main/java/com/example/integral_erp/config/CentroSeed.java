package com.example.integral_erp.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.integral_erp.centrodistribuicao.CentroDistribuicao;
import com.example.integral_erp.centrodistribuicao.CentroDistribuicaoRepository;
import com.example.integral_erp.enums.TipoCentro;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Order(1)
public class CentroSeed implements CommandLineRunner {

    private final CentroDistribuicaoRepository centroRepository;

    @Override
    public void run(String... args) {

        boolean existeBase = centroRepository
            .findByTipo(TipoCentro.BASE)
            .stream()
            .findFirst()
            .isPresent();

        if (!existeBase) {
            CentroDistribuicao centro = new CentroDistribuicao();
            centro.setNome("Centro Base");
            centro.setTipo(TipoCentro.BASE);

            centroRepository.save(centro);

            System.out.println("Centro BASE criado");
        }

        if (existeBase) {
            CentroDistribuicao base = centroRepository.findByTipo(TipoCentro.BASE)
                .stream()
                .findFirst()
                .orElseThrow();

            if (base.getNome() == null) {
                base.setNome("Centro Base");
                centroRepository.save(base);
            }
        }
    }
}
