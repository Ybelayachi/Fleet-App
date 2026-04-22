package com.fleetkm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration du flux d'authentification.
 * Vérifie login, mauvais mot de passe, email inconnu, register.
 *
 * Le DataLoader seed automatiquement admin@example.com / Admin123! au démarrage.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_avecCredentialsValides_retourneToken() throws Exception {
        Map<String, String> body = Map.of(
                "email", "admin@example.com",
                "password", "Admin123!");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_avecMauvaisMotDePasse_retourne401() throws Exception {
        Map<String, String> body = Map.of(
                "email", "admin@example.com",
                "password", "mauvaisMotDePasse!");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_avecEmailInconnu_retourne401() throws Exception {
        Map<String, String> body = Map.of(
                "email", "inexistant@example.com",
                "password", "unMotDePasse!");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_avecDonneesValides_creeUtilisateur() throws Exception {
        Map<String, Object> body = Map.of(
                "email", "nouveau-auth-it@example.com",
                "password", "Password1!",
                "firstName", "Nouveau",
                "lastName", "Utilisateur");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nouveau-auth-it@example.com"));
    }

    @Test
    void register_avecEmailDejaExistant_retourne400() throws Exception {
        // admin@example.com est seedé par le DataLoader au démarrage
        Map<String, Object> body = Map.of(
                "email", "admin@example.com",
                "password", "Admin123!",
                "firstName", "Doublon",
                "lastName", "Admin");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_avecCorpsVide_retourne400() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
