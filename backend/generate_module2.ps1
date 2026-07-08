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

    # Request DTO
    $reqDtoPath = "$basePath\dto\request\$name`Request.java"
    $reqContent = "package com.ocp.at.dto.request;`n`nimport jakarta.validation.constraints.NotBlank;`nimport lombok.AllArgsConstructor;`nimport lombok.Builder;`nimport lombok.Data;`nimport lombok.NoArgsConstructor;`n`n@Data`n@NoArgsConstructor`n@AllArgsConstructor`n@Builder`npublic class $name`Request {`n"
    foreach ($field in $fields) {
        if ($field -match "nom" -or $field -match "code") {
            $reqContent += "    @NotBlank(message = ""Le champ $field est obligatoire"")`n"
        }
        $reqContent += "    private String $field;`n"
    }
    $reqContent += "}`n"
    Set-Content -Path $reqDtoPath -Value $reqContent

    # Response DTO
    $resDtoPath = "$basePath\dto\response\$name`Response.java"
    $resContent = "package com.ocp.at.dto.response;`n`nimport lombok.AllArgsConstructor;`nimport lombok.Builder;`nimport lombok.Data;`nimport lombok.NoArgsConstructor;`n`n@Data`n@NoArgsConstructor`n@AllArgsConstructor`n@Builder`npublic class $name`Response {`n    private String id;`n"
    foreach ($field in $fields) {
        if ($field -match "Id$") {
            $parentName = $field.Substring(0, $field.Length - 2)
            $capitalizedParent = $parentName.Substring(0,1).ToUpper() + $parentName.Substring(1)
            $resContent += "    private $capitalizedParent`Response $parentName;`n"
        } else {
            $resContent += "    private String $field;`n"
        }
    }
    $resContent += "}`n"
    Set-Content -Path $resDtoPath -Value $resContent

    # Mapper
    $mapperPath = "$basePath\mapper\$name`Mapper.java"
    $mapperContent = "package com.ocp.at.mapper;`n`nimport com.ocp.at.dto.request.$name`Request;`nimport com.ocp.at.dto.response.$name`Response;`nimport com.ocp.at.entity.$name;`nimport org.mapstruct.Mapper;`nimport org.mapstruct.Mapping;`nimport org.mapstruct.MappingTarget;`n`n@Mapper(componentModel = ""spring"")`npublic interface $name`Mapper {`n"
    
    if ($name -eq "Service") {
        $mapperContent += "    @Mapping(target = ""zone.id"", source = ""zoneId"")`n"
    } elseif ($name -eq "Installation") {
        $mapperContent += "    @Mapping(target = ""service.id"", source = ""serviceId"")`n"
    } elseif ($name -eq "Equipement") {
        $mapperContent += "    @Mapping(target = ""installation.id"", source = ""installationId"")`n"
    }

    $mapperContent += "    $name toEntity($name`Request request);`n"
    $mapperContent += "    $name`Response toResponse($name entity);`n"
    $mapperContent += "    void updateEntityFromRequest($name`Request request, @MappingTarget $name entity);`n"
    $mapperContent += "}`n"
    Set-Content -Path $mapperPath -Value $mapperContent

    # Repository
    $repoPath = "$basePath\repository\$name`Repository.java"
    $repoContent = "package com.ocp.at.repository;`n`nimport com.ocp.at.entity.$name;`nimport org.springframework.data.jpa.repository.JpaRepository;`nimport org.springframework.data.jpa.repository.JpaSpecificationExecutor;`nimport org.springframework.stereotype.Repository;`n`n@Repository`npublic interface $name`Repository extends JpaRepository<$name, String>, JpaSpecificationExecutor<$name> {`n"
    if ($name -eq "Service") {
        $repoContent += "    boolean existsByZoneId(String zoneId);`n"
    } elseif ($name -eq "Installation") {
        $repoContent += "    boolean existsByServiceId(String serviceId);`n"
    } elseif ($name -eq "Equipement") {
        $repoContent += "    boolean existsByInstallationId(String installationId);`n"
    }
    $repoContent += "}`n"
    Set-Content -Path $repoPath -Value $repoContent

    # Service Interface
    $servicePath = "$basePath\service\$name`Service.java"
    $serviceContent = "package com.ocp.at.service;`n`nimport com.ocp.at.dto.request.$name`Request;`nimport com.ocp.at.dto.response.$name`Response;`nimport org.springframework.data.domain.Page;`nimport org.springframework.data.domain.Pageable;`nimport java.util.List;`n`npublic interface $name`Service {`n    $name`Response create($name`Request request);`n    $name`Response update(String id, $name`Request request);`n    $name`Response getById(String id);`n    List<$name`Response> getAll();`n    Page<$name`Response> search(String query, Pageable pageable);`n    void delete(String id);`n}`n"
    Set-Content -Path $servicePath -Value $serviceContent

    # Controller
    $controllerPath = "$basePath\controller\$name`Controller.java"
    $kebabName = $name.ToLower()
    if ($name -eq "EntrepriseExterne") { $kebabName = "entreprises-externes" }
    elseif ($name -eq "MesurePreparation") { $kebabName = "mesures-preparation" }
    elseif ($name -eq "MoyenAcces") { $kebabName = "moyens-acces" }
    else { $kebabName = $kebabName + "s" }

    $controllerContent = "package com.ocp.at.controller;`n`nimport com.ocp.at.dto.request.$name`Request;`nimport com.ocp.at.dto.response.$name`Response;`nimport com.ocp.at.service.$name`Service;`nimport io.swagger.v3.oas.annotations.Operation;`nimport io.swagger.v3.oas.annotations.tags.Tag;`nimport jakarta.validation.Valid;`nimport lombok.RequiredArgsConstructor;`nimport org.springframework.data.domain.Page;`nimport org.springframework.data.domain.Pageable;`nimport org.springframework.http.HttpStatus;`nimport org.springframework.security.access.prepost.PreAuthorize;`nimport org.springframework.web.bind.annotation.*;`n`nimport java.util.List;`n`n@RestController`n@RequestMapping(""/api/$kebabName"")`n@RequiredArgsConstructor`n@Tag(name = ""$name"", description = ""API de gestion des $name"")`npublic class $name`Controller {`n`n    private final $name`Service service;`n`n    @GetMapping`n    @Operation(summary = ""Lister tous les $name"")`n    public List<$name`Response> getAll() {`n        return service.getAll();`n    }`n`n    @GetMapping(""/{id}"")`n    @Operation(summary = ""Obtenir un(e) $name par ID"")`n    public $name`Response getById(@PathVariable String id) {`n        return service.getById(id);`n    }`n`n    @GetMapping(""/search"")`n    @Operation(summary = ""Rechercher avec pagination"")`n    public Page<$name`Response> search(@RequestParam(required = false) String query, Pageable pageable) {`n        return service.search(query, pageable);`n    }`n`n    @PostMapping`n    @ResponseStatus(HttpStatus.CREATED)`n    @PreAuthorize(""hasAuthority('MANAGE_REFERENTIELS')"")`n    @Operation(summary = ""Créer un(e) $name"")`n    public $name`Response create(@Valid @RequestBody $name`Request request) {`n        return service.create(request);`n    }`n`n    @PutMapping(""/{id}"")`n    @PreAuthorize(""hasAuthority('MANAGE_REFERENTIELS')"")`n    @Operation(summary = ""Modifier un(e) $name"")`n    public $name`Response update(@PathVariable String id, @Valid @RequestBody $name`Request request) {`n        return service.update(id, request);`n    }`n`n    @DeleteMapping(""/{id}"")`n    @ResponseStatus(HttpStatus.NO_CONTENT)`n    @PreAuthorize(""hasAuthority('MANAGE_REFERENTIELS')"")`n    @Operation(summary = ""Supprimer un(e) $name"")`n    public void delete(@PathVariable String id) {`n        service.delete(id);`n    }`n}`n"
    Set-Content -Path $controllerPath -Value $controllerContent
}
