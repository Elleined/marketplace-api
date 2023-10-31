package com.elleined.marketplaceapi.service.atm.machine.validator;

import com.elleined.marketplaceapi.model.user.User;

public interface ATMLimitPerDayValidator {
    boolean reachedLimitAmountPerDay(User currentUser);
}
