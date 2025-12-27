package com.invoice.app.service;

import com.invoice.app.config.GlobalExceptionHandler;
import com.invoice.app.dto.ClientDTO;
import com.invoice.app.entity.Client;
import com.invoice.app.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<ClientDTO> searchClients(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return clientRepository.searchClients(query.trim())
                .stream()
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClientDTO createClient(ClientDTO dto) {
        Client client = toEntity(dto);
        Client saved = clientRepository.save(client);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public ClientDTO getClient(Long id) {
        return clientRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Client not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ClientDTO toDTO(Client client) {
        return new ClientDTO(
                client.getId(),
                client.getName(),
                client.getAddress(),
                client.getGstNumber(),
                client.getPhone(),
                client.getEmail()
        );
    }

    private Client toEntity(ClientDTO dto) {
        Client client = new Client();
        client.setId(dto.getId());
        client.setName(dto.getName());
        client.setAddress(dto.getAddress());
        client.setGstNumber(dto.getGstNumber());
        client.setPhone(dto.getPhone());
        client.setEmail(dto.getEmail());
        return client;
    }
}
