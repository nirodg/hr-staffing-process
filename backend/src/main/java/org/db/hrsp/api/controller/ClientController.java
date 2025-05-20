package org.db.hrsp.api.controller;

import java.util.List;

import org.db.hrsp.api.dto.ClientDTO;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/clients")
@AllArgsConstructor
@LogMethodExecution
public class ClientController {

    private final ClientService clientService;

    @PostMapping()
    public ClientDTO addClient(@RequestBody ClientDTO client) {
        return clientService.createClient(client);
    }

    @GetMapping("/{clientId}")
    public ClientDTO getClient(@PathVariable("clientId") Long clientId) {
        return clientService.getClient(clientId);
    }

    @GetMapping()
    public List<ClientDTO> getAllClients() {
        return clientService.getAllClients();
    }

}
