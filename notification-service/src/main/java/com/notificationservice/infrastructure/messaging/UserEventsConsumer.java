package com.notificationservice.infrastructure.messaging;

import com.notificationservice.application.service.EmailService;
import com.notificationservice.domain.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventsConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "${kafka.topics.user-events}")
    public void consumeUserEvent(
            @Payload UserEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) {

        log.info("Received Kafka user event from topic: {}, partition: {}, offset: {}",
                topic, partition, offset);
        log.info("Processing user event: {} for: {} with language: {}",
                event.getOperation(), event.getEmail(), event.getLanguage());

        try {
            emailService.handleUserEvent(event);
            log.info("Successfully processed user event for: {} in language: {}",
                    event.getEmail(), event.getLanguage());
        } catch (Exception e) {
            log.error("Failed to process user event for: {}", event.getEmail(), e);
        }
    }
}