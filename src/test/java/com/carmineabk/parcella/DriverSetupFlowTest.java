package com.carmineabk.parcella;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.carmineabk.parcella.support.AuthTestHelper.registerAndLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DriverSetupFlowTest {

    @Autowired MockMvc mvc;

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Test
    void pendingDriverCannotGoAvailable_butCanGoOffline() throws Exception {
        String token = registerAndLogin(mvc, "DRIVER");

        mvc.perform(patch("/api/v1/drivers/me/availability")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availabilityStatus\":\"AVAILABLE\"}"))
                .andExpect(status().isConflict());

        mvc.perform(patch("/api/v1/drivers/me/availability")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availabilityStatus\":\"OFFLINE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driver.availabilityStatus").value("OFFLINE"));
    }

    @Test
    void busyCannotBeSetByTheDriver() throws Exception {
        String token = registerAndLogin(mvc, "DRIVER");
        mvc.perform(patch("/api/v1/drivers/me/availability")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availabilityStatus\":\"BUSY\"}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void realImageIsAccepted() throws Exception {
        String token = registerAndLogin(mvc, "DRIVER");
        mvc.perform(multipart("/api/v1/drivers/me/license")
                        .file(new MockMultipartFile("file", "license.png", "image/png", PNG))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void fakeImageIsRejected_evenWithPngNameAndContentType() throws Exception {
        String token = registerAndLogin(mvc, "DRIVER");
        mvc.perform(multipart("/api/v1/drivers/me/license")
                        .file(new MockMultipartFile("file", "license.png", "image/png", "not an image".getBytes()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void customerIsForbidden() throws Exception {
        String token = registerAndLogin(mvc, "CUSTOMER");
        mvc.perform(multipart("/api/v1/drivers/me/license")
                        .file(new MockMultipartFile("file", "license.png", "image/png", PNG))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}