package com.ocp.at.service;

import com.ocp.at.dto.request.ZoneRequest;
import com.ocp.at.dto.response.ZoneResponse;
import com.ocp.at.entity.Zone;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.ZoneMapper;
import com.ocp.at.repository.ServiceRepository;
import com.ocp.at.repository.ZoneRepository;
import com.ocp.at.service.impl.ZoneServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ZoneServiceUnitTest {

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ZoneMapper zoneMapper;

    @InjectMocks
    private ZoneServiceImpl zoneService;

    private Zone zone;
    private ZoneRequest zoneRequest;
    private ZoneResponse zoneResponse;

    @BeforeEach
    void setUp() {
        zone = Zone.builder()
                .id("zone-1")
                .nomZone("Zone A")
                .codeZone("ZA")
                .descriptionZone("Description Zone A")
                .build();

        zoneRequest = ZoneRequest.builder()
                .nomZone("Zone A")
                .codeZone("ZA")
                .descriptionZone("Description Zone A")
                .build();

        zoneResponse = ZoneResponse.builder()
                .id("zone-1")
                .nomZone("Zone A")
                .codeZone("ZA")
                .descriptionZone("Description Zone A")
                .build();
    }

    @Test
    void shouldCreateZone() {
        when(zoneMapper.toEntity(zoneRequest)).thenReturn(zone);
        when(zoneRepository.save(zone)).thenReturn(zone);
        when(zoneMapper.toResponse(zone)).thenReturn(zoneResponse);

        ZoneResponse result = zoneService.create(zoneRequest);

        assertNotNull(result);
        assertEquals("Zone A", result.getNomZone());
        verify(zoneRepository, times(1)).save(zone);
    }

    @Test
    void shouldUpdateZone() {
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(zoneRepository.save(zone)).thenReturn(zone);
        when(zoneMapper.toResponse(zone)).thenReturn(zoneResponse);

        ZoneResponse result = zoneService.update("zone-1", zoneRequest);

        assertNotNull(result);
        verify(zoneMapper, times(1)).updateEntityFromRequest(zoneRequest, zone);
        verify(zoneRepository, times(1)).save(zone);
    }

    @Test
    void shouldThrowWhenUpdateNonExistingZone() {
        when(zoneRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> zoneService.update("invalid", zoneRequest));
    }

    @Test
    void shouldDeleteZone() {
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(serviceRepository.existsByZoneId("zone-1")).thenReturn(false);

        assertDoesNotThrow(() -> zoneService.delete("zone-1"));
        verify(zoneRepository, times(1)).delete(zone);
    }

    @Test
    void shouldThrowWhenDeleteZoneWithServices() {
        when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
        when(serviceRepository.existsByZoneId("zone-1")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> zoneService.delete("zone-1"));
        assertEquals("Impossible de supprimer une Zone qui contient des Services.", exception.getMessage());
        verify(zoneRepository, never()).delete(any(Zone.class));
    }

    @Test
    void shouldSearchZones() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Zone> page = new PageImpl<>(List.of(zone));
        when(zoneRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(page);
        when(zoneMapper.toResponse(zone)).thenReturn(zoneResponse);

        Page<ZoneResponse> result = zoneService.search("A", pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Zone A", result.getContent().get(0).getNomZone());
    }
}
