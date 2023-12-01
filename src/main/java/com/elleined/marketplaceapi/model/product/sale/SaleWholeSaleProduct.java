package com.elleined.marketplaceapi.model.product.sale;

import com.elleined.marketplaceapi.model.product.RetailProduct;
import com.elleined.marketplaceapi.model.product.WholeSaleProduct;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_sale_whole_sale_product")
@NoArgsConstructor
@Getter
@Setter
public class SaleWholeSaleProduct extends SaleProduct {

    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(
            name = "sale_product_id",
            referencedColumnName = "product_id"
    )
    private WholeSaleProduct wholeSaleProduct;

    @Builder(builderMethodName = "saleWholeSaleProductBuilder")
    public SaleWholeSaleProduct(int id, LocalDateTime createdAt, LocalDateTime updatedAt, double salePercentage, WholeSaleProduct wholeSaleProduct) {
        super(id, createdAt, updatedAt, salePercentage);
        this.wholeSaleProduct = wholeSaleProduct;
    }
}
