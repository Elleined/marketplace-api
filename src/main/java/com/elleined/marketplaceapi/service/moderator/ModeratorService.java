package com.elleined.marketplaceapi.service.moderator;

import com.elleined.marketplaceapi.dto.CredentialDTO;
import com.elleined.marketplaceapi.dto.ModeratorDTO;
import com.elleined.marketplaceapi.exception.atm.transaction.TransactionRejectedException;
import com.elleined.marketplaceapi.exception.atm.transaction.TransactionReleaseException;
import com.elleined.marketplaceapi.exception.field.NotValidBodyException;
import com.elleined.marketplaceapi.exception.product.ProductAlreadyListedException;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.exception.user.*;
import com.elleined.marketplaceapi.model.Moderator;
import com.elleined.marketplaceapi.model.atm.transaction.DepositTransaction;
import com.elleined.marketplaceapi.model.atm.transaction.WithdrawTransaction;
import com.elleined.marketplaceapi.model.product.RetailProduct;
import com.elleined.marketplaceapi.model.product.WholeSaleProduct;
import com.elleined.marketplaceapi.model.user.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

// Also acts as proxy for request inheritance
public interface    ModeratorService {
    Moderator getById(int moderatorId) throws ResourceNotFoundException;

    Moderator save(ModeratorDTO moderatorDTO);

    ModeratorDTO login(CredentialDTO moderatorCredentialDTO) throws ResourceNotFoundException, InvalidUserCredentialException;

    /** User **/
    List<User> getAllUnverifiedUser();

    void verifyUser(Moderator moderator, User userToBeVerified)
            throws NoShopRegistrationException,
            UserAlreadyVerifiedException,
            UserVerificationRejectionException;

    void verifyAllUser(Moderator moderator, Set<User> usersToBeVerified);

    // set the valid id to null make user rejected
    void rejectUser(Moderator moderator, User userToBeRejected)
            throws UserAlreadyVerifiedException;

    void rejectAllUser(Moderator moderator, Set<User> usersToBeRejected);


    /** Retail Product **/
    List<RetailProduct> getAllPendingRetailProduct();

    void listRetailProduct(Moderator moderator, RetailProduct productToBeListed);

    void listAllRetailProduct(Moderator moderator, Set<RetailProduct> productsToBeListed);

    void rejectRetailProduct(Moderator moderator, RetailProduct productToBeRejected)
            throws ProductAlreadyListedException;

    void rejectAllRetailProduct(Moderator moderator, Set<RetailProduct> productsToBeRejected);

    /** Whole Sale Product **/
    List<WholeSaleProduct> getAllPendingWholeSaleProduct();

    void listWholeSaleProduct(Moderator moderator, WholeSaleProduct productToBeListed);

    void listAllWholeSaleProduct(Moderator moderator, Set<WholeSaleProduct> productsToBeListed);

    void rejectWholeSaleProduct(Moderator moderator, WholeSaleProduct productToBeRejected)
            throws ProductAlreadyListedException;

    void rejectAllWholeSaleProduct(Moderator moderator, Set<WholeSaleProduct> productsToBeRejected);

    /** Deposit **/
    List<DepositTransaction> getAllPendingDepositRequest();
    void release(Moderator moderator, DepositTransaction depositTransaction)
            throws TransactionReleaseException,
            TransactionRejectedException;

    void releaseAllDepositRequest(Moderator moderator, Set<DepositTransaction> depositTransactions);
    void reject(Moderator moderator, DepositTransaction depositTransaction)
            throws TransactionReleaseException;
    void rejectAllDepositRequest(Moderator moderator, Set<DepositTransaction> depositTransactions);


    /** Withdraw **/
    List<WithdrawTransaction> getAllPendingWithdrawRequest();
    void release(Moderator moderator, WithdrawTransaction withdrawTransaction, MultipartFile proofOfTransaction)
            throws TransactionRejectedException,
            TransactionReleaseException,
            NotValidBodyException,
            InsufficientBalanceException, IOException;

    void releaseAllWithdrawRequest(Moderator moderator, Set<WithdrawTransaction> withdrawTransactions);
    void reject(Moderator moderator, WithdrawTransaction withdrawTransaction)
            throws TransactionReleaseException;
    void rejectAllWithdrawRequest(Moderator moderator, Set<WithdrawTransaction> withdrawTransactions);

}
