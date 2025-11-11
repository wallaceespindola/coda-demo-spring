package com.example.coda.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

/**
 * CODA Record Type 0 - Header Record
 * <p>
 * Each line in the CODA file is exactly 128 characters.
 * <p>
 * <pre>
 * +----------+-------------+------------------------------------------+--------------------------------+
 * | From/To  | Length/Type | Description                              | Content / Mapping rules        |
 * +----------+-------------+------------------------------------------+--------------------------------+
 * | 1        | 1 N         | Record identification                    | "0"                            |
 * | 2 - 5    | 4 N         | Zeros                                    | "0000"                         |
 * | 6 - 11   | 6 N         | Creation date                            | System date, format DDMMYY     |
 * | 12 - 14  | 3 N         | Bank identification number or zeros      | "300"                          |
 * | 15 - 16  | 2 N         | Application code                         | "05"                           |
 * | 17       | 1 AN        | Duplicate code                           | spaces                         |
 * | 18 - 24  | 7 AN        | Filler                                   | spaces                         |
 * | 25 - 34  | 10 AN       | File reference of the bank               | "0123456789"                   |
 * | 35 - 60  | 26 AN       | Name addressee                           | "BART company"                 |
 * | 61 - 71  | 11 AN       | BIC of the account holding bank          | "BBRUBEBB"                     |
 * | 72 - 82  | 11 N        | Identification number of the Belgium-    | "00000000097"                  |
 * |          |             | based account holder (VAT)               |                                |
 * | 83       | 1 AN        | Filler                                   | space                          |
 * | 84 - 88  | 5 N         | Code separate application                | "99991" for ART                |
 * | 89 - 104 | 16 AN       | Transaction reference                    | space                          |
 * | 105 - 120| 16 AN       | Related reference                        | space                          |
 * | 121 - 127| 7 AN        | Filler                                   | space                          |
 * | 128      | 1 N         | Version code                             | "2"                            |
 * +----------+-------------+------------------------------------------+--------------------------------+
 * </pre>
 */
@Data
@Builder
public class CodaHeaderRecord
{
   private String recordIdentification;      // Pos 1: "0"
   private String zeros;                     // Pos 2-5: "0000"
   private LocalDate creationDate;           // Pos 6-11: DDMMYY
   private String bankIdentificationNumber;  // Pos 12-14: "300"
   private String applicationCode;           // Pos 15-16: "05"
   private String duplicateCode;             // Pos 17: space
   private String filler1;                   // Pos 18-24: 7 spaces
   private String fileReference;             // Pos 25-34: 10 chars
   private String nameAddressee;             // Pos 35-60: 26 chars
   private String bic;                       // Pos 61-71: 11 chars
   private String vatNumber;                 // Pos 72-82: 11 chars
   private String filler2;                   // Pos 83: space
   private String codeSeparateApplication;   // Pos 84-88: "99991" for ART
   private String transactionReference;      // Pos 89-104: 16 spaces
   private String relatedReference;          // Pos 105-120: 16 spaces
   private String filler3;                   // Pos 121-127: 7 spaces
   private String versionCode;               // Pos 128: "2"
}
