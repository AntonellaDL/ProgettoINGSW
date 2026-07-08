package com.bugboard.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;

    private User mockUser;
    private final String rawPassword = "password123";
    private final String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

    @BeforeEach
    void setUp() {
        mockUser = new User("test@bugboard.com", hashedPassword, "Test User", null);
    }

    @Test
    @DisplayName("T19: Login con credenziali corrette")
    void login_ValidCredentials_ReturnsUser() {
        when(userRepository.findByEmail("test@bugboard.com")).thenReturn(Optional.of(mockUser));

        User result = userService.login("test@bugboard.com", rawPassword);

        assertNotNull(result);
        assertEquals("test@bugboard.com", result.getEmail());
    }

    @Test
    @DisplayName("T20: Login con password errata")
    void login_WrongPassword_ReturnsNull() {
        when(userRepository.findByEmail("test@bugboard.com")).thenReturn(Optional.of(mockUser));

        User result = userService.login("test@bugboard.com", "wrongPassword");

        assertNull(result);
    }

    @Test
    @DisplayName("T21: Login con email inesistente")
    void login_NonExistentEmail_ReturnsNull() {
        when(userRepository.findByEmail("unknown@bugboard.com")).thenReturn(Optional.empty());

        User result = userService.login("unknown@bugboard.com", rawPassword);

        assertNull(result);
    }
}