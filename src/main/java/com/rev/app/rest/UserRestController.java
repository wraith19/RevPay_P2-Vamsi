package com.rev.app.rest;

import com.rev.app.dto.UserMeResponse;
import com.rev.app.entity.User;
import com.rev.app.mapper.UserMapper;
import com.rev.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.rev.app.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final IUserService userService;

    @GetMapping("/me")
    public UserMeResponse getCurrentUser(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        return UserMapper.toMeResponse(user);
    }
}

