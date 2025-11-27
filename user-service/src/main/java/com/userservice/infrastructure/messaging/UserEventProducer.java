package com.userservice.infrastructure.messaging;

import com.userservice.application.service.NotificationRestService;
import com.userservice.domain.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final NotificationRestService notificationRestService;

    private static final String USER_EVENTS_TOPIC = "user-events-topic";

    @Async
    public void sendUserEvent(UserEvent event) {
        sendViaKafka(event);
        sendViaRest(event);
    }

    private void sendViaKafka(UserEvent event) {
        try {
            kafkaTemplate.send(USER_EVENTS_TOPIC, event.getEmail(), event);
            log.info("Kafka event sent: {} for: {}", event.getOperation(), event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send Kafka event for: {} - Error: {}",
                    event.getEmail(), e.getMessage());
        }
    }

    private void sendViaRest(UserEvent event) {
        notificationRestService.sendUserEventNotification(event);
    }
}