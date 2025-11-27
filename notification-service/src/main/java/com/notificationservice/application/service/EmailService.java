package com.notificationservice.application.service;

import com.notificationservice.domain.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final MessageService messageService;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.email.test-mode:false}")
    private boolean testMode;

    @Value("${app.email.default-locale:en}")
    private String defaultLocale;

    @Value("${app.email.from-address:noreply@yoursite.com}")
    private String fromAddress;

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendEmail(String to, String subject, String text, String lang) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Would send to: {} with subject: {}", to, subject);
            return;
        }

        if (testMode) {
            log.info("[TEST MODE] Email would be sent to: {} with subject: {}", to, subject);
            log.debug("[TEST MODE] Email content: {}", text);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            helper.setFrom(fromAddress);

            mailSender.send(message);
            log.info("Email sent successfully to: {} in language: {}", to, lang != null ? lang : defaultLocale);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email to: " + to, e);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email to: " + to, e);
        }
    }

    public void sendEmail(String to, String subject, String text) {
        sendEmail(to, subject, text, defaultLocale);
    }

    public void handleUserEvent(UserEvent event) {
        if (event == null) {
            log.warn("Received null user event");
            return;
        }

        String lang = event.getLanguage() != null ? event.getLanguage() : defaultLocale;
        Locale locale = messageService.resolveLocale(lang);

        if (locale == null) {
            log.warn("Resolved locale is null for language: {}, using default", lang);
            locale = Locale.ENGLISH;
        }

        log.info("Processing user event: {} for: {} in language: {} (from UserEvent)",
                event.getOperation(), event.getEmail(), locale.getLanguage());

        if (!isSupportedEventType(event.getOperation())) {
            log.warn("Unsupported event type: {}. No email will be sent for: {}",
                    event.getOperation(), event.getEmail());
            return;
        }

        String subject = messageService.getMessage("email.subject.notification", locale);
        String text = generateEmailText(event, locale);

        sendEmail(event.getEmail(), subject, text, lang);
    }

    private boolean isSupportedEventType(String operation) {
        if (operation == null) {
            return false;
        }

        String upperOperation = operation.toUpperCase();
        return "CREATE".equals(upperOperation) || "DELETE".equals(upperOperation);
    }

    private String generateEmailText(UserEvent event, Locale locale) {
        String username = event.getUsername() != null ? event.getUsername() : "User";

        return switch (event.getOperation().toUpperCase()) {
            case "CREATE" -> messageService.getMessage(
                    "email.welcome.create",
                    new Object[]{username},
                    locale
            );
            case "DELETE" -> messageService.getMessage(
                    "email.welcome.delete",
                    new Object[]{username},
                    locale
            );
            default -> {
                log.warn("Unexpected event type in generateEmailText: {}", event.getOperation());
                yield messageService.getMessage(
                        "email.welcome.generic",
                        new Object[]{username},
                        locale
                );
            }
        };
    }

    public void sendWelcomeEmail(String email, String username, String lang) {
        String resolvedLang = lang != null ? lang : defaultLocale;
        Locale locale = messageService.resolveLocale(resolvedLang);

        if (locale == null) {
            log.warn("Resolved locale is null for language: {}, using default", resolvedLang);
            locale = Locale.ENGLISH;
        }

        String subject = messageService.getMessage("email.subject.welcome", locale);
        String text = messageService.getMessage(
                "email.direct.welcome",
                new Object[]{username != null ? username : "User"},
                locale
        );

        sendEmail(email, subject, text, resolvedLang);
    }

    public void sendAccountDeletedEmail(String email, String username, String lang) {
        String resolvedLang = lang != null ? lang : defaultLocale;
        Locale locale = messageService.resolveLocale(resolvedLang);

        if (locale == null) {
            log.warn("Resolved locale is null for language: {}, using default", resolvedLang);
            locale = Locale.ENGLISH;
        }

        String subject = messageService.getMessage("email.subject.account_deleted", locale);
        String text = messageService.getMessage(
                "email.direct.account_deleted",
                new Object[]{username != null ? username : "User"},
                locale
        );

        sendEmail(email, subject, text, resolvedLang);
    }
}