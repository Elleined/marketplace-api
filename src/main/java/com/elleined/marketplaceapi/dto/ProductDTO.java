package com.elleined.marketplaceapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private int id;

    @NotBlank(message = "Please provide a value for the Crop name")
    private String cropName;

    @NotBlank(message = "Please provide a value for the Unit name")
    private String unitName;

    @NotBlank(message = "Please provide a value for the Description")
    private String description;

    private String picture;

    @NotBlank(message = "Please provide a value for the Keyword")
    private String keyword;
    private String state;

    private int sellerId;
    private String sellerName;

    @Positive(message = "Available quantity must be greater than zero")
    private int availableQuantity;
    @Positive(message = "Price per unit must be greater than zero")
    private double pricePerUnit;
    @Positive(message = "Quantity per unit must be greater than zero")
    private int quantityPerUnit;

    @NotNull(message = "Please provide a value for the harvest date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "Cannot sell an product that are not have been harvested yet")
    private LocalDate harvestDate;

    @NotNull(message = "Please provide a value for the expiration date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")

    private LocalDate expirationDate;

    private LocalDate listingDate;

    private String shopName;

    private double totalPrice;
}
