package com.ocp.at.mapper;

import com.ocp.at.dto.request.ReceptionTravauxRequest;
import com.ocp.at.dto.response.EssaiResponse;
import com.ocp.at.dto.response.ReceptionTravauxResponse;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Essai;
import com.ocp.at.entity.ReceptionTravaux;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:13:27+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ReceptionTravauxMapperImpl implements ReceptionTravauxMapper {

    @Autowired
    private EssaiMapper essaiMapper;
    @Autowired
    private RemiseEtatMapper remiseEtatMapper;

    @Override
    public ReceptionTravaux toEntity(ReceptionTravauxRequest request) {
        if ( request == null ) {
            return null;
        }

        ReceptionTravaux.ReceptionTravauxBuilder receptionTravaux = ReceptionTravaux.builder();

        receptionTravaux.dateReception( request.getDateReception() );
        receptionTravaux.commentaire( request.getCommentaire() );
        receptionTravaux.travauxConformes( request.getTravauxConformes() );
        receptionTravaux.installationRemiseEnEtat( request.getInstallationRemiseEnEtat() );
        receptionTravaux.essaisEffectues( request.getEssaisEffectues() );

        return receptionTravaux.build();
    }

    @Override
    public ReceptionTravauxResponse toResponse(ReceptionTravaux reception) {
        if ( reception == null ) {
            return null;
        }

        ReceptionTravauxResponse receptionTravauxResponse = new ReceptionTravauxResponse();

        receptionTravauxResponse.setAutorisationTravailId( receptionAutorisationTravailId( reception ) );
        receptionTravauxResponse.setAutorisationTravailNumero( receptionAutorisationTravailNumero( reception ) );
        receptionTravauxResponse.setId( reception.getId() );
        receptionTravauxResponse.setDateReception( reception.getDateReception() );
        receptionTravauxResponse.setCommentaire( reception.getCommentaire() );
        receptionTravauxResponse.setTravauxConformes( reception.getTravauxConformes() );
        receptionTravauxResponse.setInstallationRemiseEnEtat( reception.getInstallationRemiseEnEtat() );
        receptionTravauxResponse.setEssaisEffectues( reception.getEssaisEffectues() );
        receptionTravauxResponse.setEssaisConformes( reception.getEssaisConformes() );
        receptionTravauxResponse.setValidee( reception.getValidee() );
        receptionTravauxResponse.setDateValidation( reception.getDateValidation() );
        receptionTravauxResponse.setCreatedBy( reception.getCreatedBy() );
        receptionTravauxResponse.setCreatedAt( reception.getCreatedAt() );
        receptionTravauxResponse.setUpdatedAt( reception.getUpdatedAt() );
        receptionTravauxResponse.setEssais( essaiListToEssaiResponseList( reception.getEssais() ) );
        receptionTravauxResponse.setRemiseEtat( remiseEtatMapper.toResponse( reception.getRemiseEtat() ) );

        return receptionTravauxResponse;
    }

    @Override
    public void updateFromRequest(ReceptionTravauxRequest request, ReceptionTravaux reception) {
        if ( request == null ) {
            return;
        }

        if ( request.getDateReception() != null ) {
            reception.setDateReception( request.getDateReception() );
        }
        if ( request.getCommentaire() != null ) {
            reception.setCommentaire( request.getCommentaire() );
        }
        if ( request.getTravauxConformes() != null ) {
            reception.setTravauxConformes( request.getTravauxConformes() );
        }
        if ( request.getInstallationRemiseEnEtat() != null ) {
            reception.setInstallationRemiseEnEtat( request.getInstallationRemiseEnEtat() );
        }
        if ( request.getEssaisEffectues() != null ) {
            reception.setEssaisEffectues( request.getEssaisEffectues() );
        }
    }

    private String receptionAutorisationTravailId(ReceptionTravaux receptionTravaux) {
        if ( receptionTravaux == null ) {
            return null;
        }
        AutorisationTravail autorisationTravail = receptionTravaux.getAutorisationTravail();
        if ( autorisationTravail == null ) {
            return null;
        }
        String id = autorisationTravail.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String receptionAutorisationTravailNumero(ReceptionTravaux receptionTravaux) {
        if ( receptionTravaux == null ) {
            return null;
        }
        AutorisationTravail autorisationTravail = receptionTravaux.getAutorisationTravail();
        if ( autorisationTravail == null ) {
            return null;
        }
        String numero = autorisationTravail.getNumero();
        if ( numero == null ) {
            return null;
        }
        return numero;
    }

    protected List<EssaiResponse> essaiListToEssaiResponseList(List<Essai> list) {
        if ( list == null ) {
            return null;
        }

        List<EssaiResponse> list1 = new ArrayList<EssaiResponse>( list.size() );
        for ( Essai essai : list ) {
            list1.add( essaiMapper.toResponse( essai ) );
        }

        return list1;
    }
}
