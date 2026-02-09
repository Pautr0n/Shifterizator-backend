package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftInstanceNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftTemplateNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftInstanceMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import com.shifterizator.shifterizatorbackend.shift.service.domain.ShiftInstanceDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftInstanceServiceImplTest {

    @Mock
    private ShiftInstanceRepository shiftInstanceRepository;

    @Mock
    private ShiftInstanceMapper shiftInstanceMapper;

    @Mock
    private ShiftInstanceDomainService shiftInstanceDomainService;

    @InjectMocks
    private ShiftInstanceServiceImpl service;

    private static LocalDate futureDate() {
        return LocalDate.now().plusDays(10);
    }

    @Test
    void create_shouldCreateInstance() {
        ShiftInstanceRequestDto dto = new ShiftInstanceRequestDto(
                1L,
                1L,
                futureDate(),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                3,
                null,
                "Notes"
        );

        Company company = new Company("Skynet",
                "Skynet",
                "12345678T",
                "test@test.com",
                "+34999999999");
        company.setId(1L);

        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();

        ShiftInstance instance = ShiftInstance.builder()
                .shiftTemplate(template)
                .location(location)
                .date(dto.date())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .requiredEmployees(dto.requiredEmployees())
                .notes(dto.notes())
                .isComplete(false)
                .build();

        ShiftInstance saved = ShiftInstance.builder()
                .id(99L)
                .shiftTemplate(template)
                .location(location)
                .date(dto.date())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .requiredEmployees(dto.requiredEmployees())
                .notes(dto.notes())
                .isComplete(false)
                .build();

        when(shiftInstanceDomainService.resolveTemplate(1L)).thenReturn(template);
        when(shiftInstanceDomainService.resolveLocation(1L)).thenReturn(location);
        when(shiftInstanceMapper.toEntity(dto, template, location)).thenReturn(instance);
        when(shiftInstanceRepository.save(any(ShiftInstance.class))).thenReturn(saved);

        ShiftInstance result = service.create(dto);

        assertThat(result.getId()).isEqualTo(99L);
        verify(shiftInstanceRepository).save(any(ShiftInstance.class));
        verify(shiftInstanceDomainService).validateTimes(dto.startTime(), dto.endTime());
        verify(shiftInstanceDomainService).validateIdealEmployees(dto.requiredEmployees(), dto.idealEmployees());
    }

    @Test
    void create_shouldThrowWhenIdealEmployeesLessThanRequired() {
        ShiftInstanceRequestDto dto = new ShiftInstanceRequestDto(
                1L,
                1L,
                futureDate(),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                5,
                3,
                null
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();

        when(shiftInstanceDomainService.resolveTemplate(1L)).thenReturn(template);
        when(shiftInstanceDomainService.resolveLocation(1L)).thenReturn(location);
        doThrow(new ShiftValidationException("Ideal employees must be greater than or equal to required employees"))
                .when(shiftInstanceDomainService).validateIdealEmployees(5, 3);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ShiftValidationException.class)
                .hasMessageContaining("Ideal employees must be greater than or equal to required employees");
    }

    @Test
    void create_shouldThrowWhenTemplateNotFound() {
        ShiftInstanceRequestDto dto = new ShiftInstanceRequestDto(
                999L,
                1L,
                futureDate(),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                3,
                null,
                null
        );

        when(shiftInstanceDomainService.resolveTemplate(999L))
                .thenThrow(new ShiftTemplateNotFoundException("Shift template not found"));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ShiftTemplateNotFoundException.class)
                .hasMessageContaining("Shift template not found");
    }

    @Test
    void create_shouldThrowWhenEndTimeBeforeStartTime() {
        ShiftInstanceRequestDto dto = new ShiftInstanceRequestDto(
                1L,
                1L,
                futureDate(),
                LocalTime.of(17, 0),
                LocalTime.of(9, 0),
                3,
                null,
                null
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();

        when(shiftInstanceDomainService.resolveTemplate(1L)).thenReturn(template);
        when(shiftInstanceDomainService.resolveLocation(1L)).thenReturn(location);
        doThrow(new ShiftValidationException("End time must be after start time"))
                .when(shiftInstanceDomainService).validateTimes(any(), any());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ShiftValidationException.class)
                .hasMessageContaining("End time must be after start time");
    }

    @Test
    void update_shouldUpdateInstance() {
        ShiftInstanceRequestDto dto = new ShiftInstanceRequestDto(
                1L,
                1L,
                futureDate(),
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                5,
                null,
                "Updated notes"
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();

        ShiftInstance existing = ShiftInstance.builder()
                .id(99L)
                .shiftTemplate(template)
                .location(location)
                .date(LocalDate.now().plusDays(5))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .requiredEmployees(3)
                .notes("Old notes")
                .isComplete(false)
                .build();

        when(shiftInstanceRepository.findById(99L)).thenReturn(Optional.of(existing));

        ShiftInstance result = service.update(99L, dto);

        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(result.getRequiredEmployees()).isEqualTo(5);
        assertThat(result.getNotes()).isEqualTo("Updated notes");
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        ShiftInstanceRequestDto dto = new ShiftInstanceRequestDto(
                1L,
                1L,
                futureDate(),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                3,
                null,
                null
        );

        when(shiftInstanceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(ShiftInstanceNotFoundException.class)
                .hasMessageContaining("Shift instance not found");
    }

    @Test
    void delete_shouldPerformSoftDelete() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();

        ShiftInstance instance = ShiftInstance.builder()
                .id(99L)
                .shiftTemplate(template)
                .location(location)
                .date(futureDate())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        when(shiftInstanceRepository.findById(99L)).thenReturn(Optional.of(instance));

        service.delete(99L, false);

        assertThat(instance.getDeletedAt()).isNotNull();
        verify(shiftInstanceRepository, never()).delete(any(ShiftInstance.class));
    }

    @Test
    void findById_shouldReturnInstance() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();

        ShiftInstance instance = ShiftInstance.builder()
                .id(99L)
                .shiftTemplate(template)
                .location(location)
                .date(futureDate())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        when(shiftInstanceRepository.findById(99L)).thenReturn(Optional.of(instance));

        ShiftInstance result = service.findById(99L);

        assertThat(result.getId()).isEqualTo(99L);
    }

    @Test
    void findByLocationAndDate_shouldReturnList() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();

        ShiftInstance instance = ShiftInstance.builder()
                .id(99L)
                .shiftTemplate(template)
                .location(location)
                .date(futureDate())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        when(shiftInstanceRepository.findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(1L, futureDate()))
                .thenReturn(List.of(instance));

        List<ShiftInstance> result = service.findByLocationAndDate(1L, futureDate());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(99L);
    }
}
