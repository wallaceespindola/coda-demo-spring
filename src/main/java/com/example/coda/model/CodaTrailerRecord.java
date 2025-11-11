package com.example.coda.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * CODA Record Type 9 - Trailer Record
 * <p>
 * Each line in the CODA file is exactly 128 characters.
 * <p>
 * <pre>
 * +----------+-------------+------------------------------------------+--------------------------------+
 * | From/To  | Length/Type | Description                              | Content / Mapping rules        |
 * +----------+-------------+------------------------------------------+--------------------------------+
 * | 1        | 1 N         | Record identification                    | "9"                            |
 * | 2 - 16   | 15 AN       | Filler                                   | spaces                         |
 * | 17 - 22  | 6 N         | Number of records in file                | Total records including header |
 * |          |             |                                          | and trailer                    |
 * | 23 - 37  | 15 N        | Total debit amount                       | Sum of all debit transactions  |
 * |          |             |                                          | in cents (3 decimals)          |
 * | 38 - 52  | 15 N        | Total credit amount                      | Sum of all credit transactions |
 * |          |             |                                          | in cents (3 decimals)          |
 * | 53 - 127 | 75 AN       | Filler                                   | spaces                         |
 * | 128      | 1 N         | Trailer marker                           | "1"                            |
 * +----------+-------------+------------------------------------------+--------------------------------+
 * </pre>
 */
@Data
@Builder
public class CodaTrailerRecord
{
   private String recordIdentification;      // Pos 1: "9"
   private String filler1;                   // Pos 2-16: 15 spaces
   private int numberOfRecords;              // Pos 17-22: 6 digits
   private BigDecimal totalDebit;            // Pos 23-37: 15 digits
   private BigDecimal totalCredit;           // Pos 38-52: 15 digits
   private String filler2;                   // Pos 53-127: 75 spaces
   private String trailerMarker;             // Pos 128: "1"
}
