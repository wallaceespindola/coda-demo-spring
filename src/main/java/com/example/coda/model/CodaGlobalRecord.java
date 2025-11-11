package com.example.coda.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

/**
 * CODA Record Type 2.1 - Global amount of all VCS
 * <p>
 * Each line in the CODA file is exactly 128 characters.
 * <p>
 * <pre>
 * +----------+-------------+------------------------------------------+----------------------------------+
 * | From/To  | Length/Type | Description                              | Content / Mapping rules          |
 * +----------+-------------+------------------------------------------+----------------------------------+
 * | 1        | 1 N         | Record identification                    | "2"                              |
 * | 2        | 1 N         | Article code                             | "1"                              |
 * | 3 - 6    | 4 N         | Continuous sequence number               | "0001" only one global           |
 * |          |             |                                          | transaction                      |
 * | 7 - 10   | 4 N         | Detail number                            | "0000"                           |
 * | 11 - 31  | 21 AN       | Reference number of the financial        | In KOC, the global amount is     |
 * |          |             | institution                              | booked, so there is a            |
 * |          |             |                                          | reference. In BART, there is     |
 * |          |             |                                          | no global booking, so we need    |
 * |          |             |                                          | to create in BART this           |
 * |          |             |                                          | reference                        |
 * |          |             |                                          | "123456789012300123"             |
 * | 32       | 1 N         | Movement sign                            | "0" always credit amount         |
 * | 33 - 47  | 15 N        | Amount                                   | In KAFKA event, an amount is     |
 * |          |             |                                          | on 20 digits, last 4 are         |
 * |          |             |                                          | decimal part. In CODA, it is     |
 * |          |             |                                          | on 15 digits, last 3 are         |
 * |          |             |                                          | decimal part. Drop 4th digit.    |
 * |          |             |                                          | Sum of all Trx.transactionAmount |
 * | 48 - 53  | 6 N         | Value date                               | In KOC, the global amount is     |
 * |          |             |                                          | booked, so there is a value      |
 * |          |             |                                          | date. In BART, there is no       |
 * |          |             |                                          | global booking, so we use the    |
 * |          |             |                                          | booking date of the first ART    |
 * |          |             |                                          | transaction                      |
 * |          |             |                                          | Trx.transactionValueDate,        |
 * |          |             |                                          | format DDMMYY                    |
 * | 54 - 61  | 8 N         | Transaction code                         | "20150000", i.e. 2 stands for    |
 * |          |             |                                          | totalized amount done by the     |
 * |          |             |                                          | bank, 01 stands for SEPA         |
 * |          |             |                                          | Credit Transfer and 50 stands    |
 * |          |             |                                          | for credit                       |
 * | 62       | 1 N         | Communication type, always free text     | "0"                              |
 * | 63 - 115 | 53 AN       | Communication zone                       | "GROUPING OF x VCS", x being     |
 * |          |             |                                          | the number of ART transactions   |
 * | 116 - 121| 6 N         | Entry date                               | In KOC, the global amount is     |
 * |          |             |                                          | booked, so there is a booking    |
 * |          |             |                                          | date. In BART, there is no       |
 * |          |             |                                          | global booking, so we use the    |
 * |          |             |                                          | booking date of the first ART    |
 * |          |             |                                          | transaction                      |
 * |          |             |                                          | Trx.operationPeriod, format      |
 * |          |             |                                          | DDMMYY                           |
 * | 122 - 124| 3 N         | Statement number of account              | There is no statement number     |
 * |          |             |                                          | in TRX KAFKA event. This must    |
 * |          |             |                                          | be discussed with PAN.           |
 * |          |             |                                          | "123"                            |
 * | 125      | 1 N         | Globalisation code                       | "1" it is a global amount        |
 * | 126      | 1 N         | Next code                                | "0", no record 22 or 23 follows  |
 * | 127      | 1 AN        | Filler                                   | space                            |
 * | 128      | 1 N         | Link code with next data record          | "0", no record 31 follows        |
 * +----------+-------------+------------------------------------------+----------------------------------+
 * </pre>
 */
@Data
@Builder
public class CodaGlobalRecord
{
   private String recordIdentification;      // Pos 1: "2"
   private String articleCode;               // Pos 2: "1"
   private String continuousSequenceNumber;  // Pos 3-6: "0001"
   private String detailNumber;              // Pos 7-10: "0000"
   private String referenceNumber;           // Pos 11-31: 21 chars
   private String movementSign;              // Pos 32: "0"
   private BigDecimal amount;                // Pos 33-47: 15 digits
   private LocalDate valueDate;              // Pos 48-53: DDMMYY
   private String transactionCode;           // Pos 54-61: 8 chars
   private String communicationType;         // Pos 62: "0"
   private String communicationZone;         // Pos 63-115: 53 chars
   private LocalDate entryDate;              // Pos 116-121: DDMMYY
   private String statementNumber;           // Pos 122-124: "123"
   private String globalisationCode;         // Pos 125: "1"
   private String nextCode;                  // Pos 126: "0"
   private String filler;                    // Pos 127: space
   private String linkCode;                  // Pos 128: "0"
}
