package com.example.coda.model;
import lombok.Builder;
import lombok.Data;
/**
 * CODA Record Type 3.2 - Counterparty Address (Information Record - Individual ART Transaction)
 * <p>
 * Each line in the CODA file is exactly 128 characters.
 * <p>
 * <pre>
 * +----------+-------------+------------------------------------------+----------------------------------+
 * | From/To  | Length/Type | Description                              | Content/Mapping                  |
 * +----------+-------------+------------------------------------------+----------------------------------+
 * | 1        | 1 N         | Record identification                    | "3"                              |
 * | 2        | 1 N         | Article code                             | "2"                              |
 * | 3 - 6    | 4 N         | Continuous sequence number               | "0001"                           |
 * | 7 - 10   | 4 N         | Detail number                            | Same than record 31              |
 * | 11 - 115 | 105 AN      | Communication (continued)                | pos 11-45: Sepa.counterpartyNAL2 |
 * |          |             |                                          | truncated to 35                  |
 * |          |             |                                          | pos 46-80: Sepa.counterpartyNAL3 |
 * |          |             |                                          | truncated to 35                  |
 * |          |             |                                          | Caution: not sure about content  |
 * |          |             |                                          | we will see during tests         |
 * | 116 - 125| 10 AN       | Filler                                   | space                            |
 * | 126      | 1 N         | Next code                                | "0", next record is not a        |
 * |          |             |                                          | record 33                        |
 * | 127      | 1 AN        | Filler                                   | space                            |
 * | 128      | 1 N         | Link code with next data record          | "0", next record is not a        |
 * |          |             |                                          | record 31                        |
 * +----------+-------------+------------------------------------------+----------------------------------+
 * </pre>
 * <p>
 * Example:
 * # Information records 3.2 – Individual ART transaction:
 * 3200010002BEKE TUINSTRAT 7                   9950        WALESCHELT                                                          0 0
 */
@Data
@Builder
public class CodaRecord32
{
   private String recordIdentification;      // Pos 1: "3"
   private String articleCode;               // Pos 2: "2"
   private String continuousSequenceNumber;  // Pos 3-6: e.g., "0002"
   private String detailNumber;              // Pos 7-10: "0000"
   private String counterpartyAddress;       // Pos 11-46: 36 chars
   private String counterpartyPostalCode;    // Pos 47-58: 12 chars
   private String counterpartyCity;          // Pos 59-93: 35 chars
   private String filler1;                   // Pos 94-125: 32 chars
   private String nextCode1;                 // Pos 126: "0" no more records
   private String filler2;                   // Pos 127: space
   private String nextCode2;                 // Pos 128: "0"
}
