package com.elleined.marketplaceapi.dto.forum;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ForumUserDTO(int id,
                           @NotBlank(message = "Please provide a value for the picture")
                      String picture,
                           @NotBlank(message = "Please provide a value for the name")
                      String name,
                           @NotBlank(message = "Please provide a value for the email")
                      String email,
                           @NotBlank(message = "Please provide a value for the UUID")
                      String UUID) {
}