package com.elleined.marketplaceapi.model.product;

import com.elleined.marketplaceapi.model.Crop;
import com.elleined.marketplaceapi.model.cart.RetailCartItem;
import com.elleined.marketplaceapi.model.message.prv.PrivateChatRoom;
import com.elleined.marketplaceapi.model.order.Order;
import com.elleined.marketplaceapi.model.order.RetailOrder;
import com.elleined.marketplaceapi.model.product.sale.SaleRetailProduct;
import com.elleined.marketplaceapi.model.unit.RetailUnit;
import com.elleined.marketplaceapi.model.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tbl_product_retail")
@NoArgsConstructor
@Getter
@Setter
public class RetailProduct extends Product {

    @Column(name = "price_per_unit", nullable = false)
    private double pricePerUnit;

    @Column(name = "quantity_per_unit", nullable = false)
    private int quantityPerUnit;

    @Column(name = "date_of_expiration", nullable = false)
    private LocalDate expirationDate;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "retail_unit_id",
            referencedColumnName = "id",
            nullable = false
    )
    private RetailUnit retailUnit;

    @OneToOne(mappedBy = "retailProduct")
    private SaleRetailProduct saleRetailProduct;

    // retail product id reference is in tbl order retail
    @OneToMany(mappedBy = "retailProduct")
    private List<RetailOrder> retailOrders;

    // retail product id reference is in tbl cart retail
    @OneToMany(mappedBy = "retailProduct")
    private List<RetailCartItem> retailCartItems;

    @Builder(builderMethodName = "retailProductBuilder")
    public RetailProduct(int id, String description, int availableQuantity, LocalDate harvestDate, LocalDateTime listingDate, String picture, State state, Status status, User seller, Crop crop, List<PrivateChatRoom> privateChatRooms, double pricePerUnit, int quantityPerUnit, LocalDate expirationDate, RetailUnit retailUnit, SaleRetailProduct saleRetailProduct, List<RetailOrder> retailOrders, List<RetailCartItem> retailCartItems) {
        super(id, description, availableQuantity, harvestDate, listingDate, picture, state, status, seller, crop, privateChatRooms);
        this.pricePerUnit = pricePerUnit;
        this.quantityPerUnit = quantityPerUnit;
        this.expirationDate = expirationDate;
        this.retailUnit = retailUnit;
        this.saleRetailProduct = saleRetailProduct;
        this.retailOrders = retailOrders;
        this.retailCartItems = retailCartItems;
    }


    @Override
    public boolean hasSoldOrder() {
        return this.retailOrders.stream()
                .map(RetailOrder::getStatus)
                .anyMatch(orderStatus -> orderStatus.equals(Order.Status.SOLD));
    }

    @Override
    public boolean hasPendingOrder() {
        return this.retailOrders.stream()
                .map(RetailOrder::getStatus)
                .anyMatch(orderStatus -> orderStatus.equals(Order.Status.PENDING));
    }

    @Override
    public boolean hasAcceptedOrder() {
        return this.retailOrders.stream()
                .map(RetailOrder::getStatus)
                .anyMatch(orderStatus -> orderStatus.equals(Order.Status.ACCEPTED));
    }


    @Override
    public boolean isSale() {
        return saleRetailProduct != null;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate) ||
                this.getState() == Product.State.EXPIRED ||
                LocalDate.now().equals(expirationDate);
    }

    public boolean hasNoAvailableQuantity(int orderQuantity) {
        return (this.getAvailableQuantity() - orderQuantity) < 0;
    }
}
