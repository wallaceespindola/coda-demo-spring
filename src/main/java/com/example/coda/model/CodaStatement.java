package com.example.coda.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * Complete CODA statement representation
 */
@Data
@Builder
public class CodaStatement
{
   private HeaderRecord header;
   private OldBalanceRecord oldBalance;
   private List<MovementRecord> movements;
   private NewBalanceRecord newBalance;
   private TrailerRecord trailer;
}
