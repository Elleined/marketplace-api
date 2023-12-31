package com.elleined.marketplaceapi.service.user;

import com.elleined.marketplaceapi.exception.field.password.PasswordException;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.model.user.User;

public interface PasswordService {
    void changePassword(String email, String newPassword, String retypeNewPassword)
            throws PasswordException,
            ResourceNotFoundException;


    void changePassword(User user, String oldPassword, String newPassword, String retypeNewPassword) throws PasswordException;
}
