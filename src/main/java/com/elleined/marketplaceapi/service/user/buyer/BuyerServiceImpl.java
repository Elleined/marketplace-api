package com.elleined.marketplaceapi.service.user.buyer;

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
import com.elleined.marketplaceapi.mapper.ItemMapper;
import com.elleined.marketplaceapi.model.Product;
import com.elleined.marketplaceapi.model.item.OrderItem;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.OrderItemRepository;
import com.elleined.marketplaceapi.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@Qualifier("buyerServiceImpl")
public class BuyerServiceImpl implements BuyerService, BuyerOrderChecker {
    private final ProductService productService;

    private final OrderItemRepository orderItemRepository;
    private final ItemMapper itemMapper;

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
            ProductExpiredException {

        Product product = productService.getById(orderItemDTO.getProductId());
        if (product.isExpired())
            throw new ProductExpiredException("You cannot order this product because it has expired and is no longer available for purchase.");
        if (product.isRejected())
            throw new ProductRejectedException("You cannot order this product because it has been rejected by a moderator.");
        if (isBuyerHasPendingOrderToProduct(buyer, product))
            throw new ProductHasPendingOrderException("You cannot order this product because you already have a pending order. Please wait for the seller to take action on your order request.");
        if (isBuyerHasAcceptedOrderToProduct(buyer, product))
            throw new ProductHasAcceptedOrderException("You cannot order this product because you already have an accepted order. Please contact the seller to complete your order.");
        if (product.isDeleted())
            throw new ResourceNotFoundException(" You cannot order this product because it does not exist or may have already been deleted.");
        if (product.isSold())
            throw new ProductAlreadySoldException(" You cannot order this product because it has already been sold.");
        if (!product.isListed())
            throw new ProductNotListedException(" You cannot order this product because it has not yet been listed for sale.");
        if (buyer.hasProduct(product))
            throw new ResourceOwnedException(" You cannot order your own product listing.");
        if (product.isExceedingToAvailableQuantity(orderItemDTO.getOrderQuantity()))
            throw new OrderQuantiantyExceedsException("You cannot order this product because the order quantity exceeds the available amount.");
        if (isBuyerAlreadyBeenRejected(buyer, product))
            throw new BuyerAlreadyRejectedException(" You cannot order this product because the seller of this product has rejected your order request before. Please wait for at least 1 day before reordering this product.");

        OrderItem orderItem = itemMapper.toOrderItemEntity(orderItemDTO, buyer);
        double price = productService.calculateOrderPrice(orderItem.getProduct(), orderItemDTO.getOrderQuantity());
        orderItem.setUpdatedAt(LocalDateTime.now());
        orderItem.setPrice(price);

        buyer.getOrderedItems().add(orderItem);
        orderItemRepository.save(orderItem);
        log.debug("User with id of {} successfully ordered product with id of {}", buyer.getId(), orderItem.getProduct().getId());
        return orderItem;
    }

    @Override
    public List<OrderItem> getAllOrderedProductsByStatus(User currentUser, OrderItem.OrderItemStatus orderItemStatus) {
        return currentUser.getOrderedItems().stream()
                .filter(orderItem -> orderItem.getOrderItemStatus() == orderItemStatus)
                .filter(orderItem -> orderItem.getProduct().getStatus() == Product.Status.ACTIVE)
                .sorted(Comparator.comparing(OrderItem::getOrderDate).reversed())
                .toList();
    }

    @Override
    public void cancelOrderItem(User buyer, OrderItem orderItem)
            throws NotOwnedException, OrderAlreadyAcceptedException, OrderReachedCancellingTimeLimitException, OrderAlreadyRejectedException {

        if (!buyer.hasOrder(orderItem))
            throw new NotOwnedException("You cannot cancel this order because you do not own it!");
        if (orderItem.isAccepted())
            throw new OrderAlreadyAcceptedException("You cannot cancel this order because the seller has already accepted your order request for this product!");
        if (orderItem.reachedCancellingTimeLimit())
            throw new OrderReachedCancellingTimeLimitException("You cannot cancel this order because orders can only be canceled within the first 24 hours from the time of purchase, and this order has exceeded the cancellation window!");

        orderItem.setOrderItemStatus(OrderItem.OrderItemStatus.CANCELLED);
        orderItem.setUpdatedAt(LocalDateTime.now());
        orderItemRepository.save(orderItem);
        log.debug("Buyer with id of {} cancel his order in product with id of {}", buyer.getId(), orderItem.getProduct().getId());
    }

    @Override
    public boolean isBuyerHasPendingOrderToProduct(User buyer, Product product) {
        return buyer.getOrderedItems().stream()
                .filter(orderItem -> orderItem.getOrderItemStatus() == OrderItem.OrderItemStatus.PENDING)
                .map(OrderItem::getProduct)
                .anyMatch(product::equals);
    }

    @Override
    public boolean isBuyerHasAcceptedOrderToProduct(User buyer, Product product) {
        return buyer.getOrderedItems().stream()
                .filter(orderItem -> orderItem.getOrderItemStatus() == OrderItem.OrderItemStatus.ACCEPTED)
                .map(OrderItem::getProduct)
                .anyMatch(product::equals);
    }
    @Override
    public boolean isBuyerAlreadyBeenRejected(User buyer, Product product) {
        return buyer.getOrderedItems().stream()
                .filter(orderItem -> orderItem.getProduct().equals(product))
                .anyMatch(orderItem -> {
                    LocalDateTime reOrderingDate = orderItem.getUpdatedAt().plusDays(1);
                    return orderItem.getOrderItemStatus() == OrderItem.OrderItemStatus.REJECTED && LocalDateTime.now().isBefore(reOrderingDate);
                });
    }
}
