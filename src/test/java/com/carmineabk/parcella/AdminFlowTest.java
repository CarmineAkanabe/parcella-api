package com.carmineabk.parcella;

import com.carmineabk.parcella.support.AuthTestHelper.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.carmineabk.parcella.support.AuthTestHelper.admin;
import static com.carmineabk.parcella.support.AuthTestHelper.register;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminFlowTest {

    @Autowired MockMvc mvc;

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Test
    void driverQueue_filtersByStatus_andListsOnlyDrivers() throws Exception {
        Session driver = register(mvc, "DRIVER");
        register(mvc, "CUSTOMER");

        mvc.perform(get("/api/v1/admin/drivers").param("verificationStatus", "PENDING").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.items[?(@.id=='" + driver.id() + "')]").isNotEmpty())
                .andExpect(jsonPath("$.items[?(@.role=='CUSTOMER')]").isEmpty());
    }

    @Test
    void usersList_filtersByRole() throws Exception {
        register(mvc, "DRIVER");
        register(mvc, "CUSTOMER");

        mvc.perform(get("/api/v1/admin/users").param("role", "DRIVER").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.role!='DRIVER')]").isEmpty());
    }

    @Test
    void approvalNeedsALicense() throws Exception {
        Session driver = register(mvc, "DRIVER");
        decide(driver.id(), "APPROVED", null).andExpect(status().isConflict());
    }

    @Test
    void approvedDriverCanGoAvailable_andCannotBeDecidedTwice() throws Exception {
        Session driver = register(mvc, "DRIVER");
        uploadLicense(driver.token());

        decide(driver.id(), "APPROVED", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driver.verificationStatus").value("APPROVED"));

        mvc.perform(patch("/api/v1/drivers/me/availability")
                        .header("Authorization", "Bearer " + driver.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availabilityStatus\":\"AVAILABLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driver.availabilityStatus").value("AVAILABLE"));

        decide(driver.id(), "APPROVED", null).andExpect(status().isConflict());
    }

    @Test
    void rejectionNeedsANote_andResubmissionResetsToPending() throws Exception {
        Session driver = register(mvc, "DRIVER");
        uploadLicense(driver.token());

        decide(driver.id(), "REJECTED", null).andExpect(status().isUnprocessableContent());
        decide(driver.id(), "REJECTED", "License photo is blurry").andExpect(status().isOk());

        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + driver.token()))
                .andExpect(jsonPath("$.driver.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.driver.verificationNote").value("License photo is blurry"));

        mvc.perform(multipart("/api/v1/drivers/me/license")
                        .file(new MockMultipartFile("file", "l.png", "image/png", PNG))
                        .header("Authorization", "Bearer " + driver.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driver.verificationStatus").value("PENDING"))
                .andExpect(jsonPath("$.driver.verificationNote").value(nullValue()));
    }

    @Test
    void licenseIsServedToAdminsOnly() throws Exception {
        Session driver = register(mvc, "DRIVER");
        uploadLicense(driver.token());
        String url = "/api/v1/admin/drivers/" + driver.id() + "/license";

        mvc.perform(get(url).with(admin()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
        mvc.perform(get(url).header("Authorization", "Bearer " + driver.token()))
                .andExpect(status().isForbidden());
        mvc.perform(get(url)).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/admin/drivers/" + driver.id()).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(driver.id()))
                .andExpect(jsonPath("$.licenseImageUrl").isNotEmpty());
    }

    @Test
    void nonAdminsAreForbidden() throws Exception {
        Session customer = register(mvc, "CUSTOMER");
        mvc.perform(get("/api/v1/admin/drivers").header("Authorization", "Bearer " + customer.token()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + customer.token()))
                .andExpect(status().isForbidden());
    }

    // ---- helpers ----

    private ResultActions decide(String driverId, String decision, String note) throws Exception {
        String body = note == null
                ? "{\"decision\":\"" + decision + "\"}"
                : "{\"decision\":\"" + decision + "\",\"note\":\"" + note + "\"}";
        return mvc.perform(post("/api/v1/admin/drivers/" + driverId + "/verification")
                .with(admin()).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private void uploadLicense(String token) throws Exception {
        mvc.perform(multipart("/api/v1/drivers/me/license")
                        .file(new MockMultipartFile("file", "l.png", "image/png", PNG))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}