package org.db.hrsp.controller;

import org.db.hrsp.dto.ClientDTO;
import org.db.hrsp.service.ClientService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @Transactional
    @PostMapping()
    public ResponseEntity<ClientDTO> addClient(@RequestBody ClientDTO client) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createClient(client));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientDTO> getClient(@PathVariable("clientId") Long clientId) {
        ClientDTO client = service.getClient(clientId);

        return client.getId() != null
                ? ResponseEntity.ok(client)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping()
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        List<ClientDTO> clients = service.getAllClients();
        if (clients.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clients);
    }

    @Transactional
    @PutMapping("/{clientId}")
    public ResponseEntity<ClientDTO> updateClient(@RequestBody ClientDTO client) {
        return ResponseEntity.ok(service.updateClient(client));
    }

    @Transactional
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable("clientId") Long clientId) {
        service.deleteClientById(clientId);
        return ResponseEntity.noContent().build();
    }
}
