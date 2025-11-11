package com.example.coda.model;
import lombok.Builder;
import lombok.Data;
/**
 * CODA Record Type 3.1 - Structured Communication (Information Record - Individual ART Transaction)
 * <p>
 * Each line in the CODA file is exactly 128 characters.
 * <p>
 * <pre>
 * +----------+-------------+------------------------------------------+---------------------------------+
 * | From/To  | Length/Type | Description                              | Content/Mapping                 |
 * +----------+-------------+------------------------------------------+---------------------------------+
 * | 1        | 1 N         | Record identification                    | "3"                             |
 * | 2        | 1 N         | Article code                             | "1"                             |
 * | 3 - 6    | 4 N         | Continuous sequence number               | "0001"                          |
 * | 7 - 10   | 4 N         | Detail number                            | Record 23 + 1                   |
 * | 11 - 31  | 21 AN       | Reference number of the bank             | This reference must be the      |
 * |          |             |                                          | same than the one for the       |
 * |          |             |                                          | global booking. So              |
 * |          |             |                                          | "123456789012300123"            |
 * | 32 - 39  | 8 N         | Transaction code                         | "60150000",                     |
 * | 40       | 1 N         | Communication structure code             | "1" Structured CODA             |
 * |          |             |                                          | communication 001 used for      |
 * |          |             |                                          | counterparty                    |
 * | 41 - 113 | 73 AN       | Communication                            | "001" + Sepa.counterpartyNA1    |
 * | 114 - 125| 12 AN       | Filler                                   | spaces                          |
 * | 126      | 1 N         | Next code                                | "1", next record is a record 32 |
 * | 127      | 1 AN        | Filler                                   | space                           |
 * | 128      | 1 N         | Link code with next data record          | "0", next record is not a       |
 * |          |             |                                          | record 31                       |
 * +----------+-------------+------------------------------------------+---------------------------------+
 * </pre>
 * <p>
 * Example:
 * # Information records 3.1 – Individual ART transaction:
 * 31000100023010383003291000028  601500001001UCAR                                                                              1 0
 */
@Data
@Builder
public class CodaRecord31
{
   private String recordIdentification;      // Pos 1: "3"
   private String articleCode;               // Pos 2: "1"
   private String continuousSequenceNumber;  // Pos 3-6: e.g., "0002"
   private String detailNumber;              // Pos 7-10: "0000"
   private String referenceNumber;           // Pos 11-31: 21 chars
   private String transactionCode;           // Pos 32-39: 8 chars
   private String structuredCommunication;   // Pos 40-112: 73 chars
   private String filler1;                   // Pos 113-125: 13 chars
   private String nextCode1;                 // Pos 126: "1" record 32 follows
   private String filler2;                   // Pos 127: space
   private String nextCode2;                 // Pos 128: "0" no more records
}
