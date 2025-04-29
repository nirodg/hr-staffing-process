package org.db.hrsp.api.controller;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.db.hrsp.api.dto.ClientDTO;
import org.db.hrsp.api.dto.mapper.ClientMapper;
import org.db.hrsp.service.repository.model.Client;
import org.db.hrsp.service.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/clients")
@AllArgsConstructor
public class ClientController {

    private final ClientRepository service;
    private final ClientMapper mapper;

    @Transactional
    @PostMapping()
    public ResponseEntity<ClientDTO> addClient(@RequestBody ClientDTO client) {
        Client entity = service.save(mapper.toEntity(client));
        return ResponseEntity.ok(mapper.toDto(entity));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientDTO> getClient(@PathVariable("clientId") Long clientId) {
        Optional<Client> client = service.findById(clientId);
        return client.map(value -> ResponseEntity.ok(mapper.toDto(value))).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());

    }

    @GetMapping()
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        Iterable<Client> clients = service.findAll();
        if (!clients.iterator().hasNext()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(mapper.toDtos(clients));
    }

}
