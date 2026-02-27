package com.rev.app.mapper;

import com.rev.app.dto.TransactionResponse;
import com.rev.app.entity.Transaction;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionId(),
                transaction.getSender() != null ? transaction.getSender().getId() : null,
                transaction.getReceiver() != null ? transaction.getReceiver().getId() : null,
                transaction.getAmount(),
                transaction.getType() != null ? transaction.getType().name() : null,
                transaction.getStatus() != null ? transaction.getStatus().name() : null,
                transaction.getNote(),
                transaction.getTimestamp());
    }
}
