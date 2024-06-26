package ai.cvbird.cvbirdsite.controller;

import ai.cvbird.cvbirdsite.dto.StringResponse;
import ai.cvbird.cvbirdsite.dto.UserConverter;
import ai.cvbird.cvbirdsite.dto.UserDto;
import ai.cvbird.cvbirdsite.model.User;
import ai.cvbird.cvbirdsite.registration.OnRegistrationCompleteEvent;
import ai.cvbird.cvbirdsite.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    UserService userService;
    PasswordEncoder passwordEncoder;
    ApplicationEventPublisher eventPublisher;
    UserConverter userConverter;
    @Autowired
    UserController(UserService userService, PasswordEncoder passwordEncoder,
                   ApplicationEventPublisher eventPublisher, UserConverter userConverter) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.userConverter=userConverter;
    }

    @Operation(summary = "Get current WEB user")
    @GetMapping("/user_info")
    public ResponseEntity<StringResponse> getUserInfo(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }

        StringResponse stringResponse = new StringResponse(username);
        return ResponseEntity.ok(stringResponse);
    }

    //@PostMapping(value = "/registration", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<UserDto> userRegistration(@Valid UserDto userDto, final HttpServletRequest request) {
        User user = userService.registerNewUserAccount(userConverter.fromUserDTO(userDto));
        String appUrl = request.getContextPath();
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale(), appUrl));
        return ResponseEntity.ok(userConverter.fromUser(user));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

}
