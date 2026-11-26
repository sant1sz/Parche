package com.sant1sz.parche.controller;

import com.sant1sz.parche.dto.MenuResponse;
import com.sant1sz.parche.service.MenuService;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/negocios")
public class NegocioController {

    private final MenuService menuService;

    public NegocioController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/{id}/menu")
    public MenuResponse menu(@PathVariable @Positive(message = "Debe ser un número mayor que cero.") Long id) {
        return menuService.obtenerMenu(id);
    }
}
