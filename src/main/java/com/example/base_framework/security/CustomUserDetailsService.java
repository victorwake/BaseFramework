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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var authorities = java.util.stream.Stream.concat(
                user.getRoles().stream().map(role -> role.getName()),
                user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(permission -> permission.getName())
        ).distinct().toList();

        System.out.println("AUTHORITIES: " + authorities);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities.toArray(new String[0]))
                .build();
    }
}
