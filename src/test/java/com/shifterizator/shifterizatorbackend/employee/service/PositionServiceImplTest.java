package com.shifterizator.shifterizatorbackend.employee.service;

import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.mapper.PositionMapper;
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

    @Mock
    private PositionMapper positionMapper;

    @InjectMocks
    private PositionServiceImpl positionService;

    @Test
    void create_shouldCreatePositionSuccessfully() {
        Company company = new Company();
        company.setId(1L);
        company.setName("Skynet");

        Position position = Position.builder().id(10L).name("Waiter").company(company).build();
        PositionDto dto = new PositionDto(10L, "Waiter", 1L);

        when(positionRepository.existsByNameAndCompany_Id("Waiter", 1L)).thenReturn(false);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(positionRepository.save(any(Position.class))).thenReturn(position);
        when(positionMapper.toDto(any(Position.class))).thenReturn(dto);

        PositionDto result = positionService.create("Waiter", 1L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.name()).isEqualTo("Waiter");
    }

    @Test
    void create_shouldThrowWhenPositionAlreadyExists() {
        Company company = new Company();
        company.setId(1L);
        company.setName("Skynet");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(positionRepository.existsByNameAndCompany_Id("Waiter", 1L)).thenReturn(true);

        assertThatThrownBy(() -> positionService.create("Waiter", 1L))
                .isInstanceOf(PositionAlreadyExistsException.class)
                .hasMessage("Position already exists for this company");
    }

    @Test
    void create_shouldThrowWhenCompanyNotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

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
        PositionDto dto = new PositionDto(10L, "New", 1L);

        when(positionRepository.findById(10L)).thenReturn(Optional.of(position));
        when(positionRepository.existsByNameAndCompany_Id("New", 1L)).thenReturn(false);
        when(positionMapper.toDto(position)).thenReturn(dto);

        PositionDto result = positionService.update(10L, "New");

        assertThat(result.name()).isEqualTo("New");
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
        PositionDto dto = new PositionDto(10L, "Waiter", 1L);

        when(positionRepository.findById(10L)).thenReturn(Optional.of(position));
        when(positionMapper.toDto(position)).thenReturn(dto);

        PositionDto result = positionService.findById(10L);

        assertThat(result.id()).isEqualTo(10L);
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

        Company company2 = new Company();
        company2.setId(2L);
        company2.setName("Cyberdyne");

        Position pos1 = Position.builder().id(10L).name("Waiter").company(company1).build();
        Position pos2 = Position.builder().id(11L).name("Cook").company(company2).build();

        PositionDto dto1 = new PositionDto(10L, "Waiter", 1L);
        PositionDto dto2 = new PositionDto(11L, "Cook", 2L);

        when(positionRepository.findAll()).thenReturn(List.of(pos1, pos2));
        when(positionMapper.toDto(pos1)).thenReturn(dto1);
        lenient().when(positionMapper.toDto(pos2)).thenReturn(dto2);

        List<PositionDto> result = positionService.findByCompany(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
    }
}
