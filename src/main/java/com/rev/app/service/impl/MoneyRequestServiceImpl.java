package com.rev.app.service.impl;

import com.rev.app.entity.*;
import com.rev.app.repository.IMoneyRequestRepository;
import com.rev.app.service.IMoneyRequestService;
import com.rev.app.service.INotificationService;
import com.rev.app.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ValidationException;
import com.rev.app.exception.ForbiddenOperationException;

@Service
@RequiredArgsConstructor
public class MoneyRequestServiceImpl implements IMoneyRequestService {

    private static final Logger logger = LogManager.getLogger(MoneyRequestServiceImpl.class);

    private final IMoneyRequestRepository moneyRequestRepository;
    private final ITransactionService transactionService;
    private final INotificationService notificationService;

    @Override
    @Transactional
    public MoneyRequest createRequest(User requester, User requestee, BigDecimal amount, String purpose) {
        if (requester.getId().equals(requestee.getId())) {
            throw new ValidationException("Cannot request money from yourself");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be positive");
        }

        MoneyRequest request = MoneyRequest.builder()
                .requester(requester)
                .requestee(requestee)
                .amount(amount)
                .purpose(purpose)
                .status(RequestStatus.PENDING)
                .build();

        request = moneyRequestRepository.save(request);

        notificationService.createNotification(requestee,
                requester.getFullName() + " requested $" + amount + " from you",
                NotificationType.MONEY_REQUEST);

        logger.info("Money request created from {} to {}", requester.getEmail(), requestee.getEmail());
        return request;
    }

    @Override
    @Transactional
    public MoneyRequest acceptRequest(Long requestId, User requestee) {
        MoneyRequest request = moneyRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!request.getRequestee().getId().equals(requestee.getId())) {
            throw new ForbiddenOperationException("Unauthorized to accept this request");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ValidationException("Request is no longer pending");
        }

        transactionService.sendMoney(requestee, request.getRequester().getEmail(),
                request.getAmount(), "Payment for request: " + request.getPurpose());

        request.setStatus(RequestStatus.ACCEPTED);
        request = moneyRequestRepository.save(request);

        notificationService.createNotification(request.getRequester(),
                request.getRequestee().getFullName() + " accepted your money request of $" + request.getAmount(),
                NotificationType.MONEY_REQUEST);

        logger.info("Money request {} accepted", requestId);
        return request;
    }

    @Override
    @Transactional
    public MoneyRequest declineRequest(Long requestId, User requestee) {
        MoneyRequest request = moneyRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!request.getRequestee().getId().equals(requestee.getId())) {
            throw new ForbiddenOperationException("Unauthorized to decline this request");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ValidationException("Request is no longer pending");
        }

        request.setStatus(RequestStatus.DECLINED);
        request = moneyRequestRepository.save(request);

        notificationService.createNotification(request.getRequester(),
                request.getRequestee().getFullName() + " declined your money request of $" + request.getAmount(),
                NotificationType.MONEY_REQUEST);

        logger.info("Money request {} declined", requestId);
        return request;
    }

    @Override
    @Transactional
    public MoneyRequest cancelRequest(Long requestId, User requester) {
        MoneyRequest request = moneyRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!request.getRequester().getId().equals(requester.getId())) {
            throw new ForbiddenOperationException("Unauthorized to cancel this request");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ValidationException("Request is no longer pending");
        }

        request.setStatus(RequestStatus.CANCELLED);
        request = moneyRequestRepository.save(request);

        logger.info("Money request {} cancelled", requestId);
        return request;
    }

    @Override
    public List<MoneyRequest> getIncomingRequests(User user) {
        return moneyRequestRepository.findByRequesteeOrderByCreatedAtDesc(user);
    }

    @Override
    public List<MoneyRequest> getOutgoingRequests(User user) {
        return moneyRequestRepository.findByRequesterOrderByCreatedAtDesc(user);
    }

    @Override
    public List<MoneyRequest> getPendingIncomingRequests(User user) {
        return moneyRequestRepository.findByRequesteeAndStatus(user, RequestStatus.PENDING);
    }

    @Override
    public long getPendingRequestCount(User user) {
        return moneyRequestRepository.countByRequesteeAndStatus(user, RequestStatus.PENDING);
    }
}
