package com.elleined.marketplaceapi.service.user.buyer.regular;

import com.elleined.marketplaceapi.dto.item.OrderItemDTO;
import com.elleined.marketplaceapi.exception.order.OrderAlreadyAcceptedException;
import com.elleined.marketplaceapi.exception.order.OrderAlreadyRejectedException;
import com.elleined.marketplaceapi.exception.order.OrderQuantiantyExceedsException;
import com.elleined.marketplaceapi.exception.order.OrderReachedCancellingTimeLimitException;
import com.elleined.marketplaceapi.exception.product.*;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.exception.resource.ResourceOwnedException;
import com.elleined.marketplaceapi.exception.user.NotOwnedException;
import com.elleined.marketplaceapi.exception.user.buyer.BuyerAlreadyRejectedException;
import com.elleined.marketplaceapi.exception.user.buyer.BuyerMaxOrderPerDayException;
import com.elleined.marketplaceapi.model.item.OrderItem;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.OrderItemRepository;
import com.elleined.marketplaceapi.service.user.buyer.BuyerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
@Primary
public class RegularBuyerProxy implements BuyerService, RegularBuyerRestriction {
    private final BuyerService buyerService;

    private final OrderItemRepository orderItemRepository;
    public RegularBuyerProxy(@Qualifier("buyerServiceImpl") BuyerService buyerService,
                             OrderItemRepository orderItemRepository) {
        this.buyerService = buyerService;
        this.orderItemRepository = orderItemRepository;
    }


    @Override
    public OrderItem orderProduct(User buyer, OrderItemDTO orderItemDTO)
            throws ResourceNotFoundException,
            ResourceOwnedException,
            ProductHasPendingOrderException,
            ProductHasAcceptedOrderException,
            ProductRejectedException,
            ProductAlreadySoldException,
            ProductNotListedException,
            OrderQuantiantyExceedsException,
            BuyerAlreadyRejectedException,
            BuyerMaxOrderPerDayException,
            ProductExpiredException {

        if (isBuyerExceedsToMaxOrderPerDay(buyer))
            throw new BuyerMaxOrderPerDayException("You cannot order this product because you have already reached the maximum number of orders allowed per day, which is set at " + MAX_ORDER_PER_DAY + ". Consider purchasing a premium account to remove this restriction.");
        // Add more validation for regular buyer here for future
        return buyerService.orderProduct(buyer, orderItemDTO);
    }

    @Override
    public List<OrderItem> getAllOrderedProductsByStatus(User currentUser, OrderItem.OrderItemStatus orderItemStatus) {
        // Add more validation for regular buyer here for future
        return buyerService.getAllOrderedProductsByStatus(currentUser, orderItemStatus);
    }

    @Override
    public void cancelOrderItem(User buyer, OrderItem orderItem)
            throws NotOwnedException, OrderAlreadyAcceptedException, OrderReachedCancellingTimeLimitException, OrderAlreadyRejectedException {
        // Add more validation for regular buyer here for future
        buyerService.cancelOrderItem(buyer, orderItem);
    }

    @Override
    public boolean isBuyerExceedsToMaxOrderPerDay(User buyer) {
        final LocalDateTime currentDateTimeMidnight = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        final LocalDateTime tomorrowMidnight = currentDateTimeMidnight.plusDays(1);
        return orderItemRepository.fetchBuyerOrderCount(
                currentDateTimeMidnight,
                tomorrowMidnight,
                buyer,
                OrderItem.OrderItemStatus.PENDING
        ) >= MAX_ORDER_PER_DAY;
    }
}
