package com.invoice.app.repository;

import com.invoice.app.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("SELECT c FROM Client c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.address) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.gstNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Client> searchClients(@Param("query") String query);

    List<Client> findByNameContainingIgnoreCase(String name);
}
