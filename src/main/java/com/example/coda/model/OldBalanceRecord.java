package com.example.coda.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CODA Record Type 1 - Old Balance Record
 * Format: 1 + sequence + account number + old balance + date + account holder name + description
 */
@Data
@Builder
public class OldBalanceRecord
{
   private int sequenceNumber;
   private String accountNumber;
   private String accountNumberType;
   private String currencyCode;
   private String countryCode;
   private BigDecimal oldBalance;
   private LocalDate balanceDate;
   private String accountHolderName;
   private String accountDescription;
   private int sequenceNumberDetail;
}
