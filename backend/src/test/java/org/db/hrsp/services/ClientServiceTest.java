package org.db.hrsp.services;

import org.db.hrsp.api.dto.ClientDTO;
import org.db.hrsp.service.ClientService;
import org.db.hrsp.service.repository.model.Client;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClientServiceTest extends AbstractServiceTest<Client, ClientDTO, ClientService> {

    @Override
    protected Client createMockEntity() {
        Client client = new Client();
        client.setId(1L);
        client.setClientName("Test Client");
        client.setClientEmail("test@example.com");
        return client;
    }

    @Override
    protected ClientDTO createMockDto() {
        ClientDTO dto = new ClientDTO();
        dto.setId(1L);
        dto.setClientName("Test Client");
        dto.setClientEmail("test@example.com");
        return dto;
    }

    @Test
    void create_ShouldReturnDto_WhenValidInput() {
        when(mapper.toEntity(any())).thenReturn(mockEntity);
        when(repository.save(any())).thenReturn(mockEntity);
        when(mapper.toDto(any())).thenReturn(mockDto);

        ClientDTO result = service.create(mockDto);

        assertNotNull(result);
        assertEquals(mockDto.getId(), result.getId());
        verify(repository).save(mockEntity);
    }

    @Test
    void getById_ShouldReturnDto_WhenEntityExists() throws Throwable {
        when(repository.findById(1L)).thenReturn(Optional.of(mockEntity));
        when(mapper.toDto(any())).thenReturn(mockDto);

        ClientDTO result = service.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_ShouldThrowException_WhenEntityNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getById(1L));
    }

    @Test
    void update_ShouldUpdateAllFields_WhenValidInput() throws Throwable {
        ClientDTO updatedDto = new ClientDTO();
        updatedDto.setClientName("Updated Name");
        updatedDto.setClientEmail("updated@example.com");

        when(repository.findById(1L)).thenReturn(Optional.of(mockEntity));
        when(repository.save(any())).thenReturn(mockEntity);
        when(mapper.toDto(any())).thenReturn(updatedDto);

        ClientDTO result = service.update(1L, updatedDto);

        assertEquals("Updated Name", result.getClientName());
        assertEquals("updated@example.com", result.getClientEmail());
        verify(mapper).update(mockEntity, updatedDto);
    }

    @Test
    void partialUpdate_ShouldUpdateOnlySpecifiedFields() throws Throwable {
        Map<String, Object> updates = Map.of(
                "clientName", "Partially Updated",
                "clientEmail", "partial@example.com"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(mockEntity));
        when(repository.save(any())).thenReturn(mockEntity);
        when(mapper.toDto(any())).thenReturn(mockDto);

        ClientDTO result = service.partialUpdate(1L, updates);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(repository).save(captor.capture());
        Client savedEntity = captor.getValue();

        assertEquals("Partially Updated", savedEntity.getClientName());
        assertEquals("partial@example.com", savedEntity.getClientEmail());
    }

    @Test
    void deleteById_ShouldDeleteEntity_WhenExists() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteById(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteById_ShouldThrowException_WhenEntityNotFound() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.deleteById(1L));
    }

    @Test
    void getAll_ShouldReturnListOfDtos() {
        when(repository.findAll()).thenReturn(Arrays.asList(mockEntity));
        when(mapper.toDto(any())).thenReturn(mockDto);

        List<ClientDTO> results = service.getAll();

        assertEquals(1, results.size());
        assertEquals(mockDto.getId(), results.get(0).getId());
    }

//    @Test
//    void getByReference_ShouldReturnDto_WhenEntityExists() {
//        when(repository.findByClientName("Test Client")).thenReturn(Optional.of(mockEntity));
//        when(mapper.toDto(any())).thenReturn(mockDto);
//
//        ClientDTO result = service.getByReference("Test Client",
//                reference -> repository.findByClientName(reference));
//
//        assertNotNull(result);
//        assertEquals(1L, result.getId());
//    }
}