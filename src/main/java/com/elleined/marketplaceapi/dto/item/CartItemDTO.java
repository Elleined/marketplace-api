package com.elleined.marketplaceapi.dto.item;

import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class CartItemDTO extends ItemDTO {

    @Builder

    public CartItemDTO(Long id, @Positive(message = "Order quantity must be greater than zero!") int orderQuantity, double price, int sellerId, LocalDateTime orderDate, @Positive(message = "Product id must be greater than zero!") int productId, int purchaserId, @Positive(message = "Delivery address id must be greater than zero!") int deliveryAddressId) {
        super(id, orderQuantity, price, sellerId, orderDate, productId, purchaserId, deliveryAddressId);
    }
}
