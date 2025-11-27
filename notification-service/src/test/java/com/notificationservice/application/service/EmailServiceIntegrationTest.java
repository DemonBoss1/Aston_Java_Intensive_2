package com.notificationservice.application.service;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.notificationservice.domain.model.UserEvent;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Locale;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "app.email.enabled=true",
        "app.email.test-mode=false",
        "app.email.default-locale=en",
        "app.email.from-address=noreply@test.com",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.listener.missing-topics-fatal=false",
        "spring.mail.test-connection=false",
        "spring.mail.properties.mail.smtp.connectiontimeout=10000",
        "spring.mail.properties.mail.smtp.timeout=10000",
        "spring.mail.properties.mail.smtp.writetimeout=10000"
})
@Testcontainers
@DisplayName("Интеграционные тесты EmailService")
class EmailServiceIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    private static final ServerSetup SMTP_SERVER_SETUP = ServerSetupTest.SMTP;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(SMTP_SERVER_SETUP)
            .withConfiguration(GreenMailConfiguration.aConfig()
                    .withUser("test", "test")
                    .withDisabledAuthentication())
            .withPerMethodLifecycle(true);

    @Autowired
    private EmailService emailService;

    @SpyBean
    private MessageService messageService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> SMTP_SERVER_SETUP.getPort());
        registry.add("spring.mail.username", () -> "test");
        registry.add("spring.mail.password", () -> "test");
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "true");
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.connectiontimeout", () -> "10000");
        registry.add("spring.mail.properties.mail.smtp.timeout", () -> "10000");
        registry.add("spring.mail.properties.mail.smtp.writetimeout", () -> "10000");
    }

    @BeforeEach
    void setUp() {
        try {
            greenMail.purgeEmailFromAllMailboxes();
        } catch (FolderException e) {}
        reset(messageService);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("Успешная отправка простого email")
    void shouldSendSimpleEmailSuccessfully() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Message";

        // When
        emailService.sendEmail(to, subject, text, "en");

        // Then
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertEquals(1, receivedMessages.length, "Должно быть получено 1 письмо");

                    MimeMessage message = receivedMessages[0];
                    assertEquals(subject, message.getSubject());
                    assertEquals(to, message.getAllRecipients()[0].toString());
                    assertEquals("noreply@test.com", message.getFrom()[0].toString());

                    String content = extractMessageContent(message);
                    assertTrue(content.contains(text),
                            "Содержимое письма должно содержать текст: " + text + ", но содержит: " + content);
                });
    }

    @Test
    @DisplayName("Отправка email с русским текстом")
    void shouldSendEmailWithRussianText() {
        // Given
        String to = "test@example.com";
        String subject = "Тестовая тема";
        String text = "Тестовое сообщение на русском языке";

        // When
        emailService.sendEmail(to, subject, text, "ru");

        // Then
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertEquals(1, receivedMessages.length, "Должно быть получено 1 письмо");

                    MimeMessage message = receivedMessages[0];
                    assertEquals(subject, message.getSubject());
                    assertEquals(to, message.getAllRecipients()[0].toString());

                    String content = extractMessageContent(message);
                    assertTrue(content.contains(text),
                            "Содержимое письма должно содержать русский текст: " + text + ", но содержит: " + content);
                });
    }

    @Test
    @DisplayName("Обработка события CREATE пользователя")
    void shouldHandleUserCreateEvent() {
        // Given
        UserEvent event = UserEvent.create("user@example.com", "John Doe", "en");

        when(messageService.resolveLocale("en")).thenReturn(Locale.ENGLISH);
        when(messageService.getMessage(eq("email.subject.notification"), any(Locale.class)))
                .thenReturn("Welcome Notification");
        when(messageService.getMessage(eq("email.welcome.create"), any(Object[].class), any(Locale.class)))
                .thenReturn("Welcome John Doe! Your account has been created.");

        // When
        emailService.handleUserEvent(event);

        // Then
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertEquals(1, receivedMessages.length, "Должно быть получено 1 письмо");

                    MimeMessage message = receivedMessages[0];
                    assertEquals("Welcome Notification", message.getSubject());
                    assertEquals("user@example.com", message.getAllRecipients()[0].toString());

                    String content = extractMessageContent(message);
                    assertTrue(content.contains("John Doe") && content.contains("created"),
                            "Содержимое должно содержать имя пользователя и информацию о создании: " + content);

                    verify(messageService, times(1)).resolveLocale("en");
                    verify(messageService, times(1)).getMessage(eq("email.subject.notification"), any(Locale.class));
                    verify(messageService, times(1)).getMessage(eq("email.welcome.create"), any(Object[].class), any(Locale.class));
                });
    }

    @Test
    @DisplayName("Обработка события DELETE пользователя")
    void shouldHandleUserDeleteEvent() {
        // Given
        UserEvent event = UserEvent.delete("user@example.com", "Jane Smith", "en");

        when(messageService.resolveLocale("en")).thenReturn(Locale.ENGLISH);
        when(messageService.getMessage(eq("email.subject.notification"), any(Locale.class)))
                .thenReturn("Account Deletion Notification");
        when(messageService.getMessage(eq("email.welcome.delete"), any(Object[].class), any(Locale.class)))
                .thenReturn("Dear Jane Smith, your account has been deleted.");

        // When
        emailService.handleUserEvent(event);

        // Then
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertEquals(1, receivedMessages.length, "Должно быть получено 1 письмо");

                    MimeMessage message = receivedMessages[0];
                    assertEquals("Account Deletion Notification", message.getSubject());
                    assertEquals("user@example.com", message.getAllRecipients()[0].toString());

                    String content = extractMessageContent(message);
                    assertTrue(content.contains("Jane Smith") && content.contains("deleted"),
                            "Содержимое должно содержать имя пользователя и информацию об удалении: " + content);

                    verify(messageService, times(1)).resolveLocale("en");
                    verify(messageService, times(1)).getMessage(eq("email.subject.notification"), any(Locale.class));
                    verify(messageService, times(1)).getMessage(eq("email.welcome.delete"), any(Object[].class), any(Locale.class));
                });
    }

    @Test
    @DisplayName("Отправка приветственного письма")
    void shouldSendWelcomeEmail() {
        // Given
        String email = "welcome@example.com";
        String name = "Test User";
        String language = "en";

        when(messageService.resolveLocale("en")).thenReturn(Locale.ENGLISH);
        when(messageService.getMessage(eq("email.subject.welcome"), any(Locale.class)))
                .thenReturn("Welcome to Our Service!");
        when(messageService.getMessage(eq("email.direct.welcome"), any(Object[].class), any(Locale.class)))
                .thenReturn("Hello Test User, welcome to our platform!");

        // When
        emailService.sendWelcomeEmail(email, name, language);

        // Then
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertEquals(1, receivedMessages.length, "Должно быть получено 1 письмо");

                    MimeMessage message = receivedMessages[0];
                    assertEquals("Welcome to Our Service!", message.getSubject());
                    assertEquals(email, message.getAllRecipients()[0].toString());

                    String content = extractMessageContent(message);
                    assertTrue(content.contains("Test User") && content.contains("welcome"),
                            "Содержимое должно содержать приветствие для пользователя: " + content);

                    verify(messageService, times(1)).resolveLocale("en");
                    verify(messageService, times(1)).getMessage(eq("email.subject.welcome"), any(Locale.class));
                    verify(messageService, times(1)).getMessage(eq("email.direct.welcome"), any(Object[].class), any(Locale.class));
                });
    }

    @Test
    @DisplayName("Отправка письма об удалении аккаунта")
    void shouldSendAccountDeletedEmail() {
        // Given
        String email = "delete@example.com";
        String name = "Test User";
        String language = "en";

        when(messageService.resolveLocale("en")).thenReturn(Locale.ENGLISH);
        when(messageService.getMessage(eq("email.subject.account_deleted"), any(Locale.class)))
                .thenReturn("Account Deleted");
        when(messageService.getMessage(eq("email.direct.account_deleted"), any(Object[].class), any(Locale.class)))
                .thenReturn("Dear Test User, your account has been successfully deleted.");

        // When
        emailService.sendAccountDeletedEmail(email, name, language);

        // Then
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertEquals(1, receivedMessages.length, "Должно быть получено 1 письмо");

                    MimeMessage message = receivedMessages[0];
                    assertEquals("Account Deleted", message.getSubject());
                    assertEquals(email, message.getAllRecipients()[0].toString());

                    String content = extractMessageContent(message);
                    assertTrue(content.contains("Test User") && content.contains("deleted"),
                            "Содержимое должно содержать информацию об удалении аккаунта: " + content);

                    verify(messageService, times(1)).resolveLocale("en");
                    verify(messageService, times(1)).getMessage(eq("email.subject.account_deleted"), any(Locale.class));
                    verify(messageService, times(1)).getMessage(eq("email.direct.account_deleted"), any(Object[].class), any(Locale.class));
                });
    }

    @Test
    @DisplayName("Обработка события с null языком (должен использоваться язык по умолчанию)")
    void shouldHandleEventWithNullLanguage() {
        // Given
        UserEvent event = new UserEvent("CREATE", "user@example.com", "Test User", null);

        when(messageService.resolveLocale("en")).thenReturn(Locale.ENGLISH);
        when(messageService.getMessage(eq("email.subject.notification"), any(Locale.class)))
                .thenReturn("Welcome Notification");
        when(messageService.getMessage(eq("email.welcome.create"), any(Object[].class), any(Locale.class)))
                .thenReturn("Welcome Test User! Your account has been created.");

        // When
        emailService.handleUserEvent(event);

        // Then
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertEquals(1, receivedMessages.length, "Должно быть получено 1 письмо");

                    verify(messageService, times(1)).resolveLocale("en");
                    verify(messageService, times(1)).getMessage(eq("email.subject.notification"), any(Locale.class));
                    verify(messageService, times(1)).getMessage(eq("email.welcome.create"), any(Object[].class), any(Locale.class));
                });
    }

    @Test
    @DisplayName("Обработка неизвестного типа события (письмо не должно отправляться)")
    void shouldHandleUnknownEventType() {
        // Given
        UserEvent event = new UserEvent("UNKNOWN", "user@example.com", "Test User", "en");

        // When
        emailService.handleUserEvent(event);

        // Then
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertEquals(0, greenMail.getReceivedMessages().length,
                            "Для неизвестного типа события не должно отправляться писем");
                });

        verify(messageService, never()).getMessage(anyString(), any(Object[].class), any(Locale.class));
    }

    @Test
    @DisplayName("Отправка email с длинным текстом")
    void shouldSendEmailWithLongText() {
        // Given
        String to = "test@example.com";
        String subject = "Long Text Test";
        String longText = "Это очень длинное тестовое сообщение, которое содержит много текста. "
                + "Оно предназначено для проверки корректной обработки длинных сообщений в email. "
                + "Сообщение должно быть доставлено полностью без потери данных или форматирования.";

        // When
        emailService.sendEmail(to, subject, longText, "ru");

        // Then
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertEquals(1, receivedMessages.length, "Должно быть получено 1 письмо");

                    MimeMessage message = receivedMessages[0];
                    assertEquals(subject, message.getSubject());
                    assertEquals(to, message.getAllRecipients()[0].toString());

                    String content = extractMessageContent(message);
                    assertTrue(content.contains("длинное тестовое сообщение"),
                            "Содержимое должно содержать длинный текст: " + content);
                });
    }

    @Test
    @DisplayName("Проверка корректности заголовков письма")
    void shouldSendEmailWithCorrectHeaders() {
        // Given
        String to = "test@example.com";
        String subject = "Test Headers";
        String text = "Test message content";

        // When
        emailService.sendEmail(to, subject, text, "en");

        // Then
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                    assertEquals(1, receivedMessages.length, "Должно быть получено 1 письмо");

                    MimeMessage message = receivedMessages[0];

                    assertEquals(subject, message.getSubject());
                    assertEquals(to, message.getAllRecipients()[0].toString());
                    assertEquals("noreply@test.com", message.getFrom()[0].toString());
                    assertTrue(message.getContentType().contains("multipart/mixed"),
                            "Content-Type должен быть multipart/mixed, но: " + message.getContentType());
                });
    }

    /**
     * Улучшенный метод извлечения содержимого письма
     */
    private String extractMessageContent(MimeMessage message) {
        try {
            System.out.println("=== DEBUG: Starting message content extraction ===");
            System.out.println("Content-Type: " + message.getContentType());

            Object content = message.getContent();
            System.out.println("Content class: " + (content != null ? content.getClass().getName() : "null"));

            if (content instanceof String) {
                System.out.println("String content: " + content);
                return (String) content;
            } else if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                System.out.println("Multipart count: " + multipart.getCount());

                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    System.out.println("Part " + i + " Content-Type: " + bodyPart.getContentType());
                    System.out.println("Part " + i + " is text/plain: " + bodyPart.isMimeType("text/plain"));

                    if (bodyPart.isMimeType("text/plain")) {
                        Object partContent = bodyPart.getContent();
                        if (partContent instanceof String) {
                            String textContent = (String) partContent;
                            System.out.println("Text content: " + textContent);

                            if (bodyPart.getContentType().toLowerCase().contains("base64")) {
                                try {
                                    byte[] decodedBytes = Base64.getDecoder().decode(textContent.trim());
                                    textContent = new String(decodedBytes, "UTF-8");
                                    System.out.println("Decoded base64 content: " + textContent);
                                } catch (IllegalArgumentException e) {
                                    System.out.println("Not base64 encoded, using as is");
                                }
                            }

                            sb.append(textContent);
                        }
                    }

                    if (bodyPart.getContent() instanceof Multipart) {
                        sb.append(extractFromMultipart((Multipart) bodyPart.getContent()));
                    }
                }

                String result = sb.toString();
                System.out.println("Final extracted content: " + result);
                return result;
            }

            String result = content != null ? content.toString() : "";
            System.out.println("Final content: " + result);
            return result;

        } catch (Exception e) {
            System.err.println("Error extracting message content: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Рекурсивный метод для обработки вложенных multipart
     */
    private String extractFromMultipart(Multipart multipart) {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    Object content = bodyPart.getContent();
                    if (content instanceof String) {
                        sb.append((String) content);
                    }
                }
                if (bodyPart.getContent() instanceof Multipart) {
                    sb.append(extractFromMultipart((Multipart) bodyPart.getContent()));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}