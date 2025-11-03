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
   private HeaderRecord header;
   private OldBalanceRecord oldBalance;
   private List<MovementRecord> movements;
   private NewBalanceRecord newBalance;
   private TrailerRecord trailer;
}
