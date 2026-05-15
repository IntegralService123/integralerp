package com.example.integral_erp.common;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC_PATTERN = Pattern.compile("[^a-z0-9\\s]");

    /**
     * Remove acentos, caracteres especiais de pontuação de acento e converte para minúsculo.
     * Exemplo: "Ímã de Neodímio" -> "ima de neodimio"
     */
    public static String normalizarParaBusca(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        // Decompõe os caracteres acentuados (NFD)
        String textoNormalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        textoNormalizado = DIACRITICS_PATTERN.matcher(textoNormalizado).replaceAll("");

        textoNormalizado = textoNormalizado.toLowerCase();

        // 3. Remove caracteres especiais (", /, -, ., etc) mantendo apenas letras, números e espaços
        textoNormalizado = NON_ALPHANUMERIC_PATTERN.matcher(textoNormalizado).replaceAll("");
        
        // 4. Remove espaços extras (duplos espaços que podem sobrar) e trim
        return textoNormalizado.replaceAll("\\s+", " ").trim();
    }
}
