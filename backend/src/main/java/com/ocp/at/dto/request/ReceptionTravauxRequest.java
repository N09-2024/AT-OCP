package com.ocp.at.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReceptionTravauxRequest {

    @NotNull(message = "L'identifiant de l'AT est obligatoire")
    private String autorisationTravailId;

    private LocalDateTime dateReception;

    private String commentaire;

    private Boolean travauxConformes = false;

    private Boolean installationRemiseEnEtat = false;

    private Boolean essaisEffectues = false;

    @Valid
    private List<EssaiRequest> essais;

    @Valid
    private RemiseEtatRequest remiseEtat;
}
