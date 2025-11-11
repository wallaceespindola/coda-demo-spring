package com.example.coda.model;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
/**
 * CODA Record Type 2.1 - Transaction Main Data (Individual ART Transaction)
 * <p>
 * Each line in the CODA file is exactly 128 characters.
 * <p>
 * <pre>
 * +----------+-------------+------------------------------------------+--------------------------------+
 * | From/To  | Length/Type | Description                              | Content / Mapping rules        |
 * +----------+-------------+------------------------------------------+--------------------------------+
 * | 1        | 1 N         | Record identification                    | "2"                            |
 * | 2        | 1 N         | Article code                             | "1"                            |
 * | 3 - 6    | 4 N         | Continuous sequence number               | "0001" only one global         |
 * |          |             |                                          | transaction                    |
 * | 7 - 10   | 4 N         | Detail number                            | First record detail 21 is      |
 * |          |             |                                          | "0001", after record 32 + 1    |
 * | 11 - 31  | 21 AN       | Reference number of the financial        | This reference must be the     |
 * |          |             | institution                              | same than the one for the      |
 * |          |             |                                          | global booking. So             |
 * |          |             |                                          | "123456789012300123"           |
 * | 32       | 1 N         | Movement sign                            | "0" always credit amount       |
 * | 33 - 47  | 15 N        | Amount                                   | In KAFKA event, an amount is   |
 * |          |             |                                          | on 20 digits, last 4 are       |
 * |          |             |                                          | decimal part. In CODA, it is   |
 * |          |             |                                          | on 15 digits, last 3 are       |
 * |          |             |                                          | decimal part. Drop 4th digit.  |
 * |          |             |                                          | Trx.transactionAmount          |
 * | 48 - 53  | 6 N         | Value date                               | Trx.transactionValueDate,      |
 * |          |             |                                          | format DDMMYY                  |
 * | 54 - 61  | 8 N         | Transaction code                         | "60150000", i.e. 6 stands for  |
 * |          |             |                                          | a detail of a totalized        |
 * |          |             |                                          | amount done by the bank, 01    |
 * |          |             |                                          | stands for SEPA Credit         |
 * |          |             |                                          | Transfer and 50 stands for     |
 * |          |             |                                          | credit                         |
 * | 62       | 1 N         | Communication type, always free text     | "1" for structured CODA        |
 * |          |             |                                          | communication                  |
 * | 63 - 115 | 53 AN       | Communication zone                       | POC is only.absolut.coda,      |
 * |          |             |                                          | so structured CODA             |
 * |          |             |                                          | communication 101              |
 * |          |             |                                          | "101" + the 12 digits VCS      |
 * | 116 - 121| 6 N         | Entry date                               | Trx.operationPeriod, format    |
 * |          |             |                                          | DDMMYY                         |
 * | 122 - 124| 3 N         | Statement number of account              | There is no statement number   |
 * |          |             |                                          | in TRX KAFKA event. This must  |
 * |          |             |                                          | be discussed with PAN.         |
 * |          |             |                                          | "123"                          |
 * | 125      | 1 N         | Globalisation code                       | "0" it is a detail of a        |
 * |          |             |                                          | global amount                  |
 * | 126      | 1 N         | Next code                                | "1", record 22 follows         |
 * | 127      | 1 AN        | Filler                                   | space                          |
 * | 128      | 1 N         | Link code with next data record          | "0", no record 31 follows      |
 * +----------+-------------+------------------------------------------+--------------------------------+
 * </pre>
 * <p>
 * Example:
 * # Data record 2.1 – Individual ART transaction:
 * 21000100013010383003291000028  0000000000072480030325601500001102141359004019                                      03032502401 0
 */
@Data
@Builder
public class CodaRecord21
{
   private String recordIdentification;      // Pos 1: "2"
   private String articleCode;               // Pos 2: "1"
   private String continuousSequenceNumber;  // Pos 3-6: e.g., "0001"
   private String detailNumber;              // Pos 7-10: "0000"
   private String referenceNumber;           // Pos 11-31: 21 chars
   private String movementSign;              // Pos 32: "0" credit, "1" debit
   private BigDecimal amount;                // Pos 33-47: 15 digits
   private LocalDate valueDate;              // Pos 48-53: DDMMYY
   private String transactionCode;           // Pos 54-61: 8 chars
   private String communicationType;         // Pos 62: "0" free text, "1" structured
   private String communicationZone;         // Pos 63-115: 53 chars
   private LocalDate entryDate;              // Pos 116-121: DDMMYY
   private String statementNumber;           // Pos 122-124: 3 digits
   private String globalisationCode;         // Pos 125: "0" individual transaction
   private String nextCode;                  // Pos 126: "0" or "1"
   private String filler;                    // Pos 127: space
   private String linkCode;                  // Pos 128: "0" or "1"
}
