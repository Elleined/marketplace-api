package com.elleined.marketplaceapi.dto.product.sale;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleProductRequest {
    @Positive(message = "Sale percentage cannot be 0 or less than 0")
    @Size(max = 100, message = "Sale percentage must be in range of 1 - 100 only")
    private int salePercentage;
}
