package com.userservice.application.service;

import com.userservice.domain.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRestService {

    private final RestTemplate restTemplate;

    @Value("${notification.service.base-url:http://localhost:8081}")
    private String notificationServiceBaseUrl;

    @Value("${notification.service.rest.enabled:true}")
    private boolean restNotificationsEnabled;

    public void sendUserEventNotification(UserEvent event) {
        if (!restNotificationsEnabled) {
            log.debug("REST notifications are disabled");
            return;
        }

        try {
            String url = notificationServiceBaseUrl + "/api/email/user-event";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (event.getLanguage() != null) {
                headers.set("Accept-Language", event.getLanguage());
            }

            HttpEntity<UserEvent> request = new HttpEntity<>(event, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("REST notification sent successfully for: {} - {} in language: {}",
                        event.getOperation(), event.getEmail(), event.getLanguage());
            } else {
                log.warn("REST notification failed for: {} - Status: {}",
                        event.getEmail(), response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to send REST notification for: {} - Error: {}",
                    event.getEmail(), e.getMessage());
        }
    }
}