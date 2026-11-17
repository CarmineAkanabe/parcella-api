package com.carmineabk.parcella;

import com.carmineabk.parcella.domain.enums.Role;
import com.carmineabk.parcella.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest 
@AutoConfigureMockMvc 
@ActiveProfiles ("test")
class SecurityConfigTest {

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;

    @Test 
    void healthIsPublic() throws Exception {
        mvc.perform(get("/api/v1/health")).andExpect(status().isOk());
    }

    @Test
    void noTokenIs401() throws Exception {
        mvc.perform(get("/api/v1/admin/ping")).andExpect(status().isUnauthorized());
    }

    @Test
    void customerIs403() throws Exception {
        String token = tokens.issue(UUID.randomUUID(), Role.CUSTOMER);
        mvc.perform(get("/api/v1/admin/ping").header("Authorization", "Bearer " + token))
           .andExpect(status().isForbidden());
    }

    @Test
    void adminIs200() throws Exception {
        String token = tokens.issue(UUID.randomUUID(), Role.ADMIN);
        mvc.perform(get("/api/v1/admin/ping").header("Authorization", "Bearer " + token))
           .andExpect(status().isOk());
    }

    @Test
    void garbageTokenIs401Json() throws Exception {
        mvc.perform(get("/api/v1/admin/ping").header("Authorization", "Bearer not-a-real-token"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.title").value("Unauthorized"));
    }
}
