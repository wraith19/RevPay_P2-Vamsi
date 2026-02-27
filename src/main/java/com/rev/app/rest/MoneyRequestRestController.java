package com.rev.app.rest;

import com.rev.app.dto.ApiMessageResponse;
import com.rev.app.dto.CreateMoneyRequestRequest;
import com.rev.app.dto.MoneyRequestResponse;
import com.rev.app.entity.MoneyRequest;
import com.rev.app.entity.User;
import com.rev.app.mapper.MoneyRequestMapper;
import com.rev.app.service.IMoneyRequestService;
import com.rev.app.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/money-requests")
@RequiredArgsConstructor
public class MoneyRequestRestController {

    private final IMoneyRequestService moneyRequestService;
    private final IUserService userService;

    @GetMapping("/incoming")
    public List<MoneyRequestResponse> getIncomingRequests(Principal principal) {
        User user = getAuthenticatedUser(principal);
        return moneyRequestService.getIncomingRequests(user)
                .stream()
                .map(MoneyRequestMapper::toResponse)
                .toList();
    }

    @GetMapping("/outgoing")
    public List<MoneyRequestResponse> getOutgoingRequests(Principal principal) {
        User user = getAuthenticatedUser(principal);
        return moneyRequestService.getOutgoingRequests(user)
                .stream()
                .map(MoneyRequestMapper::toResponse)
                .toList();
    }

    @PostMapping
    public MoneyRequestResponse createRequest(
            Principal principal,
            @Valid @RequestBody CreateMoneyRequestRequest request) {
        User requester = getAuthenticatedUser(principal);
        User requestee = userService.findByEmailOrPhone(request.requesteeIdentifier())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.requesteeIdentifier()));

        MoneyRequest created = moneyRequestService.createRequest(requester, requestee, request.amount(), request.purpose());
        return MoneyRequestMapper.toResponse(created);
    }

    @PatchMapping("/{id}/accept")
    public MoneyRequestResponse acceptRequest(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        return MoneyRequestMapper.toResponse(moneyRequestService.acceptRequest(id, user));
    }

    @PatchMapping("/{id}/decline")
    public MoneyRequestResponse declineRequest(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        return MoneyRequestMapper.toResponse(moneyRequestService.declineRequest(id, user));
    }

    @PatchMapping("/{id}/cancel")
    public ApiMessageResponse cancelRequest(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        moneyRequestService.cancelRequest(id, user);
        return new ApiMessageResponse("Money request cancelled", LocalDateTime.now());
    }

    private User getAuthenticatedUser(Principal principal) {
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}

