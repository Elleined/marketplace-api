package com.elleined.marketplaceapi.service.user;

import com.elleined.marketplaceapi.dto.CredentialDTO;
import com.elleined.marketplaceapi.dto.ShopDTO;
import com.elleined.marketplaceapi.dto.UserDTO;
import com.elleined.marketplaceapi.exception.field.FieldException;
import com.elleined.marketplaceapi.exception.field.HasDigitException;
import com.elleined.marketplaceapi.exception.field.MalformedEmailException;
import com.elleined.marketplaceapi.exception.field.MobileNumberException;
import com.elleined.marketplaceapi.exception.field.password.PasswordNotMatchException;
import com.elleined.marketplaceapi.exception.field.password.WeakPasswordException;
import com.elleined.marketplaceapi.exception.resource.AlreadyExistException;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.exception.user.InvalidUserCredentialException;
import com.elleined.marketplaceapi.exception.user.NoShopRegistrationException;
import com.elleined.marketplaceapi.exception.user.UserAlreadyVerifiedException;
import com.elleined.marketplaceapi.model.item.OrderItem;
import com.elleined.marketplaceapi.model.user.User;

import java.math.BigDecimal;
import java.util.Set;

public interface UserService extends PasswordService {
    int REGISTRATION_LIMIT_PROMO = 200;
    BigDecimal REGISTRATION_REWARD = new BigDecimal(50);

    // Info will also be saved in forum api
    // first 500 registrant will receive a token as a reward
    // After this method will automatically sends welcome email
    User saveByDTO(UserDTO dto)
            throws ResourceNotFoundException,
            HasDigitException,
            PasswordNotMatchException,
            WeakPasswordException,
            MalformedEmailException,
            AlreadyExistException,
            MobileNumberException;

    User getById(int id) throws ResourceNotFoundException;
    Set<User> getAllById(Set<Integer> userIds) throws ResourceNotFoundException;
    Set<User> getAllSeller();
    Set<User> searchAllSellerByName(String username);

    void resendValidId(User currentUser, String validId)
            throws UserAlreadyVerifiedException,
            NoShopRegistrationException,
            FieldException;


    // After this principal will be set to be use in web socket
    User login(CredentialDTO userCredentialDTO) throws ResourceNotFoundException, InvalidUserCredentialException;

    User getByEmail(String email) throws ResourceNotFoundException;

    void sendShopRegistration(User user, ShopDTO shopDTO) throws AlreadyExistException;

    OrderItem getOrderItemById(int orderItemId) throws ResourceNotFoundException;

    User getByReferralCode(String referralCode) throws ResourceNotFoundException;

}
