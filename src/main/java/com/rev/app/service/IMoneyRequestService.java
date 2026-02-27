package com.rev.app.service;

import com.rev.app.entity.MoneyRequest;
import com.rev.app.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface IMoneyRequestService {
    MoneyRequest createRequest(User requester, User requestee, BigDecimal amount, String purpose);

    MoneyRequest acceptRequest(Long requestId, User requestee);

    MoneyRequest declineRequest(Long requestId, User requestee);

    MoneyRequest cancelRequest(Long requestId, User requester);

    List<MoneyRequest> getIncomingRequests(User user);

    List<MoneyRequest> getOutgoingRequests(User user);

    List<MoneyRequest> getPendingIncomingRequests(User user);

    long getPendingRequestCount(User user);
}
