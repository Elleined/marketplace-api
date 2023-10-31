package com.elleined.marketplaceapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialDTO {

    @NotBlank(message = "Please provide a value for the email")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Please provide a value for the password")
    private String password;

    private String confirmPassword;
}
