package com.notificationservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    private static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
            Locale.ENGLISH,
            new Locale("ru", "RU"),
            new Locale("es", "ES")
    );

    public String getMessage(String code, Object[] args, Locale locale) {
        if (code == null || code.trim().isEmpty()) {
            log.error("Message code is null or empty");
            return "Message not configured";
        }

        try {
            Locale resolvedLocale = locale != null ? locale : Locale.ENGLISH;
            return messageSource.getMessage(code, args, resolvedLocale);
        } catch (org.springframework.context.NoSuchMessageException e) {
            log.warn("Message not found for code: '{}' in locale: {}, using default", code, locale);
            try {
                return messageSource.getMessage(code, args, Locale.ENGLISH);
            } catch (org.springframework.context.NoSuchMessageException ex) {
                log.error("Message not found for code: '{}' in default locale", code);
                return "Message not found: " + code;
            }
        } catch (Exception e) {
            log.error("Error retrieving message for code: '{}'", code, e);
            return "Error retrieving message: " + code;
        }
    }

    public String getMessage(String code, Locale locale) {
        return getMessage(code, null, locale);
    }

    public String getMessage(String code) {
        return getMessage(code, null, Locale.ENGLISH);
    }

    public Locale resolveLocale(String lang) {
        if (lang == null || lang.isEmpty()) {
            return Locale.ENGLISH;
        }

        return SUPPORTED_LOCALES.stream()
                .filter(locale -> locale.getLanguage().equalsIgnoreCase(lang))
                .findFirst()
                .orElse(Locale.ENGLISH);
    }

    public List<Locale> getSupportedLocales() {
        return SUPPORTED_LOCALES;
    }

    public boolean isLocaleSupported(String lang) {
        if (lang == null || lang.isEmpty()) {
            return false;
        }
        return SUPPORTED_LOCALES.stream()
                .anyMatch(locale -> locale.getLanguage().equalsIgnoreCase(lang));
    }
}