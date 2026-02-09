package com.shifterizator.shifterizatorbackend.employee.service;

import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.PositionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PositionServiceImplTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private PositionServiceImpl positionService;

    @Test
    void create_shouldCreatePositionSuccessfully() {
        Company company = new Company();
        company.setId(1L);
        company.setName("Skynet");

        Position savedPosition = Position.builder().id(10L).name("Waiter").company(company).build();

        when(positionRepository.existsByNameAndCompany_Id("Waiter", 1L)).thenReturn(false);
        when(companyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(company));
        when(positionRepository.save(any(Position.class))).thenReturn(savedPosition);

        Position result = positionService.create("Waiter", 1L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Waiter");
    }

    @Test
    void create_shouldThrowWhenPositionAlreadyExists() {
        Company company = new Company();
        company.setId(1L);
        company.setName("Skynet");

        when(companyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(company));
        when(positionRepository.existsByNameAndCompany_Id("Waiter", 1L)).thenReturn(true);

        assertThatThrownBy(() -> positionService.create("Waiter", 1L))
                .isInstanceOf(PositionAlreadyExistsException.class)
                .hasMessage("Position already exists for this company");
    }

    @Test
    void create_shouldThrowWhenCompanyNotFound() {
        when(companyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> positionService.create("Waiter", 1L))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessage("Company not found");
    }

    @Test
    void update_shouldUpdatePositionSuccessfully() {
        Company company = new Company();
        company.setId(1L);
        company.setName("Skynet");

        Position position = Position.builder().id(10L).name("Old").company(company).build();

        when(positionRepository.findById(10L)).thenReturn(Optional.of(position));
        when(positionRepository.existsByNameAndCompany_Id("New", 1L)).thenReturn(false);

        Position result = positionService.update(10L, "New");

        assertThat(result.getName()).isEqualTo("New");
        assertThat(position.getName()).isEqualTo("New");
    }

    @Test
    void update_shouldThrowWhenPositionNotFound() {
        when(positionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> positionService.update(10L, "New"))
                .isInstanceOf(PositionNotFoundException.class)
                .hasMessage("Position not found");
    }

    @Test
    void update_shouldThrowWhenNewNameAlreadyExists() {
        Company company = new Company();
        company.setId(1L);
        company.setName("Skynet");

        Position position = Position.builder().id(10L).name("Old").company(company).build();

        when(positionRepository.findById(10L)).thenReturn(Optional.of(position));
        when(positionRepository.existsByNameAndCompany_Id("New", 1L)).thenReturn(true);

        assertThatThrownBy(() -> positionService.update(10L, "New"))
                .isInstanceOf(PositionAlreadyExistsException.class)
                .hasMessage("Position already exists for this company");
    }

    @Test
    void delete_shouldDeletePosition() {
        Position position = Position.builder().id(10L).build();

        when(positionRepository.findById(10L)).thenReturn(Optional.of(position));

        positionService.delete(10L);

        verify(positionRepository).delete(position);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(positionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> positionService.delete(10L))
                .isInstanceOf(PositionNotFoundException.class)
                .hasMessage("Position not found");
    }

    @Test
    void findById_shouldReturnPosition() {
        Company company = new Company();
        company.setId(1L);
        company.setName("Skynet");

        Position position = Position.builder().id(10L).name("Waiter").company(company).build();

        when(positionRepository.findById(10L)).thenReturn(Optional.of(position));

        Position result = positionService.findById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Waiter");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(positionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> positionService.findById(10L))
                .isInstanceOf(PositionNotFoundException.class)
                .hasMessage("Position not found");
    }

    @Test
    void findByCompany_shouldFilterByCompanyId() {
        Company company1 = new Company();
        company1.setId(1L);
        company1.setName("Skynet");

        Position pos1 = Position.builder().id(10L).name("Waiter").company(company1).build();

        when(positionRepository.findByCompany_Id(1L)).thenReturn(List.of(pos1));

        List<Position> result = positionService.findByCompany(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getName()).isEqualTo("Waiter");
    }
}
