package user.user_service.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import user.user_service.dtos.UserDto;
import user.user_service.entities.User;
import user.user_service.repositories.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();

        user1 = new User();
        user1.setUsername("john_doe");
        user1.setPassword("password1");
        user1.setDisplayName("John Doe");

        user2 = new User();
        user2.setUsername("jane_doe");
        user2.setPassword("password2");
        user2.setDisplayName("Jane Doe");

        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Test
    public void testFindByUsername() {
        Optional<User> foundUser = userRepository.findByUsername("john_doe");

        assertTrue(foundUser.isPresent());
        assertEquals("john_doe", foundUser.get().getUsername());
        assertEquals("John Doe", foundUser.get().getDisplayName());
    }

    @Test
    public void testFindByUserId() {
        Optional<User> foundUser = userRepository.findByUserId(user1.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(user1.getId(), foundUser.get().getId());
        assertEquals("john_doe", foundUser.get().getUsername());
        assertEquals("John Doe", foundUser.get().getDisplayName());
    }

    @Test
    public void testFindByUsernameNotFound() {
        Optional<User> foundUser = userRepository.findByUsername("unknown_user");

        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testFindByUserIdNotFound() {
        Optional<User> foundUser = userRepository.findByUserId(999L);

        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testFindOnlyUser_UserExists() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("password");
        user.setDisplayName("Test User");
        User savedUser = userRepository.save(user);

        Optional<UserDto> result = userRepository.findOnlyUser(savedUser.getId());

        UserDto expectedUserDto = new UserDto(savedUser.getId(), "testUser");
        assertEquals(Optional.of(expectedUserDto), result);
    }
}
