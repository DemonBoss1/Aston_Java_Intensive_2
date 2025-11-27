package com.notificationservice.infrastructure.messaging;

import com.notificationservice.application.service.EmailService;
import com.notificationservice.domain.model.UserEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventsConsumerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserEventsConsumer userEventsConsumer;

    @Test
    void shouldConsumeUserCreateEvent() {
        // Given
        UserEvent event = UserEvent.create("user@example.com", "John Doe", "en");
        String topic = "user-events-topic";
        Integer partition = 0;
        Long offset = 1L;

        // When
        userEventsConsumer.consumeUserEvent(event, topic, partition, offset);

        // Then
        verify(emailService, times(1)).handleUserEvent(event);
    }

    @Test
    void shouldConsumeUserCreateEventWithRussianLanguage() {
        // Given
        UserEvent event = UserEvent.create("user@example.com", "Иван Иванов", "ru");
        String topic = "user-events-topic";
        Integer partition = 0;
        Long offset = 2L;

        // When
        userEventsConsumer.consumeUserEvent(event, topic, partition, offset);

        // Then
        verify(emailService, times(1)).handleUserEvent(event);
    }

    @Test
    void shouldConsumeUserDeleteEvent() {
        // Given
        UserEvent event = UserEvent.delete("user@example.com", "John Doe", "en");
        String topic = "user-events-topic";
        Integer partition = 0;
        Long offset = 3L;

        // When
        userEventsConsumer.consumeUserEvent(event, topic, partition, offset);

        // Then
        verify(emailService, times(1)).handleUserEvent(event);
    }

    @Test
    void shouldConsumeUserDeleteEventWithSpanishLanguage() {
        // Given
        UserEvent event = UserEvent.delete("user@example.com", "Juan Perez", "es");
        String topic = "user-events-topic";
        Integer partition = 1;
        Long offset = 1L;

        // When
        userEventsConsumer.consumeUserEvent(event, topic, partition, offset);

        // Then
        verify(emailService, times(1)).handleUserEvent(event);
    }

    @Test
    void shouldHandleExceptionWhenEmailFails() {
        // Given
        UserEvent event = UserEvent.create("user@example.com", "John Doe", "en");
        String topic = "user-events-topic";
        Integer partition = 0;
        Long offset = 4L;

        doThrow(new RuntimeException("Email failed")).when(emailService).handleUserEvent(event);

        // When & Then
        userEventsConsumer.consumeUserEvent(event, topic, partition, offset);

        verify(emailService, times(1)).handleUserEvent(event);
    }

    @Test
    void shouldHandleEventWithNullLanguage() {
        // Given
        UserEvent event = new UserEvent("CREATE", "user@example.com", "Test User", null);
        String topic = "user-events-topic";
        Integer partition = 0;
        Long offset = 5L;

        // When
        userEventsConsumer.consumeUserEvent(event, topic, partition, offset);

        // Then
        verify(emailService, times(1)).handleUserEvent(event);
    }

    @Test
    void shouldHandleEventWithNullUsername() {
        // Given
        UserEvent event = new UserEvent("DELETE", "user@example.com", null, "en");
        String topic = "user-events-topic";
        Integer partition = 0;
        Long offset = 6L;

        // When
        userEventsConsumer.consumeUserEvent(event, topic, partition, offset);

        // Then
        verify(emailService, times(1)).handleUserEvent(event);
    }
}