package com.elleined.marketplaceapi.service.password;

public interface EntityPasswordEncoder<T> {
    void encodePassword(T t, String rawPassword);
    boolean matches(T t, String rawPassword);
}
