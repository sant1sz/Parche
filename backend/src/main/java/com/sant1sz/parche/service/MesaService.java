package com.sant1sz.parche.service;

import com.sant1sz.parche.dto.MesaResponse;
import com.sant1sz.parche.dto.NegocioResumenResponse;
import com.sant1sz.parche.exception.RecursoNoEncontradoException;
import com.sant1sz.parche.repository.MesaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    /** Resuelve el token del QR o NFC de una mesa. Las mesas o negocios inactivos no se encuentran. */
    @Transactional(readOnly = true)
    public MesaResponse obtenerPorToken(String token) {
        return mesaRepository.buscarActivaPorToken(token)
                .map(mesa -> new MesaResponse(NegocioResumenResponse.de(mesa.getNegocio()), mesa.getNumero()))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No encontramos esta mesa. Pide ayuda al personal del lugar."));
    }
}
