package org.db.hrsp.services;

import org.db.hrsp.api.dto.AbstractEntityDto;
import org.db.hrsp.api.dto.mapper.AbstractMapper;
import org.db.hrsp.service.AbstractService;
import org.db.hrsp.service.repository.model.util.AbstractEntity;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.repository.CrudRepository;

public abstract class AbstractServiceTest<Entity extends AbstractEntity,
        Dto extends AbstractEntityDto,
        Service extends AbstractService<Entity, Dto, ?, ?>> {

    @InjectMocks
    protected CrudRepository<Entity, Long> repository;

    @InjectMocks
    protected AbstractMapper<Entity, Dto> mapper;

    @InjectMocks
    protected Service service;

    protected Entity mockEntity;
    protected Dto mockDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockEntity = createMockEntity();
        mockDto = createMockDto();
    }

    protected abstract Entity createMockEntity();
    protected abstract Dto createMockDto();
}