package com.elleined.marketplaceapi.exception.resource.exists;

public class MobileNumberExistsException extends AlreadyExistException {
    public MobileNumberExistsException(String message) {
        super(message);
    }
}
