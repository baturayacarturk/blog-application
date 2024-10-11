package user.user_service.services;

import user.user_service.dtos.AuthenticationDto;
import user.user_service.dtos.UserDto;
import user.user_service.entities.User;

import java.util.Optional;

public interface UserService {
    AuthenticationDto register(AuthenticationDto authenticationDto);

    AuthenticationDto authenticate(AuthenticationDto authenticationDto);

    Optional<User> findByUserId(Long userId);

    Optional<User> findByUsername(String username);

    //Without loading entire User but only user
    Optional<UserDto> findOnlyUserById(Long userId);

    UserDto getUserDetails();

}
