package com.shifterizator.shifterizatorbackend.auth;

import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class RoleAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;

    private User mockUser(Role role) {
        User u = new User("john", "mail", "pass", role, null);
        u.setId(1L);
        return u;
    }

    private void mockToken(Role role) {
        when(jwtUtil.getUsername("token")).thenReturn("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser(role)));
    }

    @Test
    void superadmin_should_access_everything() throws Exception {
        mockToken(Role.SUPERADMIN);

        mockMvc.perform(get("/api/test/superadmin")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());

        mockMvc.perform(get("/api/test/companyadmin")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());

        mockMvc.perform(get("/api/test/shiftmanager")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());


        mockMvc.perform(get("/api/test/readonlymanager")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());

        mockMvc.perform(get("/api/test/employee")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());
    }

    @Test
    void companyadmin_should_not_access_superadmin() throws Exception {
        mockToken(Role.COMPANYADMIN);

        mockMvc.perform(get("/api/test/superadmin")
                .header("Authorization", "Bearer token")).andExpect(status().isForbidden());

        mockMvc.perform(get("/api/test/companyadmin")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());

        mockMvc.perform(get("/api/test/shiftmanager")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());

        mockMvc.perform(get("/api/test/readonlymanager")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());

        mockMvc.perform(get("/api/test/employee")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());

    }

    @Test
    void shiftmanager_should_access_its_level_and_below() throws Exception {
        mockToken(Role.SHIFTMANAGER);

        mockMvc.perform(get("/api/test/superadmin")
                .header("Authorization", "Bearer token")).andExpect(status().isForbidden());

        mockMvc.perform(get("/api/test/companyadmin")
                .header("Authorization", "Bearer token")).andExpect(status().isForbidden());

        mockMvc.perform(get("/api/test/shiftmanager")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());

        mockMvc.perform(get("/api/test/readonlymanager")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());

        mockMvc.perform(get("/api/test/employee")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());
    }

    @Test
    void readonlymanager_should_access_its_level_and_below() throws Exception {
        mockToken(Role.READONLYMANAGER);

        mockMvc.perform(get("/api/test/superadmin")
                .header("Authorization", "Bearer token")).andExpect(status().isForbidden());

        mockMvc.perform(get("/api/test/companyadmin")
                .header("Authorization", "Bearer token")).andExpect(status().isForbidden());

        mockMvc.perform(get("/api/test/shiftmanager")
                .header("Authorization", "Bearer token")).andExpect(status().isForbidden());

        mockMvc.perform(get("/api/test/readonlymanager")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());

        mockMvc.perform(get("/api/test/employee")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());

    }

    @Test
    void employee_should_only_access_employee() throws Exception {
        mockToken(Role.EMPLOYEE);

        mockMvc.perform(get("/api/test/superadmin")
                .header("Authorization", "Bearer token")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/test/companyadmin")
                .header("Authorization", "Bearer token")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/test/shiftmanager")
                .header("Authorization", "Bearer token")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/test/readonlymanager")
                .header("Authorization", "Bearer token")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/test/employee")
                .header("Authorization", "Bearer token")).andExpect(status().isOk());
    }


}
