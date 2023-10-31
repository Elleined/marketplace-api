package com.elleined.marketplaceapi.service.validator;

import com.elleined.marketplaceapi.exception.field.MalformedEmailException;
import org.springframework.stereotype.Service;

@Service
public class EmailValidator implements Validator<String> {
    @Override
    public void validate(String email) throws MalformedEmailException {
        if (!email.endsWith("@gmail.com"))
            throw new MalformedEmailException("Email must ends with @gmail.com!");
        if (email.startsWith("@"))
            throw new MalformedEmailException("Email should not starts with @!");
    }
}
