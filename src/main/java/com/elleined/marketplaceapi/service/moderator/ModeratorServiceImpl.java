package com.elleined.marketplaceapi.service.moderator;

import com.elleined.marketplaceapi.dto.CredentialDTO;
import com.elleined.marketplaceapi.dto.ModeratorDTO;
import com.elleined.marketplaceapi.exception.atm.transaction.TransactionRejectedException;
import com.elleined.marketplaceapi.exception.atm.transaction.TransactionReleaseException;
import com.elleined.marketplaceapi.exception.field.NotValidBodyException;
import com.elleined.marketplaceapi.exception.product.ProductAlreadyListedException;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.exception.user.*;
import com.elleined.marketplaceapi.mapper.ModeratorMapper;
import com.elleined.marketplaceapi.model.Moderator;
import com.elleined.marketplaceapi.model.Product;
import com.elleined.marketplaceapi.model.atm.transaction.DepositTransaction;
import com.elleined.marketplaceapi.model.atm.transaction.WithdrawTransaction;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.ModeratorRepository;
import com.elleined.marketplaceapi.repository.atm.WithdrawTransactionRepository;
import com.elleined.marketplaceapi.service.atm.machine.validator.ATMValidator;
import com.elleined.marketplaceapi.service.image.ImageUploader;
import com.elleined.marketplaceapi.service.moderator.request.DepositRequest;
import com.elleined.marketplaceapi.service.moderator.request.ProductRequest;
import com.elleined.marketplaceapi.service.moderator.request.UserVerificationRequest;
import com.elleined.marketplaceapi.service.moderator.request.WithdrawRequest;
import com.elleined.marketplaceapi.service.password.EntityPasswordEncoder;
import com.elleined.marketplaceapi.service.validator.Validator;
import com.elleined.marketplaceapi.utils.DirectoryFolders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ModeratorServiceImpl implements ModeratorService, EntityPasswordEncoder<Moderator> {
    private final ModeratorRepository moderatorRepository;
    private final ModeratorMapper moderatorMapper;

    private final ATMValidator atmValidator;
    private final PasswordEncoder passwordEncoder;

    private final UserVerificationRequest userVerificationRequest;
    private final ProductRequest productRequest;
    private final WithdrawRequest withdrawRequest;
    private final DepositRequest depositRequest;

    private final WithdrawTransactionRepository withdrawTransactionRepository;

    private final ImageUploader imageUploader;

    @Value("${cropTrade.img.directory}")
    private String cropTradeImgDirectory;


    @Override
    public Moderator getById(int moderatorId) throws ResourceNotFoundException {
        return moderatorRepository.findById(moderatorId).orElseThrow(() -> new ResourceNotFoundException("Moderator does not exists!"));
    }

    @Override
    public Moderator save(ModeratorDTO moderatorDTO) {
        Moderator moderator = moderatorMapper.toEntity(moderatorDTO);
        this.encodePassword(moderator, moderatorDTO.moderatorCredentialDTO().getPassword());
        moderatorRepository.save(moderator);
        log.debug("Moderator name of {} successfully saved with id of {}", moderatorDTO.name(), moderatorDTO.id());
        return moderator;
    }

    @Override
    public ModeratorDTO login(CredentialDTO moderatorCredentialDTO) throws ResourceNotFoundException, InvalidUserCredentialException {
        String email = moderatorCredentialDTO.getEmail();
        if (!moderatorRepository.fetchAllEmail().contains(email))
            throw new InvalidUserCredentialException("You have provided an invalid username or password. Please check your credentials and try again!");
        Moderator moderator = moderatorRepository.fetchByEmail(moderatorCredentialDTO.getEmail()).orElseThrow(() -> new ResourceNotFoundException("Moderator does not exists!"));

        String rawPassword = moderatorCredentialDTO.getPassword();
        String encodedPassword = moderator.getModeratorCredential().getPassword();
        if (!passwordEncoder.matches(rawPassword, encodedPassword))
            throw new InvalidUserCredentialException("You have provided an invalid username or password. Please check your credentials and try again!");
        log.debug("Moderator with id of {} are now logged in", moderator.getId());
        return moderatorMapper.toDTO(moderator);
    }

    @Override
    public List<User> getAllUnverifiedUser() {
        return userVerificationRequest.getAllRequest();
    }



    @Override
    public void verifyUser(Moderator moderator, User userToBeVerified)
            throws NoShopRegistrationException,
            UserVerificationRejectionException,
            UserAlreadyVerifiedException {

        if (userToBeVerified.isVerified())
            throw new UserAlreadyVerifiedException("This user has already been verified.");
        if (!userToBeVerified.hasShopRegistration())
            throw new NoShopRegistrationException("This user does not have a pending shop registration. Please wait until user submits shop registration request first!");
        if (userToBeVerified.hasShopRegistration() && userToBeVerified.isRejected())
            throw new UserVerificationRejectionException("Your verification application has been rejected by a moderator. Please resubmit your valid ID and check your email for the reason behind the rejection.");
        // Add validation here

        userVerificationRequest.accept(moderator, userToBeVerified);
    }

    @Override
    public void verifyAllUser(Moderator moderator, Set<User> usersToBeVerified) {
        userVerificationRequest.acceptAll(moderator, usersToBeVerified);
    }

    @Override
    public void rejectUser(Moderator moderator, User userToBeRejected) throws UserAlreadyVerifiedException {
        if (userToBeRejected.isVerified())
            throw new UserAlreadyVerifiedException("The rejection cannot be processed because this user's verification request has already been verified!");
        // Add validation here
        userVerificationRequest.reject(moderator, userToBeRejected);
    }

    @Override
    public void rejectAllUser(Moderator moderator, Set<User> usersToBeRejected) {
        // Add validation here
        userVerificationRequest.rejectAll(moderator, usersToBeRejected);
    }

    @Override
    public List<Product> getAllPendingProduct() {
        return productRequest.getAllRequest();
    }

    @Override
    public void listProduct(Moderator moderator, Product productToBeListed) {
        // Add validation here
        productRequest.accept(moderator, productToBeListed);
    }

    @Override
    public void listAllProduct(Moderator moderator, Set<Product> productsToBeListed) {
        // Add validation here
        productRequest.acceptAll(moderator, productsToBeListed);
    }


    @Override
    public void rejectProduct(Moderator moderator, Product productToBeRejected) throws ProductAlreadyListedException {
        if (productToBeRejected.isListed())
            throw new ProductAlreadyListedException("The rejection cannot be processed because this product has already been listed!");
        // Add validation here
        productRequest.reject(moderator, productToBeRejected);
    }

    @Override
    public void rejectAllProduct(Moderator moderator, Set<Product> productsToBeRejected) {
        // Add validation here
        productRequest.rejectAll(moderator, productsToBeRejected);
    }

    @Override
    public List<DepositTransaction> getAllPendingDepositRequest() {
        return depositRequest.getAllRequest();
    }

    @Override
    public void release(Moderator moderator, DepositTransaction depositTransaction)
            throws TransactionReleaseException,
            TransactionRejectedException {

        if (depositTransaction.isRelease())
            throw new TransactionReleaseException("You cannot release this deposit because the transaction has already been released!");
        if (depositTransaction.isRejected())
            throw new TransactionRejectedException("You cannot release this deposit because the transaction has already been rejected!");
        // Add validation here
        depositRequest.accept(moderator, depositTransaction);
    }

    @Override
    public void releaseAllDepositRequest(Moderator moderator, Set<DepositTransaction> depositTransactions) {
        // Add validation here
        depositRequest.acceptAll(moderator, depositTransactions);
    }

    @Override
    public void reject(Moderator moderator, DepositTransaction depositTransaction) throws TransactionReleaseException {
        if (depositTransaction.isRelease())
            throw new TransactionReleaseException("You cannot reject this deposit because the transaction has already been released!");
        // Add validation here
        depositRequest.reject(moderator, depositTransaction);
    }

    @Override
    public void rejectAllDepositRequest(Moderator moderator, Set<DepositTransaction> depositTransactions) {
        // Add validation here
        depositRequest.rejectAll(moderator, depositTransactions);
    }

    @Override
    public List<WithdrawTransaction> getAllPendingWithdrawRequest() {
        return withdrawRequest.getAllRequest();
    }

    @Override
    public void release(Moderator moderator, WithdrawTransaction withdrawTransaction, MultipartFile proofOfTransaction)
            throws TransactionRejectedException, TransactionReleaseException, NotValidBodyException, InsufficientBalanceException, IOException {

        User requestingUserToWithdraw = withdrawTransaction.getUser();
        BigDecimal amountToBeWithdrawn = withdrawTransaction.getAmount();

        if (Validator.notValidMultipartFile(proofOfTransaction))
            throw new NotValidBodyException("You cannot release the withdraw request because you haven't provided proof of the transaction confirming that you've sent the money to the requesting user!");
        if (withdrawTransaction.isRelease())
            throw new TransactionReleaseException("You cannot release the withdraw request because this transaction has already been released!");
        if (withdrawTransaction.isRejected())
            throw new TransactionRejectedException("You cannot release the withdraw request because this transaction has already been rejected!");
        if (atmValidator.isBalanceEnough(requestingUserToWithdraw, amountToBeWithdrawn))
            throw new InsufficientBalanceException("You cannot release the withdraw request because the user's balance is insufficient. The current balance of the user is " + requestingUserToWithdraw.getBalance() + ", which is below the requested amount to be withdrawn, set at " + amountToBeWithdrawn);
        // Add validation here

        withdrawTransaction.setProofOfTransaction(proofOfTransaction.getOriginalFilename());
        withdrawTransactionRepository.save(withdrawTransaction);
        imageUploader.upload(cropTradeImgDirectory + DirectoryFolders.WITHDRAW_TRANSACTIONS_FOLDER, proofOfTransaction);

        withdrawRequest.accept(moderator, withdrawTransaction);
    }

    @Override
    public void releaseAllWithdrawRequest(Moderator moderator, Set<WithdrawTransaction> withdrawTransactions) {
        // Add validation here
        withdrawRequest.acceptAll(moderator, withdrawTransactions);
    }

    @Override
    public void reject(Moderator moderator, WithdrawTransaction withdrawTransaction) throws TransactionReleaseException {
        if (withdrawTransaction.isRelease())
            throw new TransactionReleaseException("You cannot reject this withdraw request because the transaction has already been released!");
        // Add validation here
        withdrawRequest.reject(moderator, withdrawTransaction);
    }

    @Override
    public void rejectAllWithdrawRequest(Moderator moderator, Set<WithdrawTransaction> withdrawTransactions) {
        // Add validation here
        withdrawRequest.rejectAll(moderator, withdrawTransactions);
    }

    @Override
    public void encodePassword(Moderator moderator, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        moderator.getModeratorCredential().setPassword(encodedPassword);
    }
}
