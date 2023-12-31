package com.elleined.marketplaceapi.service;

import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.mapper.CropMapper;
import com.elleined.marketplaceapi.model.Crop;
import com.elleined.marketplaceapi.repository.CropRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CropService {

    private final CropRepository cropRepository;
    private final CropMapper cropMapper;

    public Crop getById(int id) throws ResourceNotFoundException {
        return cropRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Crop does not exists!"));
    }


    public boolean notExist(String name) {
        return cropRepository.findAll().stream()
                .map(Crop::getName)
                .noneMatch(name::equalsIgnoreCase);
    }

    public List<String> getAll() {
        return cropRepository.findAll().stream()
                .map(Crop::getName)
                .sorted()
                .toList();
    }

    public Crop getByName(String name) throws ResourceNotFoundException {
        return cropRepository.findAll().stream()
                .filter(crop -> crop.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Crop does not exists!"));
    }

    public Crop save(String name) {
        Crop crop = cropMapper.toEntity(name);
        cropRepository.save(crop);
        log.debug("Crop with name of {} saved successfully with id of {}", crop.getName(), crop.getId());
        return crop;
    }
}
