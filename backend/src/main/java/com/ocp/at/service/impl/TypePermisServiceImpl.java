package com.ocp.at.service.impl;

import com.ocp.at.dto.request.TypePermisRequest;
import com.ocp.at.dto.response.TypePermisResponse;
import com.ocp.at.entity.TypePermis;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.TypePermisMapper;
import com.ocp.at.repository.TypePermisRepository;
import com.ocp.at.service.TypePermisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypePermisServiceImpl implements TypePermisService {

    private final TypePermisRepository typePermisRepository;
    private final TypePermisMapper typePermisMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TypePermisResponse> getAllTypePermis() {
        return typePermisRepository.findAll().stream()
                .map(typePermisMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TypePermisResponse getTypePermisById(String id) {
        TypePermis typePermis = typePermisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TypePermis non trouvé avec l'id: " + id));
        return typePermisMapper.toResponse(typePermis);
    }

    @Override
    @Transactional
    public TypePermisResponse createTypePermis(TypePermisRequest request) {
        if (typePermisRepository.existsByNom(request.getNom())) {
            throw new IllegalArgumentException("Un type de permis avec ce nom existe déjà");
        }
        TypePermis typePermis = typePermisMapper.toEntity(request);
        typePermis = typePermisRepository.save(typePermis);
        return typePermisMapper.toResponse(typePermis);
    }

    @Override
    @Transactional
    public TypePermisResponse updateTypePermis(String id, TypePermisRequest request) {
        TypePermis typePermis = typePermisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TypePermis non trouvé avec l'id: " + id));

        if (!typePermis.getNom().equals(request.getNom()) && typePermisRepository.existsByNom(request.getNom())) {
            throw new IllegalArgumentException("Un type de permis avec ce nom existe déjà");
        }

        typePermisMapper.updateEntityFromRequest(request, typePermis);
        typePermis = typePermisRepository.save(typePermis);
        return typePermisMapper.toResponse(typePermis);
    }

    @Override
    @Transactional
    public void deleteTypePermis(String id) {
        TypePermis typePermis = typePermisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TypePermis non trouvé avec l'id: " + id));
        typePermisRepository.delete(typePermis);
    }
}
