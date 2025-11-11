package com.example.coda.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CODA Record Type 1 - Old Balance Record
 * <p>
 * Each line in the CODA file is exactly 128 characters.
 * <p>
 * <pre>
 * +----------+-------------+------------------------------------------+--------------------------------+
 * | From/To  | Length/Type | Description                              | Content / Mapping rules        |
 * +----------+-------------+------------------------------------------+--------------------------------+
 * | 1        | 1 N         | Record identification                    | "1"                            |
 * | 2        | 1 N         | Account structure                        | "0"                            |
 * | 3 - 5    | 3 N         | Statement number of account              | There is no statement number   |
 * |          |             |                                          | in TRX KAFKA event. This must  |
 * |          |             |                                          | be discussed with PAN. "123"   |
 * | 6 - 42   | 37 AN       | Account number and currency              | Pos 6: Trx.ibanAccountNumber   |
 * |          |             |                                          | justified left,                |
 * |          |             |                                          | Pos 40-42: Trx.accountCurrency |
 * |          |             |                                          | converted to ISO code          |
 * | 43       | 1 N         | Old balance sign                         | "0"                            |
 * | 44 - 58  | 15 N        | Old balance                              | "000000000000000"              |
 * | 59 - 64  | 6 N         | Old balance date                         | Trx.oldAccountBalanceDate,     |
 * |          |             |                                          | format DDMMYY                  |
 * | 65 - 90  | 26 AN       | Account holder name                      | "BART company"                 |
 * | 91 - 125 | 35 AN       | Account description                      | "Current account"              |
 * | 126 - 128| 3 N         | Statement number of account              | There is no statement number   |
 * |          |             |                                          | in TRX KAFKA event. This must  |
 * |          |             |                                          | be discussed with PAN.         |
 * +----------+-------------+------------------------------------------+--------------------------------+
 * </pre>
 */
@Data
@Builder
public class CodaOldBalanceRecord
{
   private String recordIdentification;      // Pos 1: "1"
   private String accountStructure;          // Pos 2: "0"
   private String statementNumber;           // Pos 3-5: "123"
   private String accountNumber;             // Pos 6-42: 37 chars (includes currency)
   private String oldBalanceSign;            // Pos 43: "0"
   private BigDecimal oldBalance;            // Pos 44-58: 15 digits
   private LocalDate balanceDate;            // Pos 59-64: DDMMYY
   private String accountHolderName;         // Pos 65-90: 26 chars
   private String accountDescription;        // Pos 91-125: 35 chars
   private String statementNumberDetail;     // Pos 126-128: 3 digits
}
