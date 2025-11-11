package com.example.coda.model;
import lombok.Builder;
import lombok.Data;
/**
 * CODA Record Type 2.3 - Counterparty Account (Individual ART Transaction)
 * <p>
 * Each line in the CODA file is exactly 128 characters.
 * <p>
 * <pre>
 * +----------+-------------+------------------------------------------+-----------------------------------+
 * | From/To  | Length/Type | Description                              | Content / Mapping                 |
 * +----------+-------------+------------------------------------------+-----------------------------------+
 * | 1        | 1 N         | Record identification                    | "2"                               |
 * | 2        | 1 N         | Article code                             | "3"                               |
 * | 3 - 6    | 4 N         | Continuous sequence number               | "0001"                            |
 * | 7 - 10   | 4 N         | Detail number                            | Same than record 22               |
 * | 11 - 47  | 37 AN       | Counterparty's account number and        | Trx.counterpartyIbanAccountNumber |
 * |          |             | currency (or spaces)                     |                                   |
 * | 48 - 82  | 35 AN       | Counterparty's name                      | Sepa.counterpartyNA1, truncated   |
 * |          |             |                                          | to 35                             |
 * |          |             |                                          | Caution: not sure about content   |
 * |          |             |                                          | we will see during tests          |
 * | 83 - 125 | 43 AN       | Communication (continued)                | spaces                            |
 * | 126      | 1 N         | Next code                                | "0"                               |
 * | 127      | 1 AN        | Filler                                   | space                             |
 * | 128      | 1 N         | Link code with next data record          | "1" if a record 31 follows        |
 * +----------+-------------+------------------------------------------+-----------------------------------+
 * </pre>
 * <p>
 * Example:
 * # Data record 2.3 – Individual ART transaction:
 * 2300010001BE84390060159859                     UCAR                                                                          0 1
 */
@Data
@Builder
public class CodaRecord23
{
   private String recordIdentification;      // Pos 1: "2"
   private String articleCode;               // Pos 2: "3"
   private String continuousSequenceNumber;  // Pos 3-6: e.g., "0001"
   private String detailNumber;              // Pos 7-10: "0000"
   private String counterpartyAccount;       // Pos 11-47: 37 chars (IBAN)
   private String counterpartyAccountName;   // Pos 48-82: 35 chars
   private String filler1;                   // Pos 83-125: 43 chars
   private String purposeCategory;           // Pos 126: purpose code
   private String filler2;                   // Pos 127: space
   private String nextCode;                  // Pos 128: "1" record 31 follows
}
