package com.elleined.marketplaceapi.service.user;

import com.elleined.marketplaceapi.client.ForumClient;
import com.elleined.marketplaceapi.dto.CredentialDTO;
import com.elleined.marketplaceapi.dto.UserDTO;
import com.elleined.marketplaceapi.dto.forum.ForumUserDTO;
import com.elleined.marketplaceapi.exception.field.HasDigitException;
import com.elleined.marketplaceapi.exception.field.MalformedEmailException;
import com.elleined.marketplaceapi.exception.field.MobileNumberException;
import com.elleined.marketplaceapi.exception.field.NotValidBodyException;
import com.elleined.marketplaceapi.exception.field.password.PasswordException;
import com.elleined.marketplaceapi.exception.field.password.PasswordNotMatchException;
import com.elleined.marketplaceapi.exception.field.password.WeakPasswordException;
import com.elleined.marketplaceapi.exception.resource.ResourceException;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.exception.resource.exists.AlreadyExistException;
import com.elleined.marketplaceapi.exception.resource.exists.EmailAlreadyExistsException;
import com.elleined.marketplaceapi.exception.resource.exists.MobileNumberExistsException;
import com.elleined.marketplaceapi.exception.resource.exists.ShopNameAlreadyExistsException;
import com.elleined.marketplaceapi.exception.user.InvalidUserCredentialException;
import com.elleined.marketplaceapi.exception.user.NoShopRegistrationException;
import com.elleined.marketplaceapi.exception.user.UserAlreadyVerifiedException;
import com.elleined.marketplaceapi.mapper.ShopMapper;
import com.elleined.marketplaceapi.mapper.UserMapper;
import com.elleined.marketplaceapi.model.Shop;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.ShopRepository;
import com.elleined.marketplaceapi.repository.UserRepository;
import com.elleined.marketplaceapi.service.address.AddressService;
import com.elleined.marketplaceapi.service.image.ImageUploader;
import com.elleined.marketplaceapi.service.password.UserPasswordEncoder;
import com.elleined.marketplaceapi.service.validator.*;
import com.elleined.marketplaceapi.utils.DirectoryFolders;
import com.elleined.marketplaceapi.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl
        implements UserService,
        ReferralService,
        RegistrationPromoService,
        VerificationService {

    private final ShopMapper shopMapper;

    private final UserPasswordEncoder userPasswordEncoder;

    private final ImageUploader imageUploader;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final ShopRepository shopRepository;

    private final AddressService addressService;

    private final ForumClient forumClient;

    private final EmailValidator emailValidator;
    private final PasswordValidator passwordValidator;
    private final NumberValidator numberValidator;
    private final FullNameValidator fullNameValidator;

    @Value("${img-directory}")
    private String cropTradeImgDirectory;

    @Override
    public User getById(int id) throws ResourceNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User does not exists!"));
    }

    @Override
    public Set<User> getAllById(Set<Integer> userIds) throws ResourceNotFoundException {
        return userRepository.findAllById(userIds).stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<User> getAllSeller() {
        return userRepository.findAll().stream()
                .filter(User::isVerified)
                .filter(User::hasShopRegistration)
                .filter(User::isNotRejected) // Checking for rejected user
                .collect(Collectors.toSet());
    }

    @Override
    public Set<User> searchAllSellerByName(String username) {
        return userRepository.searchByUserName(username).stream()
                .filter(User::isVerified)
                .filter(User::hasShopRegistration)
                .filter(User::isNotRejected) // Checking for rejected user
                .collect(Collectors.toSet());
    }

    @Override
    public User saveByDTO(UserDTO userDTO)
            throws ResourceNotFoundException,
            HasDigitException,
            PasswordNotMatchException,
            WeakPasswordException,
            MalformedEmailException,
            AlreadyExistException,
            MobileNumberException {

        String email = userDTO.getUserCredentialDTO().getEmail();
        String mobileNumber = userDTO.getUserDetailsDTO().getMobileNumber();
        String password = userDTO.getUserCredentialDTO().getPassword();
        String confirmPassword = userDTO.getUserCredentialDTO().getConfirmPassword();

        numberValidator.validate(mobileNumber);
        fullNameValidator.validate(userDTO.getUserDetailsDTO());
        emailValidator.validate(email);
        passwordValidator.validate(password);
        if (passwordValidator.isPasswordNotMatch(password, confirmPassword))
            throw new PasswordNotMatchException("Password and confirm password not match!");
        if (userRepository.fetchAllEmail().contains(email))
            throw new EmailAlreadyExistsException("This email " + email + " is already associated with an account!");
        if (userRepository.fetchAllMobileNumber().contains(mobileNumber))
            throw new MobileNumberExistsException("Mobile number of " + mobileNumber + " are already associated with another account!");

        User registeringUser = userMapper.toEntity(userDTO);
        userPasswordEncoder.encodePassword(registeringUser, registeringUser.getUserCredential().getPassword());
        if (!StringUtil.isNotValid(userDTO.getInvitationReferralCode()))
            addInvitedUser(userDTO.getInvitationReferralCode(), registeringUser);

        userRepository.save(registeringUser);
        addressService.saveUserAddress(registeringUser, userDTO.getAddressDTO());
        // saveForumUser(registeringUser);

        log.debug("User with name of {} saved successfully with id of {}", registeringUser.getUserDetails().getFirstName(), registeringUser.getId());
        return registeringUser;
    }

    @Override
    public User saveByDTO(UserDTO dto, MultipartFile profilePicture) throws ResourceNotFoundException, HasDigitException, PasswordNotMatchException, WeakPasswordException, MalformedEmailException, AlreadyExistException, MobileNumberException, IOException {
        if (Validator.notValidMultipartFile(profilePicture)) throw new ResourceException("Profile picture attachment cannot be null!");
        User registeringUser = saveByDTO(dto);
        registeringUser.getUserDetails().setPicture(profilePicture.getOriginalFilename());

        userRepository.save(registeringUser);

        imageUploader.upload(cropTradeImgDirectory + DirectoryFolders.PROFILE_PICTURES_FOLDER, profilePicture);
        return registeringUser;
    }

    private void saveForumUser(User user) {
        ForumUserDTO forumUserDTO = ForumUserDTO.builder()
                .picture(user.getUserDetails().getPicture())
                .name(user.getFullName())
                .email(user.getUserCredential().getEmail())
                .UUID(user.getReferralCode())
                .build();
        forumClient.save(forumUserDTO);
        log.debug("Saving user with id of {} in forum api success", user.getId());
    }

    @Override
    public void resendValidId(User currentUser, MultipartFile validId)
            throws UserAlreadyVerifiedException, NoShopRegistrationException, IOException {

        if (Validator.notValidMultipartFile(validId)) throw new ResourceException("Picture attachment cannot be null!");
        if (currentUser.isVerified()) throw new UserAlreadyVerifiedException("Cannot resend valid id! you are already been verified");
        if (!currentUser.hasShopRegistration()) throw new NoShopRegistrationException("Cannot resent valid id! you need to submit a shop registration before resending you valid id.");

        currentUser.getUserVerification().setValidId(validId.getOriginalFilename());
        userRepository.save(currentUser);

        imageUploader.upload(cropTradeImgDirectory + DirectoryFolders.VALID_IDS_FOLDER, validId);
        log.debug("User with id of {} resended valid id {}", currentUser.getId(), validId);
    }


    @Override
    public User login(CredentialDTO userCredentialDTO)
            throws ResourceNotFoundException,
            InvalidUserCredentialException {

        String email = userCredentialDTO.getEmail();
        if (!userRepository.fetchAllEmail().contains(email)) throw new InvalidUserCredentialException("You have entered an invalid username or password");

        User user = getByEmail(userCredentialDTO.getEmail());
        if (!userPasswordEncoder.matches(user, userCredentialDTO.getPassword())) throw new InvalidUserCredentialException("You have entered an invalid username or password");
        log.debug("User with email of {} logged in marketplace api", userCredentialDTO.getEmail());
        return user;
    }

    @Override
    public User getByEmail(String email) throws ResourceNotFoundException {
        return userRepository.fetchByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User does not exists!"));
    }

    @Override
    public boolean isLegibleForRegistrationPromo() {
        return userRepository.findAll().size() <= REGISTRATION_LIMIT_PROMO;
    }

    @Override
    public void availRegistrationPromo(User registratingUser) {
        BigDecimal newBalance = registratingUser.getBalance().add(REGISTRATION_REWARD);
        registratingUser.setBalance(newBalance);
        userRepository.save(registratingUser);
        log.debug("Registrating user receives {} as registration reward for the first {} users", REGISTRATION_REWARD, REGISTRATION_LIMIT_PROMO);
    }

    @Override
    public void sendShopRegistration(User owner, String shopName, String description, MultipartFile shopPicture, MultipartFile validId) throws AlreadyExistException, IOException {
        if (Validator.notValidMultipartFile(validId)) throw new ResourceException("Valid id Picture attachment cannot be null!");
        if (Validator.notValidMultipartFile(shopPicture)) throw new ResourceException("Shop Picture attachment cannot be null!");
        if (StringUtil.isNotValid(shopName)) throw new NotValidBodyException("Cannot send shop registration! Please provide shop name!");
        if (StringUtil.isNotValid(description)) throw new NotValidBodyException("Cannot send shop registration! Please provide shop description!");
        if (owner.isVerified()) throw new UserAlreadyVerifiedException("Cannot send shop registration! because you are already been verified!");
        if (owner.hasShopRegistration()) throw new AlreadyExistException("Cannot send shop registration! because you already have shop registration! Please wait for email notification. If don't receive an email consider resending your valid id!");
        if (isShopNameAlreadyExists(shopName)) throw new ShopNameAlreadyExistsException("Cannot send shop registration! because the shop name you provided " + shopName + " already been taken by another seller!");

        Shop shop = shopMapper.toEntity(owner, shopName, description, shopPicture);
        owner.getUserVerification().setValidId(validId.getOriginalFilename());

        userRepository.save(owner);
        shopRepository.save(shop);

        imageUploader.upload(cropTradeImgDirectory + DirectoryFolders.VALID_IDS_FOLDER, validId);
        imageUploader.upload(cropTradeImgDirectory + DirectoryFolders.SHOP_PICTURES_FOLDER, shopPicture);
        log.debug("Shop registration of owner with id of {} success his verification are now visible in moderator", owner.getId());
    }

    @Override
    public User getByReferralCode(String referralCode) throws ResourceNotFoundException {
        return userRepository.fetchByReferralCode(referralCode).orElseThrow(() -> new ResourceNotFoundException("User does not exists!"));
    }

    @Override
    public void addInvitedUser(String invitingUserReferralCode, User invitedUser) {
        User invitingUser = getByReferralCode(invitingUserReferralCode);
        invitingUser.getReferredUsers().add(invitedUser);
        userRepository.save(invitingUser);
        log.debug("User with id of {} invited user with id of {} successfully", invitingUser.getId(), invitedUser.getId());
    }

    @Override
    public User getInvitingUser(User invitedUser) {
        return userRepository.findAll().stream()
                .map(User::getReferredUsers)
                .flatMap(Collection::stream)
                .filter(invitedUser::equals)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void changePassword(String email, String newPassword, String retypeNewPassword)
            throws PasswordException,
            ResourceNotFoundException {

        User user = getByEmail(email);
        if (passwordValidator.isPasswordNotMatch(newPassword, retypeNewPassword))
            throw new PasswordNotMatchException("New and re-type password not match!");
        passwordValidator.validate(newPassword);

        userPasswordEncoder.encodePassword(user, newPassword);
        userRepository.save(user);
        log.debug("User with id of {} successfully changed his/her password", user.getId());
    }

    @Override
    public void changePassword(User user, String oldPassword, String newPassword, String retypeNewPassword) throws PasswordException {
        if (!userPasswordEncoder.matches(user, oldPassword)) throw new PasswordNotMatchException("Old password didn't match to your current password!");
        if (passwordValidator.isPasswordNotMatch(newPassword, retypeNewPassword)) throw new PasswordNotMatchException("New and re-type password not match!");
        passwordValidator.validate(newPassword);

        userPasswordEncoder.encodePassword(user, newPassword);
        userRepository.save(user);
        log.debug("User with id of {} successfully changed his/her password", user.getId());
    }


    private boolean isShopNameAlreadyExists(String shopName) {
        return shopRepository.findAll().stream()
                .map(Shop::getName)
                .anyMatch(shopName::equals);
    }
}
