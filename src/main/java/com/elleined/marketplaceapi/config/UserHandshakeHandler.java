package com.elleined.marketplaceapi.config;

import com.elleined.marketplaceapi.service.user.PrincipalService;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserHandshakeHandler extends DefaultHandshakeHandler {
    private final PrincipalService principalService;

    @Override
     protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        int currentUserId = principalService.getPrincipal().getId();
        log.debug("User with subscriber id of {} connected to the website", currentUserId);
        return new UserPrincipal(String.valueOf(currentUserId));
    }
}