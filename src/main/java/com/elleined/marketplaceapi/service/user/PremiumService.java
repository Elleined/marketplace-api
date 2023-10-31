package com.elleined.marketplaceapi.service.user;

import com.elleined.marketplaceapi.exception.resource.AlreadyExistException;
import com.elleined.marketplaceapi.exception.user.InsufficientBalanceException;
import com.elleined.marketplaceapi.model.user.Premium;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.PremiumRepository;
import com.elleined.marketplaceapi.service.fee.FeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PremiumService {
    private final PremiumRepository premiumRepository;
    private final FeeService feeService;


    public void upgradeToPremium(User user)
            throws InsufficientBalanceException, AlreadyExistException {

        if (user.isPremium() && !user.isPremiumSubscriptionExpired())
            throw new AlreadyExistException("You cannot purchase a premium account because you already have an active premium subscription. Please wait until your current premium account expires, which is set to expire on " + user.getPremium().getRegistrationDate().plusMonths(1) + ", before making another purchase.");
        if (user.isBalanceNotEnoughForPremium())
            throw new InsufficientBalanceException("You cannot purchase a premium account because your balance is insufficient to cover the premium user fee, which amounts to " + FeeService.PREMIUM_USER_FEE);
        Premium premium = Premium.builder()
                .registrationDate(LocalDateTime.now())
                .user(user)
                .build();
        premiumRepository.save(premium);
        feeService.payForPremium(user);

        log.debug("User with id of " + user.getId() + " are now upgraded to premium");
    }
}
