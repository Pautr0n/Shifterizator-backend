package com.shifterizator.shifterizatorbackend.company.service;

import com.shifterizator.shifterizatorbackend.company.dto.LocationRequestDto;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
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

        Location savedLocation = Location.builder().id(10L).name("HQ").address("Main Street 1").company(company).build();

        when(companyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(company));
        when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);

        Location result = locationService.create(dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("HQ");
        assertThat(result.getAddress()).isEqualTo("Main Street 1");
    }

    @Test
    void create_shouldThrowWhenCompanyNotFound() {
        LocationRequestDto dto = new LocationRequestDto(
                "HQ", "Main Street 1", 1L
        );

        when(companyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

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

        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));

        Location result = locationService.update(10L, dto);

        assertThat(result.getName()).isEqualTo("New HQ");
        assertThat(result.getAddress()).isEqualTo("New Address");
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

        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));

        Location result = locationService.findById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("HQ");
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

        Location loc1 = Location.builder().id(10L).name("HQ").company(company1).build();

        when(locationRepository.findByCompany_Id(1L)).thenReturn(List.of(loc1));

        List<Location> result = locationService.findByCompany(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getName()).isEqualTo("HQ");
    }
}
