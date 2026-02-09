package com.shifterizator.shifterizatorbackend.openinghours.repository;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import com.shifterizator.shifterizatorbackend.openinghours.spec.SpecialOpeningHoursSpecs;
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
class SpecialOpeningHoursRepositoryTest {

    @Autowired
    private SpecialOpeningHoursRepository repository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private LocationRepository locationRepository;

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
        SpecialOpeningHours oh1 = SpecialOpeningHours.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 24))
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .reason("Christmas")
                .build();
        SpecialOpeningHours oh2 = SpecialOpeningHours.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 31))
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .reason("New Year")
                .build();
        repository.save(oh2);
        repository.save(oh1);

        List<SpecialOpeningHours> result =
                repository.findByLocation_IdAndDeletedAtIsNullOrderByDateAsc(location1.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDate()).isBefore(result.get(1).getDate());
    }

    @Test
    void findByLocation_IdAndDeletedAtIsNullOrderByDateAsc_shouldExcludeDeleted() {
        SpecialOpeningHours oh = SpecialOpeningHours.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 24))
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .reason("Christmas")
                .deletedAt(LocalDateTime.now())
                .build();
        repository.save(oh);

        List<SpecialOpeningHours> result =
                repository.findByLocation_IdAndDeletedAtIsNullOrderByDateAsc(location1.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByLocation_Company_IdAndDeletedAtIsNullOrderByDateAsc_shouldReturnByCompany() {
        SpecialOpeningHours oh1 = SpecialOpeningHours.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 24))
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .reason("Christmas")
                .build();
        SpecialOpeningHours oh2 = SpecialOpeningHours.builder()
                .location(location2)
                .date(LocalDate.of(2024, 12, 25))
                .openTime(LocalTime.of(10, 0))
                .closeTime(LocalTime.of(16, 0))
                .reason("Boxing Day")
                .build();
        repository.save(oh1);
        repository.save(oh2);

        List<SpecialOpeningHours> result =
                repository.findByLocation_Company_IdAndDeletedAtIsNullOrderByDateAsc(location1.getCompany().getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(oh -> oh.getLocation().getId())
                .containsExactlyInAnyOrder(location1.getId(), location2.getId());
    }

    @Test
    void findAll_withNotDeletedAndInMonthSpecs_shouldFilterCorrectly() {
        SpecialOpeningHours ohInMonth = SpecialOpeningHours.builder()
                .location(location1)
                .date(LocalDate.of(2024, 12, 24))
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .reason("Christmas")
                .build();
        SpecialOpeningHours ohOtherMonth = SpecialOpeningHours.builder()
                .location(location1)
                .date(LocalDate.of(2025, 1, 1))
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .reason("New Year")
                .build();
        repository.save(ohInMonth);
        repository.save(ohOtherMonth);

        Specification<SpecialOpeningHours> spec = SpecialOpeningHoursSpecs.notDeleted()
                .and(SpecialOpeningHoursSpecs.byLocation(location1.getId()))
                .and(SpecialOpeningHoursSpecs.inMonth(YearMonth.of(2024, 12)));

        List<SpecialOpeningHours> result = repository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2024, 12, 24));
    }
}

