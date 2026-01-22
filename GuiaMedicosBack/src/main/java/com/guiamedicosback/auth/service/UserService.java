package com.guiamedicosback.auth.service;

import com.guiamedicosback.auth.entity.UserEntity;
import com.guiamedicosback.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String registerUser(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);

        return userRepository.save(new UserEntity(username, encodedPassword)).getUsername();
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findAll()
                .stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new User(username, user.getPassword(), new java.util.ArrayList<>());
    }

    public boolean validateCredentials(String username) {
        return userRepository.existsByUsername(username);
    }
}
