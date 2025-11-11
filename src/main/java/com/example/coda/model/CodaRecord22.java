package com.example.coda.model;
import lombok.Builder;
import lombok.Data;
/**
 * CODA Record Type 2.2 - Communication (Individual ART Transaction)
 * <p>
 * Each line in the CODA file is exactly 128 characters.
 * <p>
 * <pre>
 * +----------+-------------+------------------------------------------+--------------------------------+
 * | From/To  | Length/Type | Description                              | Content / Mapping rules        |
 * +----------+-------------+------------------------------------------+--------------------------------+
 * | 1        | 1 N         | Record identification                    | "2"                            |
 * | 2        | 1 N         | Article code                             | "2"                            |
 * | 3 - 6    | 4 N         | Continuous sequence number               | "0001"                         |
 * | 7 - 10   | 4 N         | Detail number                            | Same than record 21            |
 * | 11 - 63  | 53 AN       | Communication (continued)                | space                          |
 * | 64 - 98  | 35 AN       | Customer name or blanks                  | "NOT PROVIDED"                 |
 * | 99 - 109 | 11 AN       | BIC of the counterparty's bank           | Sepa.counterpartyBIC           |
 * | 110 - 112| 3 AN        | Filler                                   | space                          |
 * | 113      | 1 AN        | Type of R-Transaction or blank           | space                          |
 * | 114 - 117| 4 AN        | ISO Reason return code or blanks         | space                          |
 * | 118 - 121| 4 AN        | Category purpose                         | space                          |
 * | 122 - 125| 4 AN        | Purpose                                  | space                          |
 * | 126      | 1 N         | Next code                                | "1", record 23 follows         |
 * | 127      | 1 AN        | Filler                                   | space                          |
 * | 128      | 1 N         | Link code with next data record          | "0", no record 31 follows      |
 * +----------+-------------+------------------------------------------+--------------------------------+
 * </pre>
 * <p>
 * Example:
 * # Data record 2.2 – Individual ART transaction:
 * 2200010001                                                     NOTPROVIDED                        GKCCBEBB                   1 0
 */
@Data
@Builder
public class CodaRecord22
{
   private String recordIdentification;      // Pos 1: "2"
   private String articleCode;               // Pos 2: "2"
   private String continuousSequenceNumber;  // Pos 3-6: e.g., "0001"
   private String detailNumber;              // Pos 7-10: "0000"
   private String clientReference;           // Pos 11-63: 53 chars
   private String counterpartyName;          // Pos 64-90: 27 chars
   private String counterpartyBic;           // Pos 91-101: 11 chars
   private String filler1;                   // Pos 102-125: 24 chars
   private String transactionCategory;       // Pos 126: category code
   private String filler2;                   // Pos 127: space
   private String nextCode;                  // Pos 128: "0" no record 23 follows
}
