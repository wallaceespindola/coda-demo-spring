package com.example.coda.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CODA Record Type 2 - Movement Record (Transaction)
 * Can have multiple detail records (type 3)
 */
@Data
@Builder
public class MovementRecord
{
   // Record 21 - Main movement data
   private int sequenceNumber;
   private String accountNumber;
   private String transactionCode;
   private BigDecimal amount;
   private LocalDate valueDate;
   private String transactionReference;
   private String statementSequence;
   private String communicationStructured;
   private String communicationFree;
   private LocalDate transactionDate;
   private String statementNumber;
   private String globalSequence;
   
   // Record 22 - Counterparty information
   private String counterpartyName;
   private String counterpartyBic;
   private String transactionCategory;
   private String purposeCategory;
   
   // Record 23 - Counterparty account
   private String counterpartyAccount;
   private String counterpartyAccountName;
   
   // Record 31 - Structured communication
   private String structuredCommunication;
   
   // Record 32 - Counterparty address
   private String counterpartyAddress;
   private String counterpartyPostalCode;
   private String counterpartyCity;
}
