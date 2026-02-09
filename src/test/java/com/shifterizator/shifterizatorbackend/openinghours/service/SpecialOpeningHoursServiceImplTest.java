package com.shifterizator.shifterizatorbackend.openinghours.service;

import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursRequestDto;
import com.shifterizator.shifterizatorbackend.openinghours.exception.SpecialOpeningHoursNotFoundException;
import com.shifterizator.shifterizatorbackend.openinghours.exception.SpecialOpeningHoursValidationException;
import com.shifterizator.shifterizatorbackend.openinghours.mapper.SpecialOpeningHoursMapper;
import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import com.shifterizator.shifterizatorbackend.openinghours.repository.SpecialOpeningHoursRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialOpeningHoursServiceImplTest {

    @Mock
    private SpecialOpeningHoursRepository openingHoursRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SpecialOpeningHoursMapper openingHoursMapper;

    @InjectMocks
    private SpecialOpeningHoursServiceImpl service;

    private static LocalDate futureDate() {
        return LocalDate.now().plusDays(10);
    }

    @Test
    void create_shouldCreateForSingleLocation() {
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                1L,
                futureDate(),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Holiday schedule",
                "#FF0000",
                false
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        SpecialOpeningHours entity = SpecialOpeningHours.builder()
                .location(location)
                .date(dto.date())
                .openTime(dto.openTime())
                .closeTime(dto.closeTime())
                .reason(dto.reason())
                .colorCode(dto.colorCode())
                .appliesToCompany(false)
                .build();

        SpecialOpeningHours saved = SpecialOpeningHours.builder()
                .id(99L)
                .location(location)
                .date(dto.date())
                .openTime(dto.openTime())
                .closeTime(dto.closeTime())
                .reason(dto.reason())
                .colorCode(dto.colorCode())
                .appliesToCompany(false)
                .build();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(openingHoursMapper.toEntity(dto, location)).thenReturn(entity);
        when(openingHoursRepository.save(any(SpecialOpeningHours.class))).thenReturn(saved);

        SpecialOpeningHours result = service.create(dto);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getLocation().getId()).isEqualTo(1L);
        verify(openingHoursRepository).save(any(SpecialOpeningHours.class));
    }

    @Test
    void create_shouldCreateForAllCompanyLocationsWhenAppliesToCompanyTrue() {
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                1L,
                futureDate(),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Company-wide holiday",
                "#00FF00",
                true
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location loc1 = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        Location loc2 = Location.builder().id(2L).name("Branch").address("Side").company(company).build();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(loc1));
        when(locationRepository.findByCompany_Id(1L)).thenReturn(List.of(loc1, loc2));
        when(openingHoursMapper.toEntity(any(SpecialOpeningHoursRequestDto.class), any(Location.class)))
                .thenAnswer(invocation -> {
                    SpecialOpeningHoursRequestDto d = invocation.getArgument(0);
                    Location l = invocation.getArgument(1);
                    return SpecialOpeningHours.builder()
                            .location(l)
                            .date(d.date())
                            .openTime(d.openTime())
                            .closeTime(d.closeTime())
                            .reason(d.reason())
                            .colorCode(d.colorCode())
                            .appliesToCompany(false)
                            .build();
                });
        when(openingHoursRepository.save(any(SpecialOpeningHours.class)))
                .thenAnswer(invocation -> {
                    SpecialOpeningHours oh = invocation.getArgument(0);
                    if (oh.getId() == null) {
                        oh.setId(100L);
                    }
                    return oh;
                });

        SpecialOpeningHours result = service.create(dto);

        assertThat(result.getLocation().getId()).isIn(1L, 2L);
        verify(openingHoursRepository, times(2)).save(any(SpecialOpeningHours.class));
    }

    @Test
    void create_shouldThrowWhenLocationNotFound() {
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                999L,
                futureDate(),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Holiday",
                null,
                false
        );

        when(locationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("Location not found");
    }

    @Test
    void create_shouldThrowWhenCloseTimeNotAfterOpenTime() {
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                1L,
                futureDate(),
                LocalTime.of(10, 0),
                LocalTime.of(10, 0),
                "Invalid",
                null,
                false
        );

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(SpecialOpeningHoursValidationException.class)
                .hasMessage("Close time must be after open time");
        verify(locationRepository, never()).findById(any());
    }

    @Test
    void update_shouldUpdateSuccessfully() {
        Long id = 99L;
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                1L,
                futureDate(),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                "Updated",
                "#0000FF",
                false
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        SpecialOpeningHours existing = SpecialOpeningHours.builder()
                .id(id)
                .location(location)
                .date(futureDate())
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(17, 0))
                .reason("Old")
                .build();

        when(openingHoursRepository.findById(id)).thenReturn(Optional.of(existing));

        SpecialOpeningHours result = service.update(id, dto);

        assertThat(result.getCloseTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(result.getReason()).isEqualTo("Updated");
        verify(locationRepository, never()).findById(any());
    }

    @Test
    void update_shouldSwitchLocationWhenDifferent() {
        Long id = 99L;
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                2L,
                futureDate(),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                "Updated",
                null,
                false
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location oldLoc = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        Location newLoc = Location.builder().id(2L).name("Branch").address("Side").company(company).build();

        SpecialOpeningHours existing = SpecialOpeningHours.builder()
                .id(id)
                .location(oldLoc)
                .date(futureDate())
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(17, 0))
                .reason("Old")
                .build();

        when(openingHoursRepository.findById(id)).thenReturn(Optional.of(existing));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(newLoc));

        SpecialOpeningHours result = service.update(id, dto);

        assertThat(result.getLocation().getId()).isEqualTo(2L);
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        when(openingHoursRepository.findById(999L)).thenReturn(Optional.empty());

        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                1L,
                futureDate(),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Reason",
                null,
                false
        );

        assertThatThrownBy(() -> service.update(999L, dto))
                .isInstanceOf(SpecialOpeningHoursNotFoundException.class)
                .hasMessage("Special opening hours not found");
    }

    @Test
    void delete_shouldSoftDeleteWhenHardDeleteFalse() {
        SpecialOpeningHours oh = SpecialOpeningHours.builder().id(99L).build();
        when(openingHoursRepository.findById(99L)).thenReturn(Optional.of(oh));

        service.delete(99L, false);

        assertThat(oh.getDeletedAt()).isNotNull();
        verify(openingHoursRepository, never()).delete(any(SpecialOpeningHours.class));
    }

    @Test
    void delete_shouldHardDeleteWhenHardDeleteTrue() {
        SpecialOpeningHours oh = SpecialOpeningHours.builder().id(99L).build();
        when(openingHoursRepository.findById(99L)).thenReturn(Optional.of(oh));

        service.delete(99L, true);

        verify(openingHoursRepository).delete(oh);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(openingHoursRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(999L, false))
                .isInstanceOf(SpecialOpeningHoursNotFoundException.class)
                .hasMessage("Special opening hours not found");
    }

    @Test
    void findById_shouldReturnWhenNotDeleted() {
        SpecialOpeningHours oh = SpecialOpeningHours.builder().id(99L).build();
        when(openingHoursRepository.findById(99L)).thenReturn(Optional.of(oh));

        SpecialOpeningHours result = service.findById(99L);

        assertThat(result.getId()).isEqualTo(99L);
    }

    @Test
    void findById_shouldThrowWhenDeleted() {
        SpecialOpeningHours oh = SpecialOpeningHours.builder().id(99L).deletedAt(LocalDateTime.now()).build();
        when(openingHoursRepository.findById(99L)).thenReturn(Optional.of(oh));

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(SpecialOpeningHoursNotFoundException.class)
                .hasMessage("Special opening hours not found");
    }

    @Test
    void search_shouldDelegateToRepository() {
        when(openingHoursRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 10))))
                .thenReturn(new PageImpl<>(List.of()));

        var result = service.search(null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        verify(openingHoursRepository).findAll(any(Specification.class), eq(PageRequest.of(0, 10)));
    }

    @Test
    void findByLocation_shouldDelegateToRepository() {
        when(openingHoursRepository.findByLocation_IdAndDeletedAtIsNullOrderByDateAsc(1L))
                .thenReturn(List.of());

        var result = service.findByLocation(1L);

        assertThat(result).isEmpty();
        verify(openingHoursRepository).findByLocation_IdAndDeletedAtIsNullOrderByDateAsc(1L);
    }

    @Test
    void findByLocationAndMonth_shouldUseSpecs() {
        when(openingHoursRepository.findAll(any(Specification.class))).thenReturn(List.of());

        var result = service.findByLocationAndMonth(1L, YearMonth.now());

        assertThat(result).isEmpty();
        verify(openingHoursRepository).findAll(any(Specification.class));
    }
}

