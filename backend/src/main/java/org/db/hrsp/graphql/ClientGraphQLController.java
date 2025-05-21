package org.db.hrsp.graphql;

import lombok.RequiredArgsConstructor;
import org.db.hrsp.api.dto.ClientDTO;
import org.db.hrsp.api.dto.mapper.ClientMapper;
import org.db.hrsp.service.ClientService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ClientGraphQLController {

    private final ClientService clientService;

    @QueryMapping
    public List<ClientDTO> getClients() {
        return clientService.getAllClients();
    }

    @QueryMapping
    public ClientDTO getClient(@Argument Long id) {
        return clientService.getClient(id);
    }

    @MutationMapping
    public ClientDTO addClient(
            @Argument String clientName,
            @Argument String clientEmail,
            @Argument String contactPersonName,
            @Argument String contactPersonEmail,
            @Argument String contactPersonPhone
    ) {
        ClientDTO dto = new ClientDTO();
        dto.setClientName(clientName);
        dto.setClientEmail(clientEmail);
        dto.setContactPersonName(contactPersonName);
        dto.setContactPersonEmail(contactPersonEmail);
        dto.setContactPersonPhone(contactPersonPhone);

        return clientService.createClient(dto);
    }
}
