package buy01.user_service.controller;

import buy01.user_service.dto.UserVerificationResponse;
import buy01.user_service.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}/exists")
    public ResponseEntity<UserVerificationResponse> checkUserAndRole(@PathVariable String id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(new UserVerificationResponse(true, user.getRole().name())))
                .orElseGet(() -> ResponseEntity.ok(new UserVerificationResponse(false, null)));
    }
}