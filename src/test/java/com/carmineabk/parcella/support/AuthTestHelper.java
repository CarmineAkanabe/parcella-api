package com.carmineabk.parcella.support;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class AuthTestHelper {

    private AuthTestHelper() {}

    public record Session(String id, String token) {}

    public static Session register(MockMvc mvc, String role) throws Exception {
        String email = role.toLowerCase() + "-" + UUID.randomUUID() + "@test.dev";
        String password = "Passw0rd!123";
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"role":"%s","fullName":"Test User","email":"%s","password":"%s",
                     "paymentPhoneNumber":"237677777777","preferredContactNumber":"237677777777",
                     "registeredLocation":{"lat":4.05,"lng":9.70,"address":"Douala"}}
                    """.formatted(role, email, password))).andExpect(status().isCreated());
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.user.id");
        String token = JsonPath.read(body, "$.accessToken");
        return new Session(id, token);
    }

    public static String registerAndLogin(MockMvc mvc, String role) throws Exception {
        return register(mvc, role).token();
    }

    /** Authenticates a request as an admin without needing an admin row in the DB. */
    public static RequestPostProcessor admin() {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}