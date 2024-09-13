package user.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import user.user_service.dtos.UserDto;
import user.user_service.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    @Query("SELECT u FROM User u WHERE u.username =:username")
    Optional<User> findByUsername(String username);
    @Query("SELECT u FROM User u WHERE u.id =:userId")
    Optional<User> findByUserId(Long userId);
    //Do not bring other relations
    @Query("SELECT new user.user_service.dtos.UserDto(u.id, u.username) FROM User u WHERE u.id = :userId")
    Optional<UserDto> findOnlyUser(Long userId);
}