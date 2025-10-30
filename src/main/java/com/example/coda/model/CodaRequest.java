package com.example.coda.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Input payload to generate a CODA statement")
public record CodaRequest(@NotBlank String bankName, @NotBlank String account, @NotBlank String currency,
                          @JsonFormat(shape = Shape.STRING) @NotNull LocalDate date, @NotNull BigDecimal opening,
                          @ArraySchema(schema = @Schema(implementation = Transaction.class)) @NotNull @Valid List<Transaction> transactions)
{

   @Builder
   public record Transaction(@JsonFormat(shape = Shape.STRING) @NotNull LocalDate bookingDate,
                             @NotNull TransactionType type, @NotNull @Positive BigDecimal amount,
                             @NotBlank String counterpartyName, @NotBlank String counterpartyAccount,
                             String description, String reference)
   {
   }
}
