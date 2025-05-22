package org.db.hrsp.service;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.db.hrsp.api.common.ConflictException;
import org.db.hrsp.api.common.NotFoundException;
import org.db.hrsp.api.common.UnexpectedException;
import org.db.hrsp.api.dto.mapper.AbstractMapper;
import org.db.hrsp.service.repository.model.util.AbstractEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unchecked")
@NoArgsConstructor(force = true)
public abstract class AbstractService<Entity extends AbstractEntity, AbstractEntityDto extends org.db.hrsp.api.dto.AbstractEntityDto, Repo extends CrudRepository, Mapper extends AbstractMapper> {

    @Autowired
    private final Repo repository;

    @Autowired
    private final Mapper mapper;

    // CREATE
    @Transactional
    public AbstractEntityDto create(AbstractEntityDto dto) {
        verifyIntegrity();
        if (dto == null) throw new IllegalArgumentException("DTO cannot be null");

        Entity entity = (Entity) mapper.toEntity(dto);

        Entity saved;
        try {
            saved = (Entity) repository.save(entity);
        } catch (DataIntegrityViolationException dup) {
            throw new ConflictException("Client with ID '%s' already exists".formatted(dto.getId()));
        } catch (RuntimeException ex) {
            throw new UnexpectedException("Error saving client");
        }

        return (AbstractEntityDto) mapper.toDto(saved);
    }

    // READ
    public AbstractEntityDto getById(Long id) throws Throwable {
        verifyIntegrity();
        return (AbstractEntityDto) repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("Client %d not found".formatted(id)));
    }

    public <R> AbstractEntityDto getByReference(R reference, Function<R, Optional<Entity>> finder) {
        verifyIntegrity();
        return (AbstractEntityDto) finder.apply(reference)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("Entity not found with reference: " + reference));
    }

    public List<AbstractEntityDto> getAll() {
        verifyIntegrity();
        ArrayList<AbstractEntityDto> items = new ArrayList<>();
        repository.findAll().forEach(e -> items.add((AbstractEntityDto) mapper.toDto(e)));
        return items;
    }

    // UPDATE - Full update (PUT)
    @Transactional
    public AbstractEntityDto update(Long id, AbstractEntityDto dto) throws Throwable {
        if (mapper == null || repository == null) return null;
        Entity entity = (Entity) repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entity not found"));
        mapper.update(entity, dto);
        return (AbstractEntityDto) mapper.toDto(repository.save(entity));
    }


    // UPDATE - Partial update (PATCH)
    @Transactional
    public AbstractEntityDto partialUpdate(Long id, Map<String, Object> updates) throws Throwable {
        verifyIntegrity();
        Entity entity = (Entity) repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entity not found with id: " + id));

        // Apply partial updates
        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(entity.getClass(), key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, entity, value);
            }
        });

        return (AbstractEntityDto) mapper.toDto(repository.save(entity));
    }

    // DELETE
    @Transactional
    public void deleteById(Long id) {
        verifyIntegrity();
        if (!repository.existsById(id)) {
            throw new RuntimeException("Entity not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public <R> void deleteByReference(R reference, Function<R, Optional<Entity>> finder) {
        verifyIntegrity();
        Entity entity = finder.apply(reference)
                .orElseThrow(() -> new RuntimeException("Entity not found with reference: " + reference));
        repository.delete(entity);
    }

    private void verifyIntegrity() {
        if (mapper == null || repository == null)
            throw new IllegalArgumentException("The mapper and/or repository is null");
    }
}
