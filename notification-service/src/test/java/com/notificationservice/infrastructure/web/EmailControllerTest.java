package com.notificationservice.infrastructure.web;

import com.notificationservice.application.dto.EmailRequest;
import com.notificationservice.application.service.EmailService;
import com.notificationservice.application.service.MessageService;
import com.notificationservice.domain.model.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailController.class)
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailService emailService;

    @MockBean
    private MessageService messageService;

    @Test
    void shouldHandleUserEvent() throws Exception {
        // Given
        UserEvent event = UserEvent.create("user@example.com", "John Doe", "en");

        // When & Then
        mockMvc.perform(post("/api/email/user-event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification processed successfully"));

        verify(emailService, times(1)).handleUserEvent(event);
    }

    @Test
    void shouldHandleUserEventWithRussianLanguage() throws Exception {
        // Given
        UserEvent event = UserEvent.create("user@example.com", "Иван Иванов", "ru");

        // When & Then
        mockMvc.perform(post("/api/email/user-event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk());

        verify(emailService, times(1)).handleUserEvent(event);
    }

    @Test
    void shouldSendEmail() throws Exception {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message");

        // When & Then
        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email sent successfully in en"));

        verify(emailService, times(1)).sendEmail(
                eq("test@example.com"),
                eq("Test Subject"),
                eq("Test Message"),
                eq("en")
        );
    }

    @Test
    void shouldSendEmailWithSpecificLanguage() throws Exception {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message");

        // When & Then
        mockMvc.perform(post("/api/email/send")
                        .param("lang", "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email sent successfully in ru"));

        verify(emailService, times(1)).sendEmail(
                eq("test@example.com"),
                eq("Test Subject"),
                eq("Test Message"),
                eq("ru")
        );
    }

    @Test
    void shouldSendWelcomeEmail() throws Exception {
        // Given
        when(messageService.isLocaleSupported("en")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/email/welcome")
                        .param("email", "test@example.com")
                        .param("username", "John Doe")
                        .param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome email sent successfully in en"));

        verify(emailService, times(1)).sendWelcomeEmail("test@example.com", "John Doe", "en");
    }

    @Test
    void shouldSendWelcomeEmailWithRussian() throws Exception {
        // Given
        when(messageService.isLocaleSupported("ru")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/email/welcome")
                        .param("email", "test@example.com")
                        .param("username", "Иван Иванов")
                        .param("lang", "ru"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome email sent successfully in ru"));

        verify(emailService, times(1)).sendWelcomeEmail("test@example.com", "Иван Иванов", "ru");
    }

    @Test
    void shouldReturnBadRequestForUnsupportedLanguage() throws Exception {
        // Given
        when(messageService.isLocaleSupported("fr")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/email/welcome")
                        .param("email", "test@example.com")
                        .param("username", "John Doe")
                        .param("lang", "fr"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unsupported language: fr. Supported: en, ru, es"));

        verify(emailService, never()).sendWelcomeEmail(any(), any(), any());
    }

    @Test
    void shouldSendAccountDeletedEmail() throws Exception {
        // Given
        when(messageService.isLocaleSupported("en")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/email/account-deleted")
                        .param("email", "test@example.com")
                        .param("username", "John Doe")
                        .param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string("Account deletion email sent successfully in en"));

        verify(emailService, times(1)).sendAccountDeletedEmail("test@example.com", "John Doe", "en");
    }

    @Test
    void shouldSendDirectEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/email/direct")
                        .param("to", "test@example.com")
                        .param("subject", "Hello")
                        .param("message", "Test message")
                        .param("lang", "es"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email sent successfully in es"));

        verify(emailService, times(1)).sendEmail("test@example.com", "Hello", "Test message", "es");
    }

    @Test
    void shouldGetSupportedLanguages() throws Exception {
        // Given
        when(messageService.getSupportedLocales()).thenReturn(List.of(
                Locale.ENGLISH,
                new Locale("ru"),
                new Locale("es")
        ));
        when(messageService.getMessage(eq("language.en"), any(Locale.class))).thenReturn("English");
        when(messageService.getMessage(eq("language.ru"), any(Locale.class))).thenReturn("Russian");
        when(messageService.getMessage(eq("language.es"), any(Locale.class))).thenReturn("Spanish");

        // When & Then
        mockMvc.perform(get("/api/email/supported-languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].code").value("en"))
                .andExpect(jsonPath("$[0].displayName").value("English"))
                .andExpect(jsonPath("$[1].code").value("ru"))
                .andExpect(jsonPath("$[1].displayName").value("Russian"))
                .andExpect(jsonPath("$[2].code").value("es"))
                .andExpect(jsonPath("$[2].displayName").value("Spanish"));
    }

    @Test
    void shouldReturnHealthCheck() throws Exception {
        mockMvc.perform(get("/api/email/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email service is running"));
    }

    @Test
    void shouldValidateEmailRequest() throws Exception {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("invalid-email");
        request.setSubject("");
        request.setMessage("");

        // When & Then
        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(emailService, never()).sendEmail(any(), any(), any(), any());
    }
}