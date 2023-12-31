package com.elleined.marketplaceapi.model.order;


import com.elleined.marketplaceapi.model.address.DeliveryAddress;
import com.elleined.marketplaceapi.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_order")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Order {

    @Id
    @GeneratedValue(
            strategy = GenerationType.TABLE,
            generator = "orderAutoIncrement"
    )
    @SequenceGenerator(
            allocationSize = 1,
            name = "orderAutoIncrement",
            sequenceName = "orderAutoIncrement"
    )
    @Column(
            name = "product_id",
            unique = true,
            nullable = false,
            updatable = false
    )
    private int id;

    @Column(name = "order_price", nullable = false)
    private double price;

    @Column(
            name = "order_date",
            nullable = false,
            updatable = false
    )
    private LocalDateTime orderDate;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "purchaser_id",
            referencedColumnName = "user_id",
            nullable = false,
            updatable = false
    )
    private User purchaser;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "delivery_address_id",
            referencedColumnName = "address_id",
            nullable = false
    )
    private DeliveryAddress deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private Status status;

    @Column(name = "seller_message_to_purchaser")
    private String sellerMessage;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum Status {
        CANCELLED,
        PENDING,
        ACCEPTED,
        REJECTED,
        SOLD
    }

    public boolean reachedCancellingTimeLimit() {
        LocalDateTime maxCancelDateTime = this.getUpdatedAt().plusDays(1);
        return LocalDateTime.now().equals(maxCancelDateTime) || LocalDateTime.now().isAfter(maxCancelDateTime);
    }

    public boolean isAccepted() {
        return this.getStatus() == Status.ACCEPTED;
    }
    public boolean isPending() {return this.getStatus() == Status.PENDING;}
    public boolean isRejected() {
        return this.getStatus() == Status.REJECTED;
    }
}
