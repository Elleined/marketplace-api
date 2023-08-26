package com.elleined.marketplaceapi.service.user;

import com.elleined.marketplaceapi.model.user.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
// Uses websocket public and private chat
public class PrincipalService {
    private User principal;

    public boolean hasNoLoggedInUser() {
        return principal == null;
    }
}