package com.ocp.at.dto.response;

import lombok.Data;

@Data
public class RemiseEtatResponse {
    private String id;
    private Boolean zoneNettoyee;
    private Boolean materielRetire;
    private Boolean protectionsRetirees;
    private Boolean consignationRetiree;
    private String commentaire;
}
