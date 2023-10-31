package com.elleined.marketplaceapi.dto.email;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class EmailMessage extends Message {

    @NotBlank(message = "Please provide a value for the email subject!")
    private String subject;

    @NotBlank(message = "Please provide a value for the email message!")
    private String messageBody;

    @Builder
    public EmailMessage(String receiver, String subject, String messageBody) {
        super(receiver);
        this.subject = subject;
        this.messageBody = messageBody;
    }
}
