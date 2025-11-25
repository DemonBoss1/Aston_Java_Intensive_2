package com.notificationservice.application.service;

import com.notificationservice.domain.model.UserEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MessageService messageService;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        ReflectionTestUtils.setField(emailService, "testMode", false);
        ReflectionTestUtils.setField(emailService, "defaultLocale", "en");
        ReflectionTestUtils.setField(emailService, "fromAddress", "noreply@test.com");
    }

    @Test
    void shouldSendEmail() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Message";
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendEmail(to, subject, text, "en");

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void shouldHandleCreateUserEvent() {
        // Given
        UserEvent event = UserEvent.create("user@example.com", "John Doe", "en");
        when(messageService.resolveLocale("en")).thenReturn(Locale.ENGLISH);
        when(messageService.getMessage(eq("email.subject.notification"), any(Locale.class)))
                .thenReturn("Notification");
        when(messageService.getMessage(eq("email.welcome.create"), any(Object[].class), any(Locale.class)))
                .thenReturn("Hello, John Doe! Your account was created.");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.handleUserEvent(event);

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(messageService, times(1)).getMessage("email.subject.notification", Locale.ENGLISH);
        verify(messageService, times(1)).getMessage("email.welcome.create", new Object[]{"John Doe"}, Locale.ENGLISH);
    }

    @Test
    void shouldHandleCreateUserEventWithNullLocale() {
        // Given
        UserEvent event = UserEvent.create("user@example.com", "John Doe", "invalid");
        when(messageService.resolveLocale("invalid")).thenReturn(null);
        when(messageService.getMessage(eq("email.subject.notification"), any(Locale.class)))
                .thenReturn("Notification");
        when(messageService.getMessage(eq("email.welcome.create"), any(Object[].class), any(Locale.class)))
                .thenReturn("Hello, John Doe! Your account was created.");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.handleUserEvent(event);

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(messageService, times(1)).getMessage("email.subject.notification", Locale.ENGLISH);
        verify(messageService, times(1)).getMessage("email.welcome.create", new Object[]{"John Doe"}, Locale.ENGLISH);
    }

    @Test
    void shouldHandleDeleteUserEvent() {
        // Given
        UserEvent event = UserEvent.delete("user@example.com", "John Doe", "en");
        when(messageService.resolveLocale("en")).thenReturn(Locale.ENGLISH);
        when(messageService.getMessage(eq("email.subject.notification"), any(Locale.class)))
                .thenReturn("Notification");
        when(messageService.getMessage(eq("email.welcome.delete"), any(Object[].class), any(Locale.class)))
                .thenReturn("Hello, John Doe! Your account was deleted.");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.handleUserEvent(event);

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(messageService, times(1)).getMessage("email.welcome.delete", new Object[]{"John Doe"}, Locale.ENGLISH);
    }

    @Test
    void shouldHandleEventWithNullLanguage() {
        // Given
        UserEvent event = new UserEvent("CREATE", "user@example.com", "John Doe", null);
        when(messageService.resolveLocale("en")).thenReturn(Locale.ENGLISH);
        when(messageService.getMessage(eq("email.subject.notification"), any(Locale.class)))
                .thenReturn("Notification");
        when(messageService.getMessage(eq("email.welcome.create"), any(Object[].class), any(Locale.class)))
                .thenReturn("Hello, John Doe!");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.handleUserEvent(event);

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(messageService, times(1)).resolveLocale("en");
    }

    @Test
    void shouldHandleEventWithNullUsername() {
        // Given
        UserEvent event = new UserEvent("CREATE", "user@example.com", null, "en");
        when(messageService.resolveLocale("en")).thenReturn(Locale.ENGLISH);
        when(messageService.getMessage(eq("email.subject.notification"), any(Locale.class)))
                .thenReturn("Notification");
        when(messageService.getMessage(eq("email.welcome.create"), any(Object[].class), any(Locale.class)))
                .thenReturn("Hello, User!");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.handleUserEvent(event);

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(messageService, times(1)).getMessage("email.welcome.create", new Object[]{"User"}, Locale.ENGLISH);
    }

    @Test
    void shouldNotSendEmailForUnsupportedEventType() {
        // Given
        UserEvent event = new UserEvent("UPDATE", "user@example.com", "John Doe", "en");

        // When
        emailService.handleUserEvent(event);

        // Then
        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(messageService, never()).getMessage(anyString(), any(Object[].class), any(Locale.class));
    }

    @Test
    void shouldNotSendEmailForNullEventType() {
        // Given
        UserEvent event = new UserEvent(null, "user@example.com", "John Doe", "en");

        // When
        emailService.handleUserEvent(event);

        // Then
        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(messageService, never()).getMessage(anyString(), any(Object[].class), any(Locale.class));
    }

    @Test
    void shouldNotSendEmailForNullEvent() {
        // When
        emailService.handleUserEvent(null);

        // Then
        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(messageService, never()).getMessage(anyString(), any(Object[].class), any(Locale.class));
    }

    @Test
    void shouldSendWelcomeEmail() {
        // Given
        Locale russianLocale = Locale.forLanguageTag("ru");
        when(messageService.resolveLocale("ru")).thenReturn(russianLocale);
        when(messageService.getMessage(eq("email.subject.welcome"), any(Locale.class)))
                .thenReturn("Добро пожаловать!");
        when(messageService.getMessage(eq("email.direct.welcome"), any(Object[].class), any(Locale.class)))
                .thenReturn("Здравствуйте, Иван Иванов! Добро пожаловать!");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendWelcomeEmail("user@example.com", "Иван Иванов", "ru");

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(messageService, times(1)).getMessage("email.subject.welcome", russianLocale);
        verify(messageService, times(1)).getMessage("email.direct.welcome", new Object[]{"Иван Иванов"}, russianLocale);
    }

    @Test
    void shouldSendWelcomeEmailWithNullLanguage() {
        // Given
        when(messageService.resolveLocale("en")).thenReturn(Locale.ENGLISH);
        when(messageService.getMessage(eq("email.subject.welcome"), any(Locale.class)))
                .thenReturn("Welcome!");
        when(messageService.getMessage(eq("email.direct.welcome"), any(Object[].class), any(Locale.class)))
                .thenReturn("Hello, Test User! Welcome!");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendWelcomeEmail("user@example.com", "Test User", null);

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(messageService, times(1)).resolveLocale("en");
    }

    @Test
    void shouldSendWelcomeEmailWithNullLocale() {
        // Given
        when(messageService.resolveLocale("en")).thenReturn(null);
        when(messageService.getMessage(eq("email.subject.welcome"), any(Locale.class)))
                .thenReturn("Welcome!");
        when(messageService.getMessage(eq("email.direct.welcome"), any(Object[].class), any(Locale.class)))
                .thenReturn("Hello, Test User! Welcome!");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendWelcomeEmail("user@example.com", "Test User", "en");

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(messageService, times(1)).getMessage("email.subject.welcome", Locale.ENGLISH);
        verify(messageService, times(1)).getMessage("email.direct.welcome", new Object[]{"Test User"}, Locale.ENGLISH);
    }

    @Test
    void shouldSendAccountDeletedEmail() {
        // Given
        Locale spanishLocale = Locale.forLanguageTag("es");
        when(messageService.resolveLocale("es")).thenReturn(spanishLocale);
        when(messageService.getMessage(eq("email.subject.account_deleted"), any(Locale.class)))
                .thenReturn("Cuenta eliminada");
        when(messageService.getMessage(eq("email.direct.account_deleted"), any(Object[].class), any(Locale.class)))
                .thenReturn("¡Hola, Juan! Su cuenta ha sido eliminada.");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendAccountDeletedEmail("user@example.com", "Juan", "es");

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(messageService, times(1)).getMessage("email.subject.account_deleted", spanishLocale);
        verify(messageService, times(1)).getMessage("email.direct.account_deleted", new Object[]{"Juan"}, spanishLocale);
    }

    @Test
    void shouldSendAccountDeletedEmailWithEmptyUsername() {
        // Given
        when(messageService.resolveLocale("en")).thenReturn(Locale.ENGLISH);
        when(messageService.getMessage(eq("email.subject.account_deleted"), any(Locale.class)))
                .thenReturn("Account Deleted");
        when(messageService.getMessage(eq("email.direct.account_deleted"), any(Object[].class), any(Locale.class)))
                .thenReturn("Hello, User! Your account has been deleted.");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendAccountDeletedEmail("user@example.com", "", "en");

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(messageService, times(1)).getMessage("email.direct.account_deleted", new Object[]{""}, Locale.ENGLISH);
    }

    @Test
    void shouldNotSendEmailWhenDisabled() {
        // Given
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);

        // When
        emailService.sendEmail("test@example.com", "Subject", "Message", "en");

        // Then
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void shouldLogInTestMode() {
        // Given
        ReflectionTestUtils.setField(emailService, "testMode", true);

        // When
        emailService.sendEmail("test@example.com", "Subject", "Message", "en");

        // Then
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void shouldHandleMessagingExceptionGracefully() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Message";
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            emailService.sendEmail(to, subject, text, "en");
        });

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}