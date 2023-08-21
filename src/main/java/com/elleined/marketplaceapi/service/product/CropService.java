package com.elleined.marketplaceapi.service.product;

import com.elleined.marketplaceapi.exception.ResourceNotFoundException;
import com.elleined.marketplaceapi.mapper.BaseMapper;
import com.elleined.marketplaceapi.model.Crop;
import com.elleined.marketplaceapi.repository.CropRepository;
import com.elleined.marketplaceapi.service.BaseEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CropService implements BaseEntityService<Crop> {

    private final CropRepository cropRepository;
    private final BaseMapper baseMapper;

    @Override
    public Crop getById(int id) throws ResourceNotFoundException {
        return cropRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Crop with id of " + id + " does not exists!"));
    }

    @Override
    public boolean existsById(int id) {
        return cropRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return cropRepository.findAll().stream()
                .map(Crop::getName)
                .anyMatch(name::equalsIgnoreCase);
    }

    @Override
    public List<String> getAll() {
        return cropRepository.findAll().stream()
                .map(Crop::getName)
                .sorted()
                .toList();
    }

    @Override
    public Crop getByName(String name) throws ResourceNotFoundException {
        return cropRepository.findAll().stream()
                .filter(crop -> crop.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Crop with name of " + name + " does not exists!"));
    }

    @Override
    public Crop save(String name) {
        Crop crop = baseMapper.toCropEntity(name);
        cropRepository.save(crop);
        log.debug("Crop with name of {} saved successfully with id of {}", crop.getName(), crop.getId());
        return crop;
    }
}