package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.shift.dto.PositionRequirementDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftTemplateNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftTemplateMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftTemplateRepository;
import com.shifterizator.shifterizatorbackend.shift.service.domain.ShiftTemplateDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftTemplateServiceImplTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftTemplateMapper shiftTemplateMapper;

    @Mock
    private ShiftTemplateDomainService shiftTemplateDomainService;

    @InjectMocks
    private ShiftTemplateServiceImpl service;

    @Test
    void create_shouldCreateTemplateWithMultiplePositions() {
        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                1L,
                List.of(
                        new PositionRequirementDto(1L, 2),
                        new PositionRequirementDto(2L, 1)
                ),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Morning shift",
                Set.of(),
                true
        );

        Company company = new Company("Skynet",
                "Skynet",
                "12345678T",
                "test@test.com",
                "+34999999999");
        company.setId(1L);

        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        Position position1 = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Position position2 = Position.builder().id(2L).name("Manager").company(company).build();

        ShiftTemplate template = ShiftTemplate.builder()
                .location(location)
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .description(dto.description())
                .isActive(true)
                .build();

        ShiftTemplate saved = ShiftTemplate.builder()
                .id(99L)
                .location(location)
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .description(dto.description())
                .isActive(true)
                .build();

        when(shiftTemplateDomainService.resolveLocation(1L)).thenReturn(location);
        when(shiftTemplateDomainService.resolveLanguages(Set.of())).thenReturn(Set.of());
        when(shiftTemplateMapper.toEntity(dto, location, Set.of())).thenReturn(template);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(saved);

        ShiftTemplate result = service.create(dto);

        assertThat(result.getId()).isEqualTo(99L);
        verify(shiftTemplateDomainService).buildPositionRequirements(template, dto.requiredPositions());
        verify(shiftTemplateRepository).save(any(ShiftTemplate.class));
    }

    @Test
    void create_shouldThrowWhenLocationNotFound() {
        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                999L,
                List.of(new PositionRequirementDto(1L, 1)),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Test",
                Set.of(),
                true
        );

        when(shiftTemplateDomainService.resolveLocation(999L))
                .thenThrow(new LocationNotFoundException("Location not found"));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessageContaining("Location not found");
    }

    @Test
    void create_shouldThrowWhenPositionNotFound() {
        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                1L,
                List.of(new PositionRequirementDto(999L, 1)),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Test",
                Set.of(),
                true
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        when(shiftTemplateDomainService.resolveLocation(1L)).thenReturn(location);
        when(shiftTemplateDomainService.resolveLanguages(Set.of())).thenReturn(Set.of());
        when(shiftTemplateMapper.toEntity(any(), any(), any())).thenReturn(ShiftTemplate.builder().build());
        doThrow(new PositionNotFoundException("Position not found: 999"))
                .when(shiftTemplateDomainService).buildPositionRequirements(any(), any());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(PositionNotFoundException.class)
                .hasMessageContaining("Position not found");
    }

    @Test
    void create_shouldThrowWhenEndTimeBeforeStartTime() {
        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                1L,
                List.of(new PositionRequirementDto(1L, 1)),
                LocalTime.of(17, 0),
                LocalTime.of(9, 0),
                "Test",
                Set.of(),
                true
        );

        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        when(shiftTemplateDomainService.resolveLocation(1L)).thenReturn(location);
        doThrow(new ShiftValidationException("End time must be after start time"))
                .when(shiftTemplateDomainService).validateTimes(any(), any());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ShiftValidationException.class)
                .hasMessageContaining("End time must be after start time");
    }

    @Test
    void update_shouldUpdateTemplateWithNewPositions() {
        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                1L,
                List.of(
                        new PositionRequirementDto(1L, 3),
                        new PositionRequirementDto(2L, 2)
                ),
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                "Updated shift",
                Set.of(),
                true
        );

        Company company = new Company("Skynet",
                "Skynet",
                "12345678T",
                "test@test.com",
                "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        Position position1 = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Position position2 = Position.builder().id(2L).name("Manager").company(company).build();

        ShiftTemplate existing = ShiftTemplate.builder()
                .id(99L)
                .location(location)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .description("Old")
                .isActive(true)
                .build();

        when(shiftTemplateRepository.findById(99L)).thenReturn(Optional.of(existing));
        when(shiftTemplateDomainService.resolveLanguages(Set.of())).thenReturn(Set.of());
        doAnswer(invocation -> {
            ShiftTemplate template = invocation.getArgument(0);
            List<PositionRequirementDto> requirements = invocation.getArgument(1);
            // Simulate building position requirements
            template.getRequiredPositions().clear();
            for (PositionRequirementDto req : requirements) {
                Position pos = req.positionId() == 1L ? position1 : position2;
                com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition stp = 
                    com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition.builder()
                        .shiftTemplate(template)
                        .position(pos)
                        .requiredCount(req.requiredCount())
                        .build();
                template.getRequiredPositions().add(stp);
            }
            return null;
        }).when(shiftTemplateDomainService).buildPositionRequirements(existing, dto.requiredPositions());

        ShiftTemplate result = service.update(99L, dto);

        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(result.getDescription()).isEqualTo("Updated shift");
        assertThat(result.getRequiredPositions()).hasSize(2);
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                1L,
                List.of(new PositionRequirementDto(1L, 1)),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Test",
                Set.of(),
                true
        );

        when(shiftTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, dto))
                .isInstanceOf(ShiftTemplateNotFoundException.class)
                .hasMessageContaining("Shift template not found");
    }

    @Test
    void delete_shouldPerformSoftDelete() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        ShiftTemplate template = ShiftTemplate.builder()
                .id(99L)
                .location(location)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .isActive(true)
                .build();

        when(shiftTemplateRepository.findById(99L)).thenReturn(Optional.of(template));

        service.delete(99L, false);

        assertThat(template.getDeletedAt()).isNotNull();
        verify(shiftTemplateRepository, never()).delete(any(ShiftTemplate.class));
    }

    @Test
    void delete_shouldPerformHardDelete() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        ShiftTemplate template = ShiftTemplate.builder()
                .id(99L)
                .location(location)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .isActive(true)
                .build();

        when(shiftTemplateRepository.findById(99L)).thenReturn(Optional.of(template));

        service.delete(99L, true);

        verify(shiftTemplateRepository).delete(template);
    }

    @Test
    void findById_shouldReturnTemplate() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        ShiftTemplate template = ShiftTemplate.builder()
                .id(99L)
                .location(location)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .isActive(true)
                .build();

        when(shiftTemplateRepository.findById(99L)).thenReturn(Optional.of(template));

        ShiftTemplate result = service.findById(99L);

        assertThat(result.getId()).isEqualTo(99L);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(shiftTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ShiftTemplateNotFoundException.class)
                .hasMessageContaining("Shift template not found");
    }

    @Test
    void search_shouldReturnPaginatedResults() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        ShiftTemplate template = ShiftTemplate.builder()
                .id(99L)
                .location(location)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .isActive(true)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);
        when(shiftTemplateRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(template)));

        var result = service.search(1L, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(99L);
    }
}
