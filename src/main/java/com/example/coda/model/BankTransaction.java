package com.example.coda.model;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class BankTransaction {
   LocalDate bookingDate;
   TransactionType type;
   BigDecimal amount;
   String counterpartyName;
   String counterpartyAccount;
   String description;
   String reference;
}
