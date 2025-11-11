package com.example.coda.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Complete CODA statement representation
 */
@Data
@Builder
public class CodaStatement
{
   private CodaHeaderRecord header; // Record Type 0 - Header Record
   private CodaOldBalanceRecord oldBalance; // Record Type 1 - Old Balance Record
   private CodaGlobalRecord global; // Record Type 2.1 - Global amount of all VCS
   private List<CodaIndividualTransactionRecord> individualTransactions; // Individual Transaction Records - Aggregates all related records (2.1, 2.2, 2.3, 3.1, 3.2)
   private CodaNewBalanceRecord newBalance; // Record Type 8 - New Balance Record
   private CodaTrailerRecord trailer; // Record Type 9 - Trailer Record
}
