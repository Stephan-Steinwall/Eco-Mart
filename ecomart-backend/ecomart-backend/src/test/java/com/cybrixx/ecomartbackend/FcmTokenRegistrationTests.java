package com.cybrixx.ecomartbackend;

import com.cybrixx.ecomartbackend.controller.UserController;
import com.cybrixx.ecomartbackend.dto.FcmTokenDTO;
import com.cybrixx.ecomartbackend.entity.User;
import com.cybrixx.ecomartbackend.repository.UserRepository;
import com.cybrixx.ecomartbackend.service.FcmService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Registering a device token: dead tokens are refused (410) so the app can fetch a fresh one. */
class FcmTokenRegistrationTests {

    private final UserRepository users = mock(UserRepository.class);
    private final FcmService fcm = mock(FcmService.class);
    private final UserController controller = new UserController(users, fcm);
    private final Principal caller = () -> "customer@example.com";

    private FcmTokenDTO token(String value) {
        FcmTokenDTO dto = new FcmTokenDTO();
        dto.setToken(value);
        return dto;
    }

    private User customer() {
        User user = User.builder().id(4L).email("customer@example.com").password("x").build();
        when(users.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void aTokenFirebaseHasForgottenIsRefusedWith410AndNotStored() {
        when(fcm.isTokenKnownInvalid("dead")).thenReturn(true);

        ResponseEntity<?> response = controller.registerFcmToken(caller, token("dead"));

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        verify(users, never()).save(any());
        verify(users, never()).clearTokenFromOtherUsers(any(), any());
    }

    @Test
    void aValidTokenIsStoredAndTakenOffAnyOtherAccount() {
        User user = customer();
        when(fcm.isTokenKnownInvalid("good")).thenReturn(false);

        ResponseEntity<?> response = controller.registerFcmToken(caller, token("good"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("good", user.getFcmToken());
        verify(users).clearTokenFromOtherUsers("good", 4L);
        verify(users).save(user);
    }

    @Test
    void aBlankTokenIsRejectedWithoutBotheringFirebase() {
        ResponseEntity<?> response = controller.registerFcmToken(caller, token("  "));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(fcm);
        verify(users, never()).save(any());
    }

    @Test
    void whenFirebaseIsOffAnyTokenIsAcceptedBecauseItCannotBeChecked() {
        // A real FcmService that never initialised: isTokenKnownInvalid() gives it the benefit of the doubt.
        UserController offline = new UserController(users, new FcmService(""));
        User user = customer();

        ResponseEntity<?> response = offline.registerFcmToken(caller, token("anything"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("anything", user.getFcmToken());
    }
}
