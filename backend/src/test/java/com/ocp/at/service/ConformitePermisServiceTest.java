package com.ocp.at.service;

import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.Permis;
import com.ocp.at.entity.enums.StatutPermis;
import com.ocp.at.entity.enums.TypePermis;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ConformitePermisServiceTest {

    private ConformitePermisService service;

    @BeforeEach
    void setUp() {
        service = new ConformitePermisService(new ObjectMapper());
    }

    private Permis buildPermis(TypePermis type, String numero) {
        Permis p = new Permis();
        p.setType(type);
        p.setNumero(numero);
        return p;
    }

    private AnalyseIA buildAnalyse(String json) {
        AnalyseIA a = new AnalyseIA();
        a.setJsonExtraction(json);
        return a;
    }

    @Test
    void evaluerConformite_ShouldReturn_CONFORME_WhenDataMatches() {
        Permis permis = buildPermis(TypePermis.FEU, "PRM-001");
        String json = "{\"numero\":\"PRM-001\",\"type\":\"FEU\",\"dateExpiration\":\"2099-12-31\"}";
        AnalyseIA analyse = buildAnalyse(json);

        StatutPermis result = service.evaluerConformite(permis, analyse);

        assertEquals(StatutPermis.CONFORME, result);
    }

    @Test
    void evaluerConformite_ShouldReturn_NON_CONFORME_WhenTypeMismatch() {
        Permis permis = buildPermis(TypePermis.FEU, "PRM-001");
        String json = "{\"numero\":\"PRM-001\",\"type\":\"FOUILLE\",\"dateExpiration\":\"2099-12-31\"}";
        AnalyseIA analyse = buildAnalyse(json);

        StatutPermis result = service.evaluerConformite(permis, analyse);

        assertEquals(StatutPermis.NON_CONFORME, result);
    }

    @Test
    void evaluerConformite_ShouldReturn_EXPIRE_WhenDateIsInPast() {
        Permis permis = buildPermis(TypePermis.CONSIGNATION, "PRM-002");
        String json = "{\"numero\":\"PRM-002\",\"type\":\"CONSIGNATION\",\"dateExpiration\":\"2020-01-01\"}";
        AnalyseIA analyse = buildAnalyse(json);

        StatutPermis result = service.evaluerConformite(permis, analyse);

        assertEquals(StatutPermis.EXPIRE, result);
    }

    @Test
    void evaluerConformite_ShouldReturn_NON_CONFORME_WhenNumeroMismatch() {
        Permis permis = buildPermis(TypePermis.FOUILLE, "PRM-999");
        String json = "{\"numero\":\"PRM-001\",\"type\":\"FOUILLE\",\"dateExpiration\":\"2099-12-31\"}";
        AnalyseIA analyse = buildAnalyse(json);

        StatutPermis result = service.evaluerConformite(permis, analyse);

        assertEquals(StatutPermis.NON_CONFORME, result);
    }

    @Test
    void evaluerConformite_ShouldReturn_A_VERIFIER_WhenNoAnalyse() {
        Permis permis = buildPermis(TypePermis.TRAVAIL_HAUTEUR, null);

        StatutPermis result = service.evaluerConformite(permis, null);

        assertEquals(StatutPermis.A_VERIFIER, result);
    }

    @Test
    void evaluerConformite_ShouldReturn_INVALIDE_WhenBadJson() {
        Permis permis = buildPermis(TypePermis.ESPACE_CONFINE, "PRM-123");
        AnalyseIA analyse = buildAnalyse("{invalid json");

        StatutPermis result = service.evaluerConformite(permis, analyse);

        assertEquals(StatutPermis.INVALIDE, result);
    }
}
