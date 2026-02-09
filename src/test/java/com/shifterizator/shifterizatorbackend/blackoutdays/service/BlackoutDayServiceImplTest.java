package com.shifterizator.shifterizatorbackend.blackoutdays.service;

import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayRequestDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.exception.BlackoutDayNotFoundException;
import com.shifterizator.shifterizatorbackend.blackoutdays.exception.BlackoutDayValidationException;
import com.shifterizator.shifterizatorbackend.blackoutdays.mapper.BlackoutDayMapper;
import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import com.shifterizator.shifterizatorbackend.blackoutdays.repository.BlackoutDayRepository;
import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
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
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlackoutDayServiceImplTest {

    @Mock
    private BlackoutDayRepository blackoutDayRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private BlackoutDayMapper blackoutDayMapper;

    @InjectMocks
    private BlackoutDayServiceImpl service;

    private static LocalDate futureDate() {
        return LocalDate.now().plusDays(10);
    }

    @Test
    void create_shouldCreateForSingleLocation() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                1L,
                futureDate(),
                "Holiday closure",
                false
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDay entity = BlackoutDay.builder()
                .location(location)
                .date(dto.date())
                .reason(dto.reason())
                .appliesToCompany(false)
                .build();

        BlackoutDay saved = BlackoutDay.builder()
                .id(99L)
                .location(location)
                .date(dto.date())
                .reason(dto.reason())
                .appliesToCompany(false)
                .build();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(blackoutDayRepository.existsSpecialOpeningHoursForLocationAndDate(1L, dto.date())).thenReturn(false);
        when(blackoutDayMapper.toEntity(dto, location)).thenReturn(entity);
        when(blackoutDayRepository.save(any(BlackoutDay.class))).thenReturn(saved);

        BlackoutDay result = service.create(dto);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getLocation().getId()).isEqualTo(1L);
        verify(blackoutDayRepository).save(any(BlackoutDay.class));
    }

    @Test
    void create_shouldCreateForAllCompanyLocations() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                1L,
                futureDate(),
                "Company holiday",
                true
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location1 = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        Location location2 = Location.builder().id(2L).name("Branch").address("Side").company(company).build();

        BlackoutDay saved1 = BlackoutDay.builder().id(99L).location(location1).date(dto.date()).reason(dto.reason()).appliesToCompany(false).build();
        BlackoutDay saved2 = BlackoutDay.builder().id(100L).location(location2).date(dto.date()).reason(dto.reason()).appliesToCompany(false).build();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location1));
        when(locationRepository.findByCompany_Id(1L)).thenReturn(List.of(location1, location2));
        when(blackoutDayRepository.existsSpecialOpeningHoursForLocationAndDate(1L, dto.date())).thenReturn(false);
        when(blackoutDayRepository.existsSpecialOpeningHoursForLocationAndDate(2L, dto.date())).thenReturn(false);
        when(blackoutDayMapper.toEntity(any(BlackoutDayRequestDto.class), eq(location1))).thenReturn(
                BlackoutDay.builder().location(location1).date(dto.date()).reason(dto.reason()).appliesToCompany(false).build()
        );
        when(blackoutDayMapper.toEntity(any(BlackoutDayRequestDto.class), eq(location2))).thenReturn(
                BlackoutDay.builder().location(location2).date(dto.date()).reason(dto.reason()).appliesToCompany(false).build()
        );
        when(blackoutDayRepository.save(any(BlackoutDay.class))).thenReturn(saved1, saved2);

        BlackoutDay result = service.create(dto);

        assertThat(result.getId()).isEqualTo(99L);
        verify(blackoutDayRepository, times(2)).save(any(BlackoutDay.class));
    }

    @Test
    void create_shouldThrowWhenLocationNotFound() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                999L,
                futureDate(),
                "Holiday closure",
                false
        );

        when(locationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessageContaining("Location not found");
    }

    @Test
    void create_shouldThrowWhenOverlapsWithSpecialOpeningHours() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                1L,
                futureDate(),
                "Holiday closure",
                false
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(blackoutDayRepository.existsSpecialOpeningHoursForLocationAndDate(1L, dto.date())).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BlackoutDayValidationException.class)
                .hasMessageContaining("special opening hours record already exists");
    }

    @Test
    void create_shouldThrowWhenNoLocationsFoundForCompany() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                1L,
                futureDate(),
                "Company holiday",
                true
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.findByCompany_Id(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BlackoutDayValidationException.class)
                .hasMessageContaining("No locations found");
    }

    @Test
    void update_shouldUpdateExistingBlackoutDay() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                1L,
                futureDate(),
                "Updated reason",
                false
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDay existing = BlackoutDay.builder()
                .id(99L)
                .location(location)
                .date(LocalDate.now().plusDays(5))
                .reason("Old reason")
                .appliesToCompany(false)
                .build();

        when(blackoutDayRepository.findById(99L)).thenReturn(Optional.of(existing));
        when(blackoutDayRepository.existsSpecialOpeningHoursForLocationAndDate(1L, dto.date())).thenReturn(false);

        BlackoutDay result = service.update(99L, dto);

        assertThat(result.getReason()).isEqualTo("Updated reason");
        assertThat(result.getDate()).isEqualTo(dto.date());
        verify(blackoutDayRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                1L,
                futureDate(),
                "Updated reason",
                false
        );

        when(blackoutDayRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(BlackoutDayNotFoundException.class)
                .hasMessageContaining("Blackout day not found");
    }

    @Test
    void update_shouldThrowWhenDeleted() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                1L,
                futureDate(),
                "Updated reason",
                false
        );

        BlackoutDay deleted = BlackoutDay.builder()
                .id(99L)
                .deletedAt(LocalDateTime.now())
                .build();

        when(blackoutDayRepository.findById(99L)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(BlackoutDayNotFoundException.class)
                .hasMessageContaining("Blackout day not found");
    }

    @Test
    void update_shouldThrowWhenOverlapsWithSpecialOpeningHours() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                1L,
                futureDate(),
                "Updated reason",
                false
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDay existing = BlackoutDay.builder()
                .id(99L)
                .location(location)
                .date(LocalDate.now().plusDays(5))
                .reason("Old reason")
                .appliesToCompany(false)
                .build();

        when(blackoutDayRepository.findById(99L)).thenReturn(Optional.of(existing));
        when(blackoutDayRepository.existsSpecialOpeningHoursForLocationAndDate(1L, dto.date())).thenReturn(true);

        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(BlackoutDayValidationException.class)
                .hasMessageContaining("special opening hours record already exists");
    }

    @Test
    void update_shouldNotCheckOverlapWhenDateAndLocationUnchanged() {
        LocalDate sameDate = futureDate();
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                1L,
                sameDate,
                "Updated reason",
                false
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDay existing = BlackoutDay.builder()
                .id(99L)
                .location(location)
                .date(sameDate)
                .reason("Old reason")
                .appliesToCompany(false)
                .build();

        when(blackoutDayRepository.findById(99L)).thenReturn(Optional.of(existing));

        BlackoutDay result = service.update(99L, dto);

        assertThat(result.getReason()).isEqualTo("Updated reason");
        verify(blackoutDayRepository, never()).existsSpecialOpeningHoursForLocationAndDate(any(), any());
    }

    @Test
    void delete_shouldPerformSoftDelete() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDay blackoutDay = BlackoutDay.builder()
                .id(99L)
                .location(location)
                .date(futureDate())
                .reason("Test")
                .build();

        when(blackoutDayRepository.findById(99L)).thenReturn(Optional.of(blackoutDay));

        service.delete(99L, false);

        assertThat(blackoutDay.getDeletedAt()).isNotNull();
        verify(blackoutDayRepository, never()).delete(any(BlackoutDay.class));
    }

    @Test
    void delete_shouldPerformHardDelete() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDay blackoutDay = BlackoutDay.builder()
                .id(99L)
                .location(location)
                .date(futureDate())
                .reason("Test")
                .build();

        when(blackoutDayRepository.findById(99L)).thenReturn(Optional.of(blackoutDay));

        service.delete(99L, true);

        verify(blackoutDayRepository).delete(blackoutDay);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(blackoutDayRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L, false))
                .isInstanceOf(BlackoutDayNotFoundException.class)
                .hasMessageContaining("Blackout day not found");
    }

    @Test
    void findById_shouldReturnBlackoutDay() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDay blackoutDay = BlackoutDay.builder()
                .id(99L)
                .location(location)
                .date(futureDate())
                .reason("Test")
                .build();

        when(blackoutDayRepository.findById(99L)).thenReturn(Optional.of(blackoutDay));

        BlackoutDay result = service.findById(99L);

        assertThat(result.getId()).isEqualTo(99L);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(blackoutDayRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BlackoutDayNotFoundException.class)
                .hasMessageContaining("Blackout day not found");
    }

    @Test
    void findById_shouldThrowWhenDeleted() {
        BlackoutDay deleted = BlackoutDay.builder()
                .id(99L)
                .deletedAt(LocalDateTime.now())
                .build();

        when(blackoutDayRepository.findById(99L)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BlackoutDayNotFoundException.class)
                .hasMessageContaining("Blackout day not found");
    }

    @Test
    void search_shouldReturnPaginatedResults() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDay blackoutDay = BlackoutDay.builder()
                .id(99L)
                .location(location)
                .date(futureDate())
                .reason("Test")
                .build();

        PageRequest pageable = PageRequest.of(0, 10);
        when(blackoutDayRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(blackoutDay)));

        var result = service.search(1L, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(99L);
    }

    @Test
    void findByLocation_shouldReturnList() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDay blackoutDay = BlackoutDay.builder()
                .id(99L)
                .location(location)
                .date(futureDate())
                .reason("Test")
                .build();

        when(blackoutDayRepository.findByLocation_IdAndDeletedAtIsNullOrderByDateAsc(1L))
                .thenReturn(List.of(blackoutDay));

        List<BlackoutDay> result = service.findByLocation(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(99L);
    }

    @Test
    void findByLocationAndMonth_shouldReturnList() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDay blackoutDay = BlackoutDay.builder()
                .id(99L)
                .location(location)
                .date(LocalDate.of(2024, 12, 15))
                .reason("Test")
                .build();

        YearMonth month = YearMonth.of(2024, 12);
        when(blackoutDayRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(blackoutDay));

        List<BlackoutDay> result = service.findByLocationAndMonth(1L, month);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(99L);
    }
}
