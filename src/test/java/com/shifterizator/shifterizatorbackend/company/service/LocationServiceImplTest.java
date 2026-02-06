package com.shifterizator.shifterizatorbackend.company.service;

import com.shifterizator.shifterizatorbackend.company.dto.LocationRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.mapper.LocationMapper;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationServiceImpl locationService;


    @Test
    void create_shouldCreateLocationSuccessfully() {
        LocationRequestDto dto = new LocationRequestDto(
                "HQ", "Main Street 1", 1L
        );

        Company company = new Company();
        company.setId(1L);
        company.setName("Skynet");

        Location location = Location.builder().id(10L).name("HQ").address("Main Street 1").company(company).build();
        LocationResponseDto responseDto = new LocationResponseDto(
                10L, "HQ", "Main Street 1", 1L
        );

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(locationRepository.save(any(Location.class))).thenReturn(location);
        when(locationMapper.toDto(any(Location.class))).thenReturn(responseDto);

        LocationResponseDto result = locationService.create(dto);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("HQ");
    }

    @Test
    void create_shouldThrowWhenCompanyNotFound() {
        LocationRequestDto dto = new LocationRequestDto(
                "HQ", "Main Street 1", 1L
        );

        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.create(dto))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessage("Company not found");
    }

    @Test
    void update_shouldUpdateLocationSuccessfully() {
        LocationRequestDto dto = new LocationRequestDto(
                "New HQ", "New Address", 1L
        );

        Company company = new Company();
        company.setId(1L);
        company.setName("Skynet");

        Location location = Location.builder().id(10L).name("HQ").address("Old").company(company).build();
        LocationResponseDto responseDto = new LocationResponseDto(
                10L, "New HQ", "New Address", 1L
        );

        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(responseDto);

        LocationResponseDto result = locationService.update(10L, dto);

        assertThat(result.name()).isEqualTo("New HQ");
        assertThat(location.getName()).isEqualTo("New HQ");
        assertThat(location.getAddress()).isEqualTo("New Address");
    }

    @Test
    void update_shouldThrowWhenLocationNotFound() {
        LocationRequestDto dto = new LocationRequestDto(
                "New HQ", "New Address", 1L
        );

        when(locationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.update(10L, dto))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("Location not found");
    }

    @Test
    void delete_shouldDeleteLocation() {
        Location location = Location.builder().id(10L).build();

        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));

        locationService.delete(10L);

        verify(locationRepository).delete(location);
    }

    @Test
    void delete_shouldThrowWhenLocationNotFound() {
        when(locationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.delete(10L))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("Location not found");
    }

    @Test
    void findById_shouldReturnLocation() {
        Company company = new Company();
        company.setId(1L);

        Location location = Location.builder().id(10L).name("HQ").address("Main").company(company).build();
        LocationResponseDto responseDto = new LocationResponseDto(
                10L, "HQ", "Main", 1L
        );

        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(responseDto);

        LocationResponseDto result = locationService.findById(10L);

        assertThat(result.id()).isEqualTo(10L);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(locationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.findById(10L))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("Location not found");
    }

    @Test
    void findByCompany_shouldFilterByCompanyId() {
        Company company1 = new Company();
        company1.setId(1L);
        company1.setName("Skynet");

        Company company2 = new Company();
        company2.setId(2L);
        company2.setName("Cyberdyne");

        Location loc1 = Location.builder().id(10L).name("HQ").company(company1).build();
        Location loc2 = Location.builder().id(11L).name("Branch").company(company2).build();

        LocationResponseDto dto1 = new LocationResponseDto(10L, "HQ", null, 1L);
        LocationResponseDto dto2 = new LocationResponseDto(11L, "Branch", null, 2L);

        when(locationRepository.findAll()).thenReturn(List.of(loc1, loc2));
        when(locationMapper.toDto(loc1)).thenReturn(dto1);
        lenient().when(locationMapper.toDto(loc2)).thenReturn(dto2);

        List<LocationResponseDto> result = locationService.findByCompany(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
    }
}
