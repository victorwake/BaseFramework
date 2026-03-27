package com.example.base_framework.security;

import com.example.base_framework.entity.User;
import com.example.base_framework.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;



@NullMarked
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_USER", "USER_READ")
                .build();
    }
}
