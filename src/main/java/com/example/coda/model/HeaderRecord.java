package com.example.coda.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

/**
 * CODA Record Type 0 - Header Record
 * Format: 0 + sequence + version + creation date + bank identification + application code + etc.
 */
@Data
@Builder
public class HeaderRecord
{
   private int sequenceNumber;
   private String versionCode;
   private LocalDate creationDate;
   private String bankIdentificationNumber;
   private String applicationCode;
   private String recipientName;
   private String bic;
   private String accountNumber;
   private String accountDescription;
   private String oldBalanceSign;
   private String duplicateCode;
}
