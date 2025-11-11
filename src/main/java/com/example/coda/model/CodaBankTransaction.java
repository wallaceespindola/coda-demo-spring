package com.example.coda.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record CodaBankTransaction(LocalDate bookingDate, TransactionType type, BigDecimal amount, String counterpartyName,
                                  String counterpartyAccount, String description, String reference)
{
}
