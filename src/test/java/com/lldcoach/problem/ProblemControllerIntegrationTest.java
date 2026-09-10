package com.lldcoach.problem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Problem API endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProblemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllProblems_returnsThreeSeededProblems() throws Exception {
        mockMvc.perform(get("/api/problems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3));
    }

    @Test
    void getExistingProblem_returnsProblemWithCorrectFields() throws Exception {
        // Get all problems and find Parking Lot by title
        String response = mockMvc.perform(get("/api/problems"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Find Parking Lot problem ID using simple string search
        // The response is a JSON array, so we look for "Parking Lot" and then find its id
        int parkingLotStart = response.indexOf("\"title\":\"Parking Lot\"");
        assertTrue(parkingLotStart > 0, "Parking Lot should exist in response");
        
        // Look backwards to find the opening brace of this object
        int objectStart = response.lastIndexOf("{", parkingLotStart);
        // Find the id field within this object (format: "id":"xxx-xxx-xxx")
        int idStartMarker = response.indexOf("\"id\":\"", objectStart);
        int idStartIndex = idStartMarker + 6; // length of "\"id\":\""
        int idEndIndex = response.indexOf("\"", idStartIndex);
        String parkingLotId = response.substring(idStartIndex, idEndIndex);

        // Verify we can fetch this specific problem
        mockMvc.perform(get("/api/problems/" + parkingLotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(parkingLotId))
                .andExpect(jsonPath("$.title").value("Parking Lot"))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.difficulty").exists())
                .andExpect(jsonPath("$.requirements").exists());
    }

    @Test
    void getNonExistentProblem_returns404() throws Exception {
        String nonExistentId = "non-existent-id-12345";

        mockMvc.perform(get("/api/problems/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void parkingLotProblem_containsExpectedRequirements() throws Exception {
        String response = mockMvc.perform(get("/api/problems"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify Parking Lot exists and has expected content
        assertTrue(response.contains("Parking Lot"));
        assertTrue(response.contains("multiple floors"));
        assertTrue(response.contains("parking spots"));
        assertTrue(response.contains("vehicle types"));
    }
}
