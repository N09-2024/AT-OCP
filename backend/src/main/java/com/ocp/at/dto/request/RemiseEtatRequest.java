package com.ocp.at.dto.request;

import lombok.Data;

@Data
public class RemiseEtatRequest {

    private Boolean zoneNettoyee = false;
    private Boolean materielRetire = false;
    private Boolean protectionsRetirees = false;
    private Boolean consignationRetiree = false;
    private String commentaire;
}
