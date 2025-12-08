package cn.hdj.jpa_tdd.service;

import cn.hdj.jpa_tdd.entity.User;
import cn.hdj.jpa_tdd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllUsers_shouldReturnAllUsers() {
        // Given
        User user1 = new User(1L, "John Doe", "john@example.com");
        User user2 = new User(2L, "Jane Smith", "jane@example.com");
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.findAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(user1, user2);
        
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findUserById_shouldReturnUser_whenUserExists() {
        // Given
        User user = new User(1L, "John Doe", "john@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findUserById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
        
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void saveUser_shouldSaveAndReturnUser() {
        // Given
        User user = new User(null, "John Doe", "john@example.com");
        User savedUser = new User(1L, "John Doe", "john@example.com");
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.saveUser(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deleteUserById_shouldCallDeleteMethod() {
        // When
        userService.deleteUserById(1L);

        // Then
        verify(userRepository, times(1)).deleteById(1L);
    }
}