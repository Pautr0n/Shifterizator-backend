package com.shifterizator.shifterizatorbackend.shift.repository;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.PositionRepository;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition;
import com.shifterizator.shifterizatorbackend.shift.spec.ShiftTemplateSpecs;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShiftTemplateRepositoryTest {

    @Autowired
    private ShiftTemplateRepository repository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private EntityManager entityManager;

    private Location location1;
    private Position position1;
    private Position position2;

    @BeforeEach
    void setUp() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company = companyRepository.save(company);

        location1 = locationRepository.save(Location.builder()
                .name("HQ")
                .address("Main")
                .company(company)
                .build());

        position1 = positionRepository.save(Position.builder()
                .name("Sales Assistant")
                .company(company)
                .build());

        position2 = positionRepository.save(Position.builder()
                .name("Manager")
                .company(company)
                .build());
    }

    @Test
    void findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByStartTimeAsc_shouldReturnOnlyActiveNonDeletedOrdered() {
        ShiftTemplate template1 = ShiftTemplate.builder()
                .location(location1)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .isActive(true)
                .build();
        ShiftTemplatePosition stp1 = ShiftTemplatePosition.builder()
                .shiftTemplate(template1)
                .position(position1)
                .requiredCount(2)
                .build();
        template1.setRequiredPositions(Set.of(stp1));
        repository.save(template1);

        ShiftTemplate template2 = ShiftTemplate.builder()
                .location(location1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .isActive(true)
                .build();
        ShiftTemplatePosition stp2 = ShiftTemplatePosition.builder()
                .shiftTemplate(template2)
                .position(position2)
                .requiredCount(1)
                .build();
        template2.setRequiredPositions(Set.of(stp2));
        repository.save(template2);

        entityManager.flush();
        entityManager.clear();

        List<ShiftTemplate> result = repository.findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByStartTimeAsc(location1.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStartTime()).isBefore(result.get(1).getStartTime());
    }

    @Test
    void findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByStartTimeAsc_shouldExcludeDeleted() {
        ShiftTemplate template = ShiftTemplate.builder()
                .location(location1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .isActive(true)
                .deletedAt(LocalDateTime.now())
                .build();
        ShiftTemplatePosition stp = ShiftTemplatePosition.builder()
                .shiftTemplate(template)
                .position(position1)
                .requiredCount(1)
                .build();
        template.setRequiredPositions(new HashSet<>(Set.of(stp)));
        repository.save(template);

        entityManager.flush();
        entityManager.clear();

        List<ShiftTemplate> result = repository.findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByStartTimeAsc(location1.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_withNotDeletedAndByPositionSpecs_shouldFilterCorrectly() {
        ShiftTemplate template1 = ShiftTemplate.builder()
                .location(location1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .isActive(true)
                .build();
        ShiftTemplatePosition stp1 = ShiftTemplatePosition.builder()
                .shiftTemplate(template1)
                .position(position1)
                .requiredCount(2)
                .build();
        template1.setRequiredPositions(new HashSet<>(Set.of(stp1)));
        repository.save(template1);

        ShiftTemplate template2 = ShiftTemplate.builder()
                .location(location1)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .isActive(true)
                .build();
        ShiftTemplatePosition stp2 = ShiftTemplatePosition.builder()
                .shiftTemplate(template2)
                .position(position2)
                .requiredCount(1)
                .build();
        template2.setRequiredPositions(new HashSet<>(Set.of(stp2)));
        repository.save(template2);

        entityManager.flush();
        entityManager.clear();

        Specification<ShiftTemplate> spec = ShiftTemplateSpecs.notDeleted()
                .and(ShiftTemplateSpecs.byPosition(position1.getId()));

        List<ShiftTemplate> result = repository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(template1.getId());
    }
}
