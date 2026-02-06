package com.shifterizator.shifterizatorbackend.employee.controller;

import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.mapper.PositionMapper;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.service.PositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@WebMvcTest(PositionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PositionService positionService;

    @MockitoBean
    private PositionMapper positionMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Position position;
    private PositionDto responseDto;
    private Company company;

    @BeforeEach
    void setup() {
        company = new Company("Company 1", "Legal 1", "12345678A", "c1@c.com", "+341111111");
        company.setId(1L);

        position = Position.builder()
                .id(10L)
                .name("Waiter")
                .company(company)
                .build();

        responseDto = new PositionDto(10L, "Waiter", 1L);
    }

    @Test
    void create_should_return_201_and_body() throws Exception {
        when(positionService.create(eq("Waiter"), eq(1L))).thenReturn(position);
        when(positionMapper.toDto(position)).thenReturn(responseDto);

        mockMvc.perform(post("/api/positions")
                        .with(csrf())
                        .param("name", "Waiter")
                        .param("companyId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Waiter"))
                .andExpect(jsonPath("$.companyId").value(1));

        verify(positionService).create("Waiter", 1L);
        verify(positionMapper).toDto(position);
    }

    @Test
    void create_should_return_404_when_company_not_found() throws Exception {
        when(positionService.create(eq("Waiter"), eq(99L)))
                .thenThrow(new CompanyNotFoundException("Company not found"));

        mockMvc.perform(post("/api/positions")
                        .with(csrf())
                        .param("name", "Waiter")
                        .param("companyId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(positionService).create("Waiter", 99L);
    }

    @Test
    void create_should_return_400_when_position_already_exists() throws Exception {
        when(positionService.create(eq("Waiter"), eq(1L)))
                .thenThrow(new PositionAlreadyExistsException("Position already exists for this company"));

        mockMvc.perform(post("/api/positions")
                        .with(csrf())
                        .param("name", "Waiter")
                        .param("companyId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Position already exists for this company"))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(positionService).create("Waiter", 1L);
    }

    @Test
    void update_should_return_200_and_body() throws Exception {
        when(positionService.update(eq(10L), eq("Manager"))).thenReturn(position);
        when(positionMapper.toDto(position)).thenReturn(responseDto);

        mockMvc.perform(put("/api/positions/10")
                        .with(csrf())
                        .param("name", "Manager"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Waiter"));

        verify(positionService).update(10L, "Manager");
        verify(positionMapper).toDto(position);
    }

    @Test
    void update_should_return_404_when_position_not_found() throws Exception {
        when(positionService.update(eq(99L), eq("Manager")))
                .thenThrow(new PositionNotFoundException("Position not found"));

        mockMvc.perform(put("/api/positions/99")
                        .with(csrf())
                        .param("name", "Manager"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Position not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(positionService).update(99L, "Manager");
    }

    @Test
    void update_should_return_400_when_name_already_exists() throws Exception {
        when(positionService.update(eq(10L), eq("ExistingPosition")))
                .thenThrow(new PositionAlreadyExistsException("Position already exists for this company"));

        mockMvc.perform(put("/api/positions/10")
                        .with(csrf())
                        .param("name", "ExistingPosition"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Position already exists for this company"))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(positionService).update(10L, "ExistingPosition");
    }

    @Test
    void delete_should_return_204() throws Exception {
        mockMvc.perform(delete("/api/positions/10").with(csrf()))
                .andExpect(status().isNoContent());

        verify(positionService).delete(10L);
    }

    @Test
    void delete_should_return_404_when_not_found() throws Exception {
        doThrow(new PositionNotFoundException("Position not found"))
                .when(positionService).delete(99L);

        mockMvc.perform(delete("/api/positions/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Position not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(positionService).delete(99L);
    }

    @Test
    void findById_should_return_200_and_body() throws Exception {
        when(positionService.findById(10L)).thenReturn(position);
        when(positionMapper.toDto(position)).thenReturn(responseDto);

        mockMvc.perform(get("/api/positions/10").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Waiter"));

        verify(positionService).findById(10L);
        verify(positionMapper).toDto(position);
    }

    @Test
    void findById_should_return_404_when_not_found() throws Exception {
        when(positionService.findById(99L))
                .thenThrow(new PositionNotFoundException("Position not found"));

        mockMvc.perform(get("/api/positions/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Position not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(positionService).findById(99L);
    }

    @Test
    void findByCompany_should_return_200_and_list() throws Exception {
        when(positionService.findByCompany(1L)).thenReturn(List.of(position));
        when(positionMapper.toDto(position)).thenReturn(responseDto);

        mockMvc.perform(get("/api/positions/company/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].name").value("Waiter"));

        verify(positionService).findByCompany(1L);
    }

    @Test
    void findByCompany_should_return_200_and_empty_list() throws Exception {
        when(positionService.findByCompany(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/positions/company/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(positionService).findByCompany(1L);
    }
}
