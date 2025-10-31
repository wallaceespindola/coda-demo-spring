package com.example.coda.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CODA Record Type 8 - New Balance Record
 * Format: 8 + sequence + account number + new balance + date
 */
@Data
@Builder
public class NewBalanceRecord
{
   private int sequenceNumber;
   private String accountNumber;
   private String accountNumberType;
   private String currencyCode;
   private String countryCode;
   private BigDecimal newBalance;
   private LocalDate balanceDate;
}
