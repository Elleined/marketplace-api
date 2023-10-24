package com.elleined.marketplaceapi.repository.order;

import com.elleined.marketplaceapi.model.order.Order;
import com.elleined.marketplaceapi.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT COUNT(oi)
            FROM OrderItem oi
            WHERE oi.updatedAt
            BETWEEN :currentDateTimeMidnight AND :tomorrowMidnight
            AND oi.product.seller = :seller
            AND oi.orderStatus = :orderStatus
            """)
    int fetchSellerRejectedOrderCount(@Param("currentDateTimeMidnight") LocalDateTime currentDateTimeMidnight,
                                 @Param("tomorrowMidnight") LocalDateTime tomorrowMidnight,
                                 @Param("seller") User seller,
                                 @Param("orderStatus") Order.OrderStatus orderStatus);

    @Query("""
            SELECT COUNT(oi)
            FROM OrderItem oi
            WHERE oi.orderDate
            BETWEEN :currentDateTimeMidnight AND :tomorrowMidnight
            AND oi.purchaser = :buyer
            AND oi.orderStatus = :orderStatus
            """)
    int fetchBuyerOrderCount(@Param("currentDateTimeMidnight") LocalDateTime currentDateTimeMidnight,
                             @Param("tomorrowMidnight") LocalDateTime tomorrowMidnight,
                             @Param("buyer") User buyer,
                             @Param("orderStatus") Order.OrderStatus orderStatus);
}