package com.example.manicure_backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityFlowTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private JsonNode register(String name, String email, boolean manicure) throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of(
                "nome", name, "email", email, "senha", "test-password-123",
                "especialidade", manicure ? "unhas" : ""));
        String response = mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(body)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        assertSafe(response);
        return mapper.readTree(response);
    }

    private String token(String email) throws Exception {
        String body = mapper.writeValueAsString(java.util.Map.of("email", email, "senha", "test-password-123"));
        JsonNode login = mapper.readTree(mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(login.toString()).doesNotContain("senha", "$2a$", "$2b$");
        return login.get("token").asText();
    }

    @Test
    void rejectsMissingInvalidAndExpiredAuthentication() throws Exception {
        mvc.perform(get("/agendamentos/meus")).andExpect(status().isUnauthorized());
        mvc.perform(get("/agendamentos/meus").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        String expired = Jwts.builder().setSubject("expired@example.invalid")
                .setIssuedAt(new Date(1_000)).setExpiration(new Date(2_000))
                .signWith(Keys.hmacShaKeyFor("test-only-insecure-secret-for-isolated-h2-tests-123456789"
                        .getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256).compact();
        mvc.perform(get("/agendamentos/meus").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void openApiDocumentationIsPublic() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void ownerAndParticipantsCanActButThirdPartyCannot() throws Exception {
        String random = suffix();
        String clientEmail = "cliente-" + random + "@example.invalid";
        String manicureEmail = "manicure-" + random + "@example.invalid";
        String thirdEmail = "terceiro-" + random + "@example.invalid";
        long clientId = register("Cliente", clientEmail, false).get("id").asLong();
        long manicureId = register("Manicure", manicureEmail, true).get("id").asLong();
        register("Terceiro", thirdEmail, false);
        String clientToken = token(clientEmail);
        String manicureToken = token(manicureEmail);
        String thirdToken = token(thirdEmail);

        String update = "{\"nome\":\"Cliente Atualizado\"}";
        String updated = mvc.perform(put("/usuarios/{id}", clientId)
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON).content(update)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertSafe(updated);
        mvc.perform(put("/usuarios/{id}", clientId).header("Authorization", "Bearer " + thirdToken)
                .contentType(MediaType.APPLICATION_JSON).content(update)).andExpect(status().isForbidden());
        mvc.perform(put("/usuarios/{id}", clientId).contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/usuarios/{id}", clientId).header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"A\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/usuarios/{id}", 999999).header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isNotFound());
        String postResponse = mvc.perform(post("/posts").header("Authorization", "Bearer " + manicureToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Exemplo\",\"descricao\":\"Portfolio\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertSafe(postResponse);
        assertThat(postResponse).doesNotContain("\"author\"");

        String booking = mapper.writeValueAsString(java.util.Map.of("manicureId", manicureId,
                "descricao", "Manicure", "data", "20/12/2030", "hora", "14:00"));
        String created = mvc.perform(post("/agendamentos").header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON).content(booking))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertSafe(created);
        long bookingId = mapper.readTree(created).get("idAgendamento").asLong();
        for (String participantToken : new String[]{clientToken, manicureToken}) {
            String response = mvc.perform(get("/agendamentos/{id}", bookingId)
                    .header("Authorization", "Bearer " + participantToken))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertSafe(response);
        }
        mvc.perform(get("/agendamentos/{id}", bookingId).header("Authorization", "Bearer " + thirdToken))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/agendamentos/{id}", bookingId).header("Authorization", "Bearer " + thirdToken))
                .andExpect(status().isForbidden());
        assertSafe(mvc.perform(get("/agendamentos/meus").header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertSafe(mvc.perform(get("/agendamentos/minha-agenda")
                .header("Authorization", "Bearer " + manicureToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        mvc.perform(delete("/agendamentos/{id}", bookingId).header("Authorization", "Bearer " + manicureToken))
                .andExpect(status().isNoContent());
        mvc.perform(get("/agendamentos/{id}", bookingId).header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isNotFound());
        String secondBooking = booking.replace("14:00", "15:00");
        long secondId = mapper.readTree(mvc.perform(post("/agendamentos")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON).content(secondBooking))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString())
                .get("idAgendamento").asLong();
        mvc.perform(delete("/agendamentos/{id}", secondId).header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/agendamentos/{id}", secondId).header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isNotFound());
        mvc.perform(post("/agendamentos").header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicProfilesDoNotRevealPrivateFields() throws Exception {
        String email = "public-" + suffix() + "@example.invalid";
        long id = register("Perfil Publico", email, false).get("id").asLong();
        String profile = mvc.perform(get("/usuarios/{id}", id)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertSafe(profile);
        assertThat(profile).doesNotContain(email, "email", "telefone", "cep");
    }

    private void assertSafe(String json) {
        assertThat(json).doesNotContain("senha", "test-password-123", "$2a$", "$2b$");
    }
}
