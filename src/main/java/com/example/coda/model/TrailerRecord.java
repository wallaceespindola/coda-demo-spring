package com.example.coda.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * CODA Record Type 9 - Trailer Record
 * Format: 9 + totals and counts
 */
@Data
@Builder
public class TrailerRecord
{
   private int sequenceNumber;
   private int numberOfRecords;
   private BigDecimal totalDebit;
   private BigDecimal totalCredit;
}
