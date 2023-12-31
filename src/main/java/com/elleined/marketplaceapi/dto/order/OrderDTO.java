package com.elleined.marketplaceapi.dto.order;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class OrderDTO {

    private Long id;

    private double price;

    private int sellerId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime orderDate;

    @Positive(message = "Product id cannot be 0 or less than zero!")
    private int productId;

    private int purchaserId; // This will be get in path variable

    @Positive(message = "Delivery address id cannot be 0 or less than zero!")
    private int deliveryAddressId;

    @JsonProperty("orderItemStatus")
    private String orderStatus;

    private String sellerMessage;

    private LocalDateTime updatedAt;
}
