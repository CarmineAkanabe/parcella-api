package com.carmineabk.parcella;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.carmineabk.parcella.support.AuthTestHelper.registerAndLogin;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AvatarFlowTest {

    @Autowired MockMvc mvc;

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @ParameterizedTest
    @ValueSource(strings = {"CUSTOMER", "DRIVER"})
    void upload_isServedPublicly_thenDeleteRemovesIt(String role) throws Exception {
        String token = registerAndLogin(mvc, role);

        String body = mvc.perform(multipart("/api/v1/me/avatar")
                        .file(new MockMultipartFile("file", "me.png", "image/png", PNG))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String url = "/api/v1" + JsonPath.read(body, "$.avatarUrl");

        mvc.perform(get(url)) // no token on purpose
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));

        mvc.perform(delete("/api/v1/me/avatar").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value(nullValue()));

        mvc.perform(get(url)).andExpect(status().isNotFound());
    }

    @Test
    void fakeImageIsRejected() throws Exception {
        String token = registerAndLogin(mvc, "CUSTOMER");
        mvc.perform(multipart("/api/v1/me/avatar")
                        .file(new MockMultipartFile("file", "me.png", "image/png", "not an image".getBytes()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void uploadNeedsAToken() throws Exception {
        mvc.perform(multipart("/api/v1/me/avatar")
                        .file(new MockMultipartFile("file", "me.png", "image/png", PNG)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void avatarOfUnknownUserIs404_notAnAuthError() throws Exception {
        mvc.perform(get("/api/v1/users/" + UUID.randomUUID() + "/avatar"))
                .andExpect(status().isNotFound());
    }
}