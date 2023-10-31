package com.elleined.marketplaceapi.dto.item;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class ItemDTO {

    private Long id;

    @Positive(message = "Order quantity must be greater than zero!")
    private int orderQuantity;

    private double price;

    private int sellerId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime orderDate;

    @Positive(message = "Product id must be greater than zero!")
    private int productId;

    private int purchaserId; // This will be get in path variable

    @Positive(message = "Delivery address id must be greater than zero!")
    private int deliveryAddressId;
}
