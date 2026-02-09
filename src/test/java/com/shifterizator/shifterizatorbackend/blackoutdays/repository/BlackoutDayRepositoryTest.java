package com.shifterizator.shifterizatorbackend.blackoutdays.repository;

import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import com.shifterizator.shifterizatorbackend.blackoutdays.spec.BlackoutDaySpecs;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import com.shifterizator.shifterizatorbackend.openinghours.repository.SpecialOpeningHoursRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BlackoutDayRepositoryTest {

    @Autowired
    private BlackoutDayRepository repository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private SpecialOpeningHoursRepository specialOpeningHoursRepository;

    @Autowired
    private EntityManager entityManager;

    private Location location1;
    private Location location2;

    @BeforeEach
    void setUp() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company = companyRepository.save(company);

        location1 = locationRepository.save(Location.builder()
                .name("HQ")
                .address("Main")
                .company(company)
                .build());

        location2 = locationRepository.save(Location.builder()
                .name("Branch")
                .address("Side")
                .company(company)
                .build());
    }

    @Test
    void findByLocation_IdAndDeletedAtIsNullOrderByDateAsc_shouldReturnOnlyNonDeletedOrdered() {
        BlackoutDay bd1 = BlackoutDay.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 24))
                .reason("Christmas")
                .build();
        BlackoutDay bd2 = BlackoutDay.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 31))
                .reason("New Year")
                .build();
        repository.save(bd2);
        repository.save(bd1);

        List<BlackoutDay> result =
                repository.findByLocation_IdAndDeletedAtIsNullOrderByDateAsc(location1.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDate()).isBefore(result.get(1).getDate());
    }

    @Test
    void findByLocation_IdAndDeletedAtIsNullOrderByDateAsc_shouldExcludeDeleted() {
        BlackoutDay bd = BlackoutDay.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 24))
                .reason("Christmas")
                .deletedAt(LocalDateTime.now())
                .build();
        repository.save(bd);

        List<BlackoutDay> result =
                repository.findByLocation_IdAndDeletedAtIsNullOrderByDateAsc(location1.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByLocation_Company_IdAndDeletedAtIsNullOrderByDateAsc_shouldReturnByCompany() {
        BlackoutDay bd1 = BlackoutDay.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 24))
                .reason("Christmas")
                .build();
        BlackoutDay bd2 = BlackoutDay.builder()
                .location(location2)
                .date(LocalDate.of(2024, 12, 25))
                .reason("Boxing Day")
                .build();
        repository.save(bd1);
        repository.save(bd2);

        List<BlackoutDay> result =
                repository.findByLocation_Company_IdAndDeletedAtIsNullOrderByDateAsc(location1.getCompany().getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(bd -> bd.getLocation().getId())
                .containsExactlyInAnyOrder(location1.getId(), location2.getId());
    }

    @Test
    void existsSpecialOpeningHoursForLocationAndDate_shouldReturnTrueWhenExists() {
        SpecialOpeningHours soh = SpecialOpeningHours.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 24))
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .reason("Christmas")
                .build();
        specialOpeningHoursRepository.save(soh);
        entityManager.flush();
        entityManager.clear();

        boolean exists = repository.existsSpecialOpeningHoursForLocationAndDate(
                location1.getId(),
                LocalDate.of(2024, 12, 24)
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsSpecialOpeningHoursForLocationAndDate_shouldReturnFalseWhenNotExists() {
        boolean exists = repository.existsSpecialOpeningHoursForLocationAndDate(
                location1.getId(),
                LocalDate.of(2024, 12, 24)
        );

        assertThat(exists).isFalse();
    }

    @Test
    void existsSpecialOpeningHoursForLocationAndDate_shouldReturnFalseWhenDeleted() {
        SpecialOpeningHours soh = SpecialOpeningHours.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 24))
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .reason("Christmas")
                .deletedAt(LocalDateTime.now())
                .build();
        specialOpeningHoursRepository.save(soh);
        entityManager.flush();
        entityManager.clear();

        boolean exists = repository.existsSpecialOpeningHoursForLocationAndDate(
                location1.getId(),
                LocalDate.of(2024, 12, 24)
        );

        assertThat(exists).isFalse();
    }

    @Test
    void existsSpecialOpeningHoursForLocationAndDate_shouldReturnFalseForDifferentLocation() {
        SpecialOpeningHours soh = SpecialOpeningHours.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 24))
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .reason("Christmas")
                .build();
        specialOpeningHoursRepository.save(soh);
        entityManager.flush();
        entityManager.clear();

        boolean exists = repository.existsSpecialOpeningHoursForLocationAndDate(
                location2.getId(),
                LocalDate.of(2024, 12, 24)
        );

        assertThat(exists).isFalse();
    }

    @Test
    void existsSpecialOpeningHoursForLocationAndDate_shouldReturnFalseForDifferentDate() {
        SpecialOpeningHours soh = SpecialOpeningHours.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 24))
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .reason("Christmas")
                .build();
        specialOpeningHoursRepository.save(soh);
        entityManager.flush();
        entityManager.clear();

        boolean exists = repository.existsSpecialOpeningHoursForLocationAndDate(
                location1.getId(),
                LocalDate.of(2024, 12, 25)
        );

        assertThat(exists).isFalse();
    }

    @Test
    void findAll_withNotDeletedAndInMonthSpecs_shouldFilterCorrectly() {
        BlackoutDay bdInMonth = BlackoutDay.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 24))
                .reason("Christmas")
                .build();
        BlackoutDay bdOtherMonth = BlackoutDay.builder()
                .location(location1)
                .date(LocalDate.of(2025, 1, 1))
                .reason("New Year")
                .build();
        repository.save(bdInMonth);
        repository.save(bdOtherMonth);

        Specification<BlackoutDay> spec = BlackoutDaySpecs.notDeleted()
                .and(BlackoutDaySpecs.byLocation(location1.getId()))
                .and(BlackoutDaySpecs.inMonth(YearMonth.of(2024, 12)));

        List<BlackoutDay> result = repository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2024, 12, 24));
    }
}
