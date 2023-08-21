package com.elleined.marketplaceapi.service.user;

import com.elleined.marketplaceapi.dto.AddressDTO;
import com.elleined.marketplaceapi.dto.UserDTO;
import com.elleined.marketplaceapi.exception.AlreadExistException;
import com.elleined.marketplaceapi.exception.HasDigitException;
import com.elleined.marketplaceapi.exception.MobileNumberException;
import com.elleined.marketplaceapi.service.address.AddressService;
import com.elleined.marketplaceapi.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserDetailsValidator {

    private final UserService userService;
    private final AddressService addressService;

    public void validatePhoneNumber(UserDTO.UserDetailsDTO userDetailsDTO)
            throws AlreadExistException, MobileNumberException {
        String mobileNumber = userDetailsDTO.getMobileNumber();
        List<Character> letters = StringUtil.toCharArray(mobileNumber);

        if (userService.getAllMobileNumber().contains(mobileNumber)) throw new AlreadExistException("Mobile number of " + mobileNumber + " are already associated with another account!");
        if (letters.stream().anyMatch(Character::isLetter)) throw new MobileNumberException("Phone number cannot contain letters!");
        if (!mobileNumber.startsWith("09")) throw new MobileNumberException("Phone number must starts with 09!");
        if (mobileNumber.length() != 11) throw new MobileNumberException("Phone number must be 11 digits long!");
    }

    public void validateFullName(UserDTO.UserDetailsDTO userDetailsDTO) throws HasDigitException {
        String firstName = userDetailsDTO.getFirstName();
        String middleName = userDetailsDTO.getMiddleName();
        String lastName = userDetailsDTO.getLastName();

        List<Character> firstNameLetters = StringUtil.toCharArray(firstName);
        List<Character> middleNameLetters = StringUtil.toCharArray(middleName);
        List<Character> lastNameLetters = StringUtil.toCharArray(lastName);

        if (firstNameLetters.stream().anyMatch(Character::isDigit)) throw new HasDigitException("First name must not contain any digit!");
        if (middleNameLetters.stream().anyMatch(Character::isDigit)) throw new HasDigitException("Middle name must not contain any digit!");
        if (lastNameLetters.stream().anyMatch(Character::isDigit)) throw new HasDigitException("Last name must not contain any digit!");
    }

    public void validateAddressDetails(AddressDTO addressDTO) throws AlreadExistException {
        if (addressService.getAllAddressDetails().contains(addressDTO.getDetails())) throw new AlreadExistException("Address details of " + addressDTO.getDetails() + " already been taken by another user");
    }
}