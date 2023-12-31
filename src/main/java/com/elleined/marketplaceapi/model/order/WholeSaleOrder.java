package com.elleined.marketplaceapi.model.order;

import com.elleined.marketplaceapi.model.address.DeliveryAddress;
import com.elleined.marketplaceapi.model.product.WholeSaleProduct;
import com.elleined.marketplaceapi.model.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "tbl_order_whole_sale")
@NoArgsConstructor
@Setter
@Getter
public class WholeSaleOrder extends Order {

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "whole_sale_product",
            referencedColumnName = "product_id",
            nullable = false
    )
    private WholeSaleProduct wholeSaleProduct;

    @Builder(builderMethodName = "wholeSaleOrderBuilder")
    public WholeSaleOrder(int id, double price, LocalDateTime orderDate, User purchaser, DeliveryAddress deliveryAddress, Status status, String sellerMessage, LocalDateTime updatedAt, WholeSaleProduct wholeSaleProduct) {
        super(id, price, orderDate, purchaser, deliveryAddress, status, sellerMessage, updatedAt);
        this.wholeSaleProduct = wholeSaleProduct;
    }
}
