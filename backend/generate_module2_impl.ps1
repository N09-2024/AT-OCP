$basePath = "C:\Users\pc\.gemini\antigravity-ide\scratch\ocp-at-system\backend\src\main\java\com\ocp\at"
$entities = @(
    @{ Name="Zone"; Fields=@("nomZone", "descriptionZone", "codeZone"); Lower="zone" },
    @{ Name="Service"; Fields=@("nomService", "descriptionService", "codeService", "zoneId"); Lower="service" },
    @{ Name="Installation"; Fields=@("nomInstallation", "atelier", "localisation", "codeInstallation", "serviceId"); Lower="installation" },
    @{ Name="Equipement"; Fields=@("nomEquipement", "codeEquipement", "descriptionEquipement", "installationId"); Lower="equipement" },
    @{ Name="EntrepriseExterne"; Fields=@("nomEntreprise", "adresse", "telephone", "responsable"); Lower="entrepriseExterne" },
    @{ Name="Risque"; Fields=@("nomRisque", "descriptionRisque", "niveau"); Lower="risque" },
    @{ Name="MesurePreparation"; Fields=@("nomMesure", "descriptionMesure"); Lower="mesurePreparation" },
    @{ Name="EPI"; Fields=@("nomepi", "descriptionepi"); Lower="epi" },
    @{ Name="MoyenAcces"; Fields=@("nomMoyen", "descriptionMoyen"); Lower="moyenAcces" }
)

foreach ($entity in $entities) {
    $name = $entity.Name
    $lower = $entity.Lower
    $fields = $entity.Fields

    # ServiceImpl
    $serviceImplPath = "$basePath\service\impl\$name`ServiceImpl.java"
    $serviceImplContent = "package com.ocp.at.service.impl;`n`nimport com.ocp.at.dto.request.$name`Request;`nimport com.ocp.at.dto.response.$name`Response;`nimport com.ocp.at.entity.$name;`nimport com.ocp.at.exception.BusinessException;`nimport com.ocp.at.exception.ResourceNotFoundException;`nimport com.ocp.at.mapper.$name`Mapper;`nimport com.ocp.at.repository.$name`Repository;`nimport com.ocp.at.service.$name`Service;`nimport lombok.RequiredArgsConstructor;`nimport lombok.extern.slf4j.Slf4j;`nimport org.springframework.data.domain.Page;`nimport org.springframework.data.domain.Pageable;`nimport org.springframework.data.jpa.domain.Specification;`nimport org.springframework.stereotype.Service;`nimport org.springframework.transaction.annotation.Transactional;`n`nimport java.util.List;`nimport java.util.stream.Collectors;`n`n"
    
    # Imports for related repositories
    if ($name -eq "Zone") {
        $serviceImplContent += "import com.ocp.at.repository.ServiceRepository;`n"
    } elseif ($name -eq "Service") {
        $serviceImplContent += "import com.ocp.at.repository.InstallationRepository;`n"
    } elseif ($name -eq "Installation") {
        $serviceImplContent += "import com.ocp.at.repository.EquipementRepository;`n"
    }

    $serviceImplContent += "`n@Service`n@RequiredArgsConstructor`n@Slf4j`npublic class $name`ServiceImpl implements $name`Service {`n`n    private final $name`Repository repository;`n    private final $name`Mapper mapper;`n"

    # Inject related repos
    if ($name -eq "Zone") {
        $serviceImplContent += "    private final ServiceRepository serviceRepository;`n"
    } elseif ($name -eq "Service") {
        $serviceImplContent += "    private final InstallationRepository installationRepository;`n"
    } elseif ($name -eq "Installation") {
        $serviceImplContent += "    private final EquipementRepository equipementRepository;`n"
    }

    $serviceImplContent += "`n    @Override`n    @Transactional`n    public $name`Response create($name`Request request) {`n        log.info(""Création d'un(e) $name"");`n        $name entity = mapper.toEntity(request);`n        entity = repository.save(entity);`n        return mapper.toResponse(entity);`n    }`n`n"
    $serviceImplContent += "    @Override`n    @Transactional`n    public $name`Response update(String id, $name`Request request) {`n        log.info(""Modification d'un(e) $name avec ID: {}"", id);`n        $name entity = repository.findById(id)`n                .orElseThrow(() -> new ResourceNotFoundException(""$name non trouvé(e)""));`n        mapper.updateEntityFromRequest(request, entity);`n        entity = repository.save(entity);`n        return mapper.toResponse(entity);`n    }`n`n"
    $serviceImplContent += "    @Override`n    public $name`Response getById(String id) {`n        log.info(""Consultation d'un(e) $name avec ID: {}"", id);`n        return repository.findById(id)`n                .map(mapper::toResponse)`n                .orElseThrow(() -> new ResourceNotFoundException(""$name non trouvé(e)""));`n    }`n`n"
    $serviceImplContent += "    @Override`n    public List<$name`Response> getAll() {`n        log.info(""Consultation de tous/toutes les $name"");`n        return repository.findAll().stream()`n                .map(mapper::toResponse)`n                .collect(Collectors.toList());`n    }`n`n"
    
    $serviceImplContent += "    @Override`n    public Page<$name`Response> search(String query, Pageable pageable) {`n        log.info(""Recherche $name avec query: {}"", query);`n        Specification<$name> spec = Specification.where(null);`n        // Implement search logic if needed`n        return repository.findAll(spec, pageable).map(mapper::toResponse);`n    }`n`n"

    $serviceImplContent += "    @Override`n    @Transactional`n    public void delete(String id) {`n        log.info(""Suppression d'un(e) $name avec ID: {}"", id);`n        $name entity = repository.findById(id)`n                .orElseThrow(() -> new ResourceNotFoundException(""$name non trouvé(e)""));`n`n"

    # Deletion constraints
    if ($name -eq "Zone") {
        $serviceImplContent += "        if (serviceRepository.existsByZoneId(id)) {`n            throw new BusinessException(""Impossible de supprimer une Zone qui contient des Services."");`n        }`n"
    } elseif ($name -eq "Service") {
        $serviceImplContent += "        if (installationRepository.existsByServiceId(id)) {`n            throw new BusinessException(""Impossible de supprimer un Service qui contient des Installations."");`n        }`n"
    } elseif ($name -eq "Installation") {
        $serviceImplContent += "        if (equipementRepository.existsByInstallationId(id)) {`n            throw new BusinessException(""Impossible de supprimer une Installation qui contient des Equipements."");`n        }`n"
    } else {
        $serviceImplContent += "        // Check if used in AT before deleting (A implémenter)`n"
    }

    $serviceImplContent += "        repository.delete(entity);`n    }`n}`n"
    Set-Content -Path $serviceImplPath -Value $serviceImplContent
}
