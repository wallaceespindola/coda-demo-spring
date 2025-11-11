package com.example.coda.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CODA Record Type 8 - New Balance Record
 * <p>
 * Each line in the CODA file is exactly 128 characters.
 * <p>
 * <pre>
 * +----------+-------------+------------------------------------------+--------------------------------+
 * | From/To  | Length/Type | Description                              | Content / Mapping rules        |
 * +----------+-------------+------------------------------------------+--------------------------------+
 * | 1        | 1 N         | Record identification                    | "8"                            |
 * | 2        | 1 N         | Account structure                        | "0"                            |
 * | 3 - 5    | 3 N         | Statement number of account              | There is no statement number   |
 * |          |             |                                          | in TRX KAFKA event. This must  |
 * |          |             |                                          | be discussed with PAN. "024"   |
 * | 6 - 42   | 37 AN       | Account number and currency              | Pos 6: Trx.ibanAccountNumber   |
 * |          |             |                                          | justified left,                |
 * |          |             |                                          | Pos 40-42: Trx.accountCurrency |
 * |          |             |                                          | converted to ISO code          |
 * | 43       | 1 N         | New balance sign                         | "0" for positive, "1" negative |
 * | 44 - 58  | 15 N        | New balance                              | Amount in cents (3 decimals)   |
 * | 59 - 64  | 6 N         | New balance date                         | Trx.newAccountBalanceDate,     |
 * |          |             |                                          | format DDMMYY                  |
 * | 65 - 128 | 64 AN       | Filler                                   | spaces, last char may be "0"   |
 * +----------+-------------+------------------------------------------+--------------------------------+
 * </pre>
 */
@Data
@Builder
public class CodaNewBalanceRecord
{
   private String recordIdentification;      // Pos 1: "8"
   private String accountStructure;          // Pos 2: "0"
   private String statementNumber;           // Pos 3-5: "024"
   private String accountNumber;             // Pos 6-42: 37 chars (includes currency)
   private String newBalanceSign;            // Pos 43: "0" or "1"
   private BigDecimal newBalance;            // Pos 44-58: 15 digits
   private LocalDate balanceDate;            // Pos 59-64: DDMMYY
   private String filler;                    // Pos 65-128: 64 chars
}
