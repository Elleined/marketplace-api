package com.elleined.marketplaceapi.dto;

import com.elleined.marketplaceapi.dto.address.AddressDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private int id;
    private String referralCode;
    private String validId;
    private String status;

    @JsonProperty("isPremium")
    private boolean isPremium;

    private String invitationReferralCode;

    private BigDecimal balance;

    @Valid
    @NotNull(message = "Please provide a value for the User details")
    private UserDetailsDTO userDetailsDTO;

    @Valid
    @NotNull(message = "Please provide a value for the Address")
    private AddressDTO addressDTO;

    @Valid
    @NotNull(message = "Please provide a value for the Credentials")
    private CredentialDTO userCredentialDTO;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetailsDTO {
        @NotBlank(message = "Please provide a value for the first name")
        private String firstName;
        private String middleName;

        @NotBlank(message = "Please provide a value for the last name")
        private String lastName;

        private LocalDateTime registrationDate;

        @NotBlank(message = "Please provide a value for the gender")
        private String gender;

        @NotNull(message = "Please provide a value for the birthday")
        @Past(message = "Birth Date should be in the past")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthDate;


        @NotBlank(message = "Please provide a value for the mobile number")
        private String mobileNumber;

        private String picture;

        @NotBlank(message = "Suffix id must be greater than zero. If suffix use NONE")
        private String suffix;
    }
}
