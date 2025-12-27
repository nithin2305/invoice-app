package com.invoice.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoice.app.dto.ClientDTO;
import com.invoice.app.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

    @Test
    void searchClients_shouldReturnMatchingClients() throws Exception {
        ClientDTO client1 = new ClientDTO(1L, "ABC Corp", "123 Main St", "GST123", "1234567890", "abc@test.com");
        ClientDTO client2 = new ClientDTO(2L, "ABC Ltd", "456 Oak Ave", "GST456", "0987654321", "abcltd@test.com");

        when(clientService.searchClients(eq("ABC"), eq(10))).thenReturn(Arrays.asList(client1, client2));

        mockMvc.perform(get("/api/clients/search")
                        .param("q", "ABC")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("ABC Corp"))
                .andExpect(jsonPath("$[1].name").value("ABC Ltd"));
    }

    @Test
    void createClient_shouldReturnCreatedClient() throws Exception {
        ClientDTO inputDTO = new ClientDTO(null, "New Client", "789 Pine Rd", "GST789", "5555555555", "new@test.com");
        ClientDTO outputDTO = new ClientDTO(1L, "New Client", "789 Pine Rd", "GST789", "5555555555", "new@test.com");

        when(clientService.createClient(any(ClientDTO.class))).thenReturn(outputDTO);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Client"));
    }

    @Test
    void getClient_shouldReturnClient() throws Exception {
        ClientDTO client = new ClientDTO(1L, "Test Client", "Test Address", "GST123", "1234567890", "test@test.com");

        when(clientService.getClient(1L)).thenReturn(client);

        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Client"));
    }
}
