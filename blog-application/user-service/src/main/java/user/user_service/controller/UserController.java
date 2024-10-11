package user.user_service.controller;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import user.user_service.dtos.AuthenticationDto;
import user.user_service.dtos.UserDto;
import user.user_service.services.UserService;

@RestController
@RequestMapping(path = "/api/users", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private static final Logger logger = LogManager.getLogger(UserController.class);


    @PostMapping("/register")
    public ResponseEntity<AuthenticationDto> register(
            @RequestBody AuthenticationDto registerRequest) {

        AuthenticationDto authenticationResponse = userService.register(registerRequest);
        logger.info("User with username {} has been registered successfully", registerRequest.getUsername());
        return new ResponseEntity<>(authenticationResponse, HttpStatus.CREATED);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationDto> authenticate(
            @RequestBody AuthenticationDto authenticationRequest) {
        AuthenticationDto authenticationResponse = userService.authenticate(authenticationRequest);
        return new ResponseEntity<>(authenticationResponse, HttpStatus.OK);
    }
    @GetMapping("/getUserDetails")
    public ResponseEntity<UserDto> getUserDetails(){
        UserDto user = userService.getUserDetails();
        return new ResponseEntity<>(user,HttpStatus.OK);
    }
}
