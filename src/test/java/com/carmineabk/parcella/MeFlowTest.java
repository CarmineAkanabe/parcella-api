package com.carmineabk.parcella;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MeFlowTest {

    @Autowired MockMvc mvc;

    private static final String CUSTOMER = """
            {
              "role": "CUSTOMER",
              "fullName": "Test Customer",
              "email": "me@test.com",
              "password": "password123",
              "paymentPhoneNumber": "237677777777",
              "preferredContactNumber": "237677777777",
              "registeredLocation": { "lat": 4.05, "lng": 9.70, "address": "Test street" }
            }
            """;

    private String registerAndLogin() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(CUSTOMER));
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"me@test.com\",\"password\":\"password123\"}"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    @Test
    void meReturnsCurrentUser() throws Exception {
        String token = registerAndLogin();
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@test.com"))
                .andExpect(jsonPath("$.registeredLocation.address").value("Test street"));
    }

    @Test
    void meWithoutTokenIs401() throws Exception {
        mvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void patchUpdatesNameAndLocation() throws Exception {
        String token = registerAndLogin();
        String patch = "{\"fullName\":\"New Name\",\"registeredLocation\":{\"lat\":3.9,\"lng\":11.5,\"address\":\"New place\"}}";
        mvc.perform(patch("/api/v1/me").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(patch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("New Name"))
                .andExpect(jsonPath("$.registeredLocation.lat").value(3.9))
                .andExpect(jsonPath("$.email").value("me@test.com"));
    }

    @Test
    void patchWithBadPhoneIs422() throws Exception {
        String token = registerAndLogin();
        mvc.perform(patch("/api/v1/me").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"paymentPhoneNumber\":\"123\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.paymentPhoneNumber").exists());
    }
}