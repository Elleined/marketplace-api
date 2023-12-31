package com.elleined.marketplaceapi.service.atm.machine.deposit;

import com.elleined.marketplaceapi.exception.atm.NotValidAmountException;
import com.elleined.marketplaceapi.exception.atm.amount.DepositAmountAboveMaximumException;
import com.elleined.marketplaceapi.exception.atm.amount.DepositAmountBelowMinimumException;
import com.elleined.marketplaceapi.exception.atm.limit.DepositLimitPerDayException;
import com.elleined.marketplaceapi.exception.resource.PictureNotValidException;
import com.elleined.marketplaceapi.mock.MultiPartFileDataFactory;
import com.elleined.marketplaceapi.model.atm.transaction.DepositTransaction;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.UserRepository;
import com.elleined.marketplaceapi.service.AppWalletService;
import com.elleined.marketplaceapi.service.atm.fee.ATMFeeService;
import com.elleined.marketplaceapi.service.atm.machine.validator.ATMValidator;
import com.elleined.marketplaceapi.service.image.ImageUploader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class DepositServiceTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private DepositTransactionService depositTransactionService;
    @Mock
    private ATMValidator atmValidator;
    @Mock
    private ATMFeeService feeService;

    @Mock
    private AppWalletService appWalletService;

    @Mock
    private ImageUploader imageUploader;

    @InjectMocks
    private DepositService depositService;

    @Test
    void deposit() {
        User user = User.builder()
                .balance(new BigDecimal(500))
                .build();

        BigDecimal amountToBeDeposited = new BigDecimal(400);
        float depositFee = 50;
        when(feeService.getDepositFee(amountToBeDeposited)).thenReturn(depositFee);
        depositService.deposit(user, amountToBeDeposited);

        verify(userRepository).save(user);
        verify(appWalletService).addAndSaveBalance(depositFee);
        assertEquals(new BigDecimal(850), user.getBalance());
    }

    @Test
    void requestDeposit() throws IOException {
        User user = User.builder()
                .depositTransactions(new ArrayList<>())
                .build();
        BigDecimal depositAmount = new BigDecimal(501);
        MultipartFile multipartFile = MultiPartFileDataFactory.notEmpty();

        DepositTransaction depositTransaction = new DepositTransaction();
        when(depositTransactionService.save(user, depositAmount, multipartFile)).thenReturn(depositTransaction);
        doNothing().when(imageUploader).upload(any(), any());
        depositService.requestDeposit(user, depositAmount, multipartFile);

        verify(depositTransactionService).save(user, depositAmount, multipartFile);
        verify(imageUploader).upload(any(), any());
        assertDoesNotThrow(() -> depositService.requestDeposit(user, depositAmount, multipartFile));
    }

    @Test
    @DisplayName("requesst withdraw validation 1: proof of transaction must be valid")
    void requestDepositProofOfTransactionMustBeValid() {
        User user = new User();
        BigDecimal amount = new BigDecimal(500);

        MultipartFile nullMultiPartFile = null;
        MultipartFile emptyMultiPartFile = MultiPartFileDataFactory.empty();

        assertThrows(PictureNotValidException.class, () -> depositService.requestDeposit(user, amount, nullMultiPartFile));
        assertThrows(PictureNotValidException.class, () -> depositService.requestDeposit(user, amount, emptyMultiPartFile));

        verifyNoInteractions(depositTransactionService, imageUploader);
    }

    @Test

    void shouldThrowBelowMinimumException() {
        User mockUser = new User();
        MultipartFile mockMultiPartFile = MultiPartFileDataFactory.notEmpty();
        BigDecimal belowMinimumAmount = new BigDecimal(499);

        assertNotNull(mockMultiPartFile);
        assertNotEquals(MultiPartFileDataFactory.empty(), mockMultiPartFile);
        assertThrows(DepositAmountBelowMinimumException.class, () -> depositService.requestDeposit(mockUser, belowMinimumAmount, mockMultiPartFile), "Failed because the deposit amount " + belowMinimumAmount + " is above to " + DepositService.MINIMUM_DEPOSIT_AMOUNT);
        verifyNoInteractions(depositTransactionService, imageUploader);
    }

    @Test
    void shouldThrowAboveMaximumException() {
        User mockUser = new User();
        MultipartFile mockMultiPartFile = MultiPartFileDataFactory.notEmpty();

        BigDecimal aboveMaximumAmount = new BigDecimal(10_001);

        assertNotNull(mockMultiPartFile);
        assertNotEquals(MultiPartFileDataFactory.empty(), mockMultiPartFile);
        assertThrows(DepositAmountAboveMaximumException.class, () -> depositService.requestDeposit(mockUser, aboveMaximumAmount, mockMultiPartFile), "Failed becuase the deposit amount " + aboveMaximumAmount + " is below the maximum amount " + DepositService.MAXIMUM_DEPOSIT_AMOUNT);

        verifyNoInteractions(depositTransactionService, imageUploader);
    }

    @Test
    void shouldThrowNotValidAmountException() throws IOException {
        User user = new User();

        when(atmValidator.isNotValidAmount(any(BigDecimal.class))).thenReturn(true);

        assertThrows(NotValidAmountException.class, () -> depositService.requestDeposit(user, new BigDecimal(0), MultiPartFileDataFactory.notEmpty()));

        verifyNoInteractions(depositTransactionService, imageUploader);
    }

    @Test
    void shouldThrowDepositLimitPerDayException() {
        User mockUser = new User();

        BigDecimal amount = new BigDecimal(500);
        MultipartFile mockMultiPartFile = MultiPartFileDataFactory.notEmpty();

        DepositTransaction depositTransaction = DepositTransaction.builder()
                .amount(new BigDecimal(5000))
                .transactionDate(LocalDateTime.now())
                .build();
        List<DepositTransaction> depositTransactions = Arrays.asList(
                depositTransaction,
                depositTransaction
        );
        mockUser.setDepositTransactions(depositTransactions);

        assertThrows(DepositLimitPerDayException.class, () -> depositService.requestDeposit(mockUser, amount, mockMultiPartFile));
        verifyNoInteractions(depositTransactionService, imageUploader);
    }

    @Test
    void isBelowMinimum() {
        BigDecimal amount = new BigDecimal(499);
        assertTrue(depositService.isBelowMinimum(amount));
    }

    @Test
    void isAboveMaximum() {
        BigDecimal amount = new BigDecimal(10_001);
        assertTrue(depositService.isAboveMaximum(amount));
    }

    @Test
    void reachedLimitAmountPerDay() {
        User user = new User();

        DepositTransaction depositTransaction1 = DepositTransaction.builder()
                .amount(new BigDecimal(5000))
                .transactionDate(LocalDateTime.now())
                .build();

        DepositTransaction depositTransaction2 = DepositTransaction.builder()
                .amount(new BigDecimal(5000))
                .transactionDate(LocalDateTime.now())
                .build();
        List<DepositTransaction> depositTransactions = Arrays.asList(depositTransaction1, depositTransaction2);
        user.setDepositTransactions(depositTransactions);

        assertTrue(depositService.reachedLimitAmountPerDay(user));
    }
}