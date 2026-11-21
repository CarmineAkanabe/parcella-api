package com.carmineabk.parcella; // match AuthFlowTest's package

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleFlowTest {

    @Autowired MockMvc mvc;

    @Test
    void firstVehicleIsActive_secondIsNot_activateSwitches() throws Exception {
        String token = registerAndLogin("DRIVER");
        String plate1 = uniquePlate();
        String plate2 = uniquePlate();
        create(token, plate1);
        String second = create(token, plate2);

        mvc.perform(get("/api/v1/drivers/me/vehicles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.plateNumber=='" + plate1 + "')].active").value(true))
                .andExpect(jsonPath("$[?(@.plateNumber=='" + plate2 + "')].active").value(false));

        mvc.perform(post("/api/v1/drivers/me/vehicles/" + idOf(second) + "/activate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mvc.perform(get("/api/v1/drivers/me/vehicles").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[?(@.active==true)]").value(hasSize(1)));
    }

    @Test
    void duplicatePlateIgnoringCaseAndSpacing_returns409() throws Exception {
        String token = registerAndLogin("DRIVER");
        String plate = uniquePlate();
        create(token, plate);

        String variant = plate.toLowerCase().replace(" ", "  "); // lowercase + double space
        mvc.perform(post("/api/v1/drivers/me/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CAR\",\"plateNumber\":\"" + variant + "\",\"maxPackageSize\":\"MEDIUM\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void customerIsForbidden() throws Exception {
        String token = registerAndLogin("CUSTOMER");
        mvc.perform(get("/api/v1/drivers/me/vehicles").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void anotherDriversVehicleIsNotFound() throws Exception {
        String owner = registerAndLogin("DRIVER");
        String intruder = registerAndLogin("DRIVER");
        String vehicle = create(owner, uniquePlate());
        mvc.perform(post("/api/v1/drivers/me/vehicles/" + idOf(vehicle) + "/activate")
                        .header("Authorization", "Bearer " + intruder))
                .andExpect(status().isNotFound());
    }

    // ---- helpers ----

    /** Plates are globally unique and the DB is only reset per class, so never reuse one. */
    private static String uniquePlate() {
        return "LT " + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    private String create(String token, String plate) throws Exception {
        return mvc.perform(post("/api/v1/drivers/me/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CAR\",\"plateNumber\":\"" + plate + "\",\"maxPackageSize\":\"MEDIUM\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private static String idOf(String json) {
        return JsonPath.read(json, "$.id");
    }

    private String registerAndLogin(String role) throws Exception {
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
        return JsonPath.read(body, "$.accessToken");
    }
}