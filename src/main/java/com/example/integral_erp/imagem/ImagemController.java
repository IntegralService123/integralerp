package com.example.integral_erp.imagem;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/imagens")
@RequiredArgsConstructor
public class ImagemController {

    private final ImagemService imagemService;

    @PostMapping("/upload")
    public String uploadImagem(@RequestParam MultipartFile file) throws Exception {
        
        return imagemService.upload(file);
    }
}
