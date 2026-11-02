package com.tiocantinas.senac.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiocantinas.senac.dto.caixa.AbrirCaixaRequest;
import com.tiocantinas.senac.dto.caixa.CaixaResponse;
import com.tiocantinas.senac.dto.caixa.FecharCaixaRequest;
import com.tiocantinas.senac.service.CaixaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/caixa")
@RequiredArgsConstructor
public class CaixaController {

    private final CaixaService caixaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CaixaResponse abrirCaixa(@RequestBody @Valid AbrirCaixaRequest request) {
        return caixaService.abrir(request);
    }

    @GetMapping("/aberto")
    public ResponseEntity<CaixaResponse> buscarCaixaAberto() {
        return caixaService.buscarAberto()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/fechados")
    public Page<CaixaResponse> listarCaixasFechados(
            @PageableDefault(size = 10, sort = "abertura", direction = Sort.Direction.DESC) Pageable pageable,

            @RequestParam(required = false) String responsavel,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {

        return caixaService.listarFechados(
                pageable,
                responsavel,
                dataInicial,
                dataFinal);
    }

    @PatchMapping("/{id}/fechar")
    public CaixaResponse fecharCaixa(@PathVariable Long id, @Valid @RequestBody FecharCaixaRequest request) {
        return caixaService.fechar(id, request);
    }

}
