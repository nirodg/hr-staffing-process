package org.db.hrsp.api.dto.mapper;

import org.db.hrsp.api.dto.ClientDTO;
import org.db.hrsp.service.repository.model.Client;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    Client toEntity(ClientDTO dto);
    ClientDTO toDto(Client entity);

    List<ClientDTO> toDtos(Iterable<Client> clients);
}
