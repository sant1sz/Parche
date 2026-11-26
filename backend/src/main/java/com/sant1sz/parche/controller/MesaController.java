package com.sant1sz.parche.controller;

import com.sant1sz.parche.dto.MesaResponse;
import com.sant1sz.parche.service.MesaService;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    /** Lo usa el QR o la etiqueta NFC de la mesa (fase 1). */
    @GetMapping("/{token}")
    public MesaResponse porToken(
            @PathVariable
            @Pattern(regexp = "^[A-Za-z0-9_-]{16,64}$", message = "El código de la mesa no tiene un formato válido.")
            String token) {
        return mesaService.obtenerPorToken(token);
    }
}
