package com.elleined.marketplaceapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopDTO {

    private int id;

    private String shopName;

    private String description;

    private String picture;
    
    private String validId;
}