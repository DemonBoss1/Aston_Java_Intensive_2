package com.notificationservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private String operation; // "CREATE", "DELETE"
    private String email;
    private String username;
    private String language; // "en", "ru", "es"

    public static UserEvent create(String email, String username, String language) {
        return new UserEvent("CREATE", email, username, language);
    }

    public static UserEvent delete(String email, String username, String language) {
        return new UserEvent("DELETE", email, username, language);
    }

    public static UserEvent create(String email, String username) {
        return new UserEvent("CREATE", email, username, "en");
    }

    public static UserEvent delete(String email, String username) {
        return new UserEvent("DELETE", email, username, "en");
    }
}