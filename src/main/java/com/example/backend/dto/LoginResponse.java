package com.example.backend.dto;

public class LoginResponse {

    private String message;
    private String token;
    private Long id;
    private String firstname;
    private String lastname;
    private String emailaddress;

    public LoginResponse(
            String message,
            String token,
            Long id,
            String firstname,
            String lastname,
            String emailaddress) {

        this.message = message;
        this.token = token;
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.emailaddress = emailaddress;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public Long getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmailaddress() {
        return emailaddress;
    }
}
