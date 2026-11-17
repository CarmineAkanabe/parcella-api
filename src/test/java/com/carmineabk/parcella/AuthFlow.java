package com.carmineabk.parcella;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthFlowTest {

    @Autowired MockMvc mvc;

    private static final String CUSTOMER = """
            {
              "role": "CUSTOMER",
              "fullName": "Test Customer",
              "email": "Customer@Test.com",
              "password": "password123",
              "paymentPhoneNumber": "237677777777",
              "preferredContactNumber": "237677777777",
              "registeredLocation": { "lat": 4.05, "lng": 9.70, "address": "Test street" }
            }
            """;

    private void register(String json) throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void registerCreatesUserAndHidesPassword() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(CUSTOMER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.email").value("customer@test.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.registeredLocation.lat").value(4.05));
    }

    @Test
    void duplicateEmailIs409() throws Exception {
        register(CUSTOMER);
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(CUSTOMER))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidBodyIs422WithFieldErrors() throws Exception {
        String bad = CUSTOMER.replace("Customer@Test.com", "nope").replace("password123", "short");
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(bad))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void loginReturnsToken() throws Exception {
        register(CUSTOMER);
        String login = "{\"email\":\"customer@test.com\",\"password\":\"password123\"}";
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("customer@test.com"));
    }

    @Test
    void wrongPasswordIs401() throws Exception {
        register(CUSTOMER);
        String login = "{\"email\":\"customer@test.com\",\"password\":\"wrong-password\"}";
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isUnauthorized());
    }
}