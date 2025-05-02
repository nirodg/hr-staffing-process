package org.db.hrsp.api.controller;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.db.hrsp.api.dto.ClientDTO;
import org.db.hrsp.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/clients")
@AllArgsConstructor
public class ClientController {


    private final ClientService clientService;

    @Transactional
    @PostMapping()
    public ResponseEntity<ClientDTO> addClient(@RequestBody ClientDTO client) {
        return ResponseEntity.ok(clientService.createClient(client));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientDTO> getClient(@PathVariable("clientId") Long clientId) {
        return clientService.findById(clientId);
    }

    @GetMapping()
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        return clientService.findAll();
    }

}
