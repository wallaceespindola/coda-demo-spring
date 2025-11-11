package com.example.coda.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CODA Individual Transaction Record - Aggregates all related records (2.1, 2.2, 2.3, 3.1, 3.2)
 * <p>
 * An individual ART transaction consists of:
 * - Record 2.1: Transaction main data (mandatory)
 * - Record 2.2: Communication (optional)
 * - Record 2.3: Counterparty account (optional)
 * - Record 3.1: Structured communication (optional)
 * - Record 3.2: Counterparty address (optional)
 * <p>
 * All transaction data is accessed through the specific record objects.
 * Each record object contains the complete field structure as defined in the CODA specification.
 * <p>
 * Examples of the individual ART transaction records:
 * <p>
 * # Data record 2.1 – Individual ART transaction:
 * 21000100013010383003291000028  0000000000072480030325601500001102141359004019                                      03032502401 0
 * <p>
 * # Data record 2.2 – Individual ART transaction:
 * 2200010001                                                     NOTPROVIDED                        GKCCBEBB                   1 0
 * <p>
 * # Data record 2.3 – Individual ART transaction:
 * 2300010001BE84390060159859                     UCAR                                                                          0 1
 * <p>
 * # Information records 3.1 – Individual ART transaction:
 * 31000100023010383003291000028  601500001001UCAR                                                                              1 0
 * <p>
 * # Information records 3.2 – Individual ART transaction:
 * 3200010002BEKE TUINSTRAT 7                   9950        WALESCHELT                                                          0 0
 */
@Data
@Builder(toBuilder = true)
public class CodaIndividualTransactionRecord
{
   // Detailed record objects - all transaction data is accessed through these
   private CodaRecord21 record21;
   private CodaRecord22 record22;
   private CodaRecord23 record23;
   private CodaRecord31 record31;
   private CodaRecord32 record32;

   // Convenience methods for backward compatibility - delegate to nested records

   // From Record21
   public Integer getSequenceNumber() {
      return record21 != null && record21.getContinuousSequenceNumber() != null
            ? Integer.parseInt(record21.getContinuousSequenceNumber()) : null;
   }

   public String getAccountNumber() {
      return record21 != null ? record21.getReferenceNumber() : null;
   }

   public String getTransactionCode() {
      return record21 != null ? record21.getTransactionCode() : null;
   }

   public BigDecimal getAmount() {
      return record21 != null ? record21.getAmount() : null;
   }

   public LocalDate getValueDate() {
      return record21 != null ? record21.getValueDate() : null;
   }

   public String getTransactionReference() {
      return record21 != null ? record21.getReferenceNumber() : null;
   }

   public String getCommunicationStructured() {
      return record21 != null ? record21.getCommunicationZone() : null;
   }

   public LocalDate getTransactionDate() {
      return record21 != null ? record21.getEntryDate() : null;
   }

   public String getStatementNumber() {
      return record21 != null ? record21.getStatementNumber() : null;
   }

   public String getGlobalSequence() {
      return record21 != null ? record21.getGlobalisationCode() : null;
   }

   public String getStatementSequence() {
      return record21 != null ? record21.getLinkCode() : null;
   }

   // From Record22
   public String getCounterpartyName() {
      return record22 != null ? record22.getCounterpartyName() : null;
   }

   public String getCounterpartyBic() {
      return record22 != null ? record22.getCounterpartyBic() : null;
   }

   public String getTransactionCategory() {
      return record22 != null ? record22.getTransactionCategory() : null;
   }

   public String getPurposeCategory() {
      return record23 != null ? record23.getPurposeCategory() : null;
   }

   // From Record23
   public String getCounterpartyAccount() {
      return record23 != null ? record23.getCounterpartyAccount() : null;
   }

   public String getCounterpartyAccountName() {
      return record23 != null ? record23.getCounterpartyAccountName() : null;
   }

   // From Record31
   public String getStructuredCommunication() {
      return record31 != null ? record31.getStructuredCommunication() : null;
   }

   // From Record32
   public String getCounterpartyAddress() {
      return record32 != null ? record32.getCounterpartyAddress() : null;
   }

   public String getCounterpartyPostalCode() {
      return record32 != null ? record32.getCounterpartyPostalCode() : null;
   }

   public String getCounterpartyCity() {
      return record32 != null ? record32.getCounterpartyCity() : null;
   }
}
