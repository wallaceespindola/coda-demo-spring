package com.example.coda.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
@Schema(description = "Input payload to generate a CODA statement")
public class CodaRequest {

   @NotBlank
   String bankName;

   @NotBlank
   String account;

   @NotBlank
   String currency;

   @NotNull
   @JsonFormat(shape = JsonFormat.Shape.STRING)
   LocalDate date;

   @NotNull
   BigDecimal opening;

   @NotNull
   @Valid
   @ArraySchema(schema = @Schema(implementation = Tx.class))
   List<Tx> transactions;

   @Value
   @Builder
   public static class Tx {
      @NotNull
      @JsonFormat(shape = JsonFormat.Shape.STRING)
      LocalDate bookingDate;

      @NotNull
      TransactionType type;

      @NotNull @Positive
      BigDecimal amount;

      @NotBlank
      String counterpartyName;

      @NotBlank
      String counterpartyAccount;

      String description;
      String reference;
   }
}
