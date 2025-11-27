package com.notificationservice.infrastructure.web;

import com.notificationservice.application.dto.EmailRequest;
import com.notificationservice.application.service.EmailService;
import com.notificationservice.application.service.MessageService;
import com.notificationservice.domain.model.UserEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final MessageService messageService;

    /**
     * Обработка user events через REST (для user-service)
     * Теперь используем language из UserEvent, а не из header
     */
    @PostMapping("/user-event")
    public ResponseEntity<String> handleUserEvent(@Valid @RequestBody UserEvent event) {

        log.info("Received REST user event: {} for: {} with language: {}",
                event.getOperation(), event.getEmail(), event.getLanguage());

        try {
            emailService.handleUserEvent(event);
            return ResponseEntity.ok("Notification processed successfully");
        } catch (Exception e) {
            log.error("Failed to process REST user event for: {}", event.getEmail(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to process notification: " + e.getMessage());
        }
    }

    /**
     * Отправка произвольного email
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(
            @Valid @RequestBody EmailRequest request,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {

        log.info("Received email send request for: {} with language: {}",
                request.getTo(), lang);

        try {
            emailService.sendEmail(request.getTo(), request.getSubject(),
                    request.getMessage(), lang);
            return ResponseEntity.ok("Email sent successfully in " + lang);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", request.getTo(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Отправка welcome email (удобный shortcut)
     */
    @PostMapping("/welcome")
    public ResponseEntity<String> sendWelcomeEmail(
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {

        log.info("Sending welcome email to: {} in language: {}", email, lang);

        if (!messageService.isLocaleSupported(lang)) {
            return ResponseEntity.badRequest()
                    .body("Unsupported language: " + lang + ". Supported: en, ru, es");
        }

        try {
            emailService.sendWelcomeEmail(email, username, lang);
            return ResponseEntity.ok("Welcome email sent successfully in " + lang);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", email, e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send welcome email: " + e.getMessage());
        }
    }

    /**
     * Отправка email о удалении аккаунта (удобный shortcut)
     */
    @PostMapping("/account-deleted")
    public ResponseEntity<String> sendAccountDeletedEmail(
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {

        log.info("Sending account deletion email to: {} in language: {}", email, lang);

        if (!messageService.isLocaleSupported(lang)) {
            return ResponseEntity.badRequest()
                    .body("Unsupported language: " + lang + ". Supported: en, ru, es");
        }

        try {
            emailService.sendAccountDeletedEmail(email, username, lang);
            return ResponseEntity.ok("Account deletion email sent successfully in " + lang);
        } catch (Exception e) {
            log.error("Failed to send account deletion email to: {}", email, e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send account deletion email: " + e.getMessage());
        }
    }

    /**
     * Прямая отправка email через параметры URL
     */
    @PostMapping("/direct")
    public ResponseEntity<String> sendDirectEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String message,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {

        log.info("Received direct email request for: {} in language: {}", to, lang);

        try {
            emailService.sendEmail(to, subject, message, lang);
            return ResponseEntity.ok("Email sent successfully in " + lang);
        } catch (Exception e) {
            log.error("Failed to send direct email to: {}", to, e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Получение списка поддерживаемых языков
     */
    @GetMapping("/supported-languages")
    public ResponseEntity<?> getSupportedLanguages() {
        return ResponseEntity.ok(messageService.getSupportedLocales().stream()
                .map(locale -> {
                    String displayName = messageService.getMessage(
                            "language." + locale.getLanguage(),
                            locale
                    );
                    return new LanguageInfo(locale.getLanguage(), displayName);
                })
                .toList());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Email service is running");
    }

    private record LanguageInfo(String code, String displayName) {}
}