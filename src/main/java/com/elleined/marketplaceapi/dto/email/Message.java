package com.elleined.marketplaceapi.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Message {

    @Email(message = "Please provide a proper email!")
    @NotBlank(message = "Please provide a value for the receiver email!")
    private String receiver;
}
