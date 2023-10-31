package com.elleined.marketplaceapi.service.cart;

import com.elleined.marketplaceapi.dto.item.CartItemDTO;
import com.elleined.marketplaceapi.exception.order.OrderQuantiantyExceedsException;
import com.elleined.marketplaceapi.exception.product.*;
import com.elleined.marketplaceapi.exception.resource.AlreadyExistException;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.exception.resource.ResourceOwnedException;
import com.elleined.marketplaceapi.exception.user.NotOwnedException;
import com.elleined.marketplaceapi.exception.user.buyer.BuyerAlreadyRejectedException;
import com.elleined.marketplaceapi.model.Product;
import com.elleined.marketplaceapi.model.item.CartItem;
import com.elleined.marketplaceapi.model.item.OrderItem;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.service.product.ProductService;
import com.elleined.marketplaceapi.service.user.buyer.BuyerOrderChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@Primary
public class CartItemServiceProxy implements CartItemService {
    private final CartItemService cartItemService;
    private final ProductService productService;
    private final BuyerOrderChecker buyerOrderChecker;

    public CartItemServiceProxy(@Qualifier("cartItemServiceImpl") CartItemService cartItemService,
                                ProductService productService,
                                BuyerOrderChecker buyerOrderChecker) {
        this.cartItemService = cartItemService;
        this.productService = productService;
        this.buyerOrderChecker = buyerOrderChecker;
    }

    @Override
    public List<CartItem> getAll(User currentUser) {
        return cartItemService.getAll(currentUser);
    }

    @Override
    public void delete(User currentUser, CartItem cartItem) throws NotOwnedException {
        if (currentUser.getCartItems().stream().noneMatch(cartItem::equals)) throw new NotOwnedException("User with id of " + currentUser.getId() + " doesn't have cart item with id of " + cartItem.getId());
        cartItemService.delete(currentUser, cartItem);
    }

    @Override
    public void delete(CartItem cartItem) {
        cartItemService.delete(cartItem);
    }

    @Override
    public CartItem save(User currentUser, CartItemDTO cartItemDTO)
    throws AlreadyExistException,
            ProductHasPendingOrderException,
            ProductHasAcceptedOrderException,
            ResourceOwnedException,
            ResourceNotFoundException,
            ProductAlreadySoldException,
            ProductNotListedException,
            OrderQuantiantyExceedsException,
            ProductExpiredException,
            BuyerAlreadyRejectedException {
        Product product = productService.getById(cartItemDTO.getProductId());

        if (product.isExpired())
            throw new ProductExpiredException("You cannot add this product to your cart because it has already expired and is no longer available for purchase.");
        if (currentUser.isProductAlreadyInCart(product))
            throw new AlreadyExistException("You cannot add this product to your cart because it is already in your cart. If you wish to change the quantity, please remove the existing item from your cart and then add it again.");
        if (buyerOrderChecker.isBuyerHasPendingOrderToProduct(currentUser, product))
            throw new ProductHasPendingOrderException("You cannot add this product to your cart because you have a pending order for this item. Please wait for the seller to take action on your order request.");
        if (buyerOrderChecker.isBuyerHasAcceptedOrderToProduct(currentUser, product))
            throw new ProductHasAcceptedOrderException("You cannot add this product to your cart because you already have an accepted order for this item. Please contact the seller to complete your order.");
        if (currentUser.hasProduct(product))
            throw new ResourceOwnedException("You cannot add this product to your cart because it belongs to you. Cart items should be products from other sellers.");
        if (product.isDeleted())
            throw new ResourceNotFoundException("You cannot add this product to your cart because it may have been deleted or does not exist.");
        if (product.isSold())
            throw new ProductAlreadySoldException("You cannot add this product to your cart because it has already been sold to another buyer.");
        if (!product.isListed())
            throw new ProductNotListedException("You cannot add this product to your cart because it has not yet been listed for sale.");
        if (product.isExceedingToAvailableQuantity(cartItemDTO.getOrderQuantity()))
            throw new OrderQuantiantyExceedsException("You cannot add this product to your cart because the order quantity exceeds the available amount.");
        if (buyerOrderChecker.isBuyerAlreadyBeenRejected(currentUser, product))
            throw new BuyerAlreadyRejectedException("You cannot add this product to your cart because the seller has previously rejected your order request for this item. Please wait for at least 1 day before attempting to reorder this product.");
        double price = productService.calculateOrderPrice(product, cartItemDTO.getOrderQuantity());
        cartItemDTO.setPrice(price);
        return cartItemService.save(currentUser, cartItemDTO);
    }

    @Override
    public OrderItem moveToOrderItem(User currentUser, CartItem cartItem)
            throws ResourceNotFoundException,
            ResourceOwnedException,
            ProductHasPendingOrderException,
            ProductHasAcceptedOrderException,
            ProductAlreadySoldException,
            ProductNotListedException,
            OrderQuantiantyExceedsException,
            ProductExpiredException,
            BuyerAlreadyRejectedException {

        Product product = cartItem.getProduct();

        if (product.isExpired())
            throw new ProductExpiredException("You cannot order this product because it has already expired.");
        if (buyerOrderChecker.isBuyerHasPendingOrderToProduct(currentUser, product))
            throw new ProductHasPendingOrderException("You cannot order this product because you already have a pending order for it. Please wait for the seller to take action on your order request.");
        if (buyerOrderChecker.isBuyerHasAcceptedOrderToProduct(currentUser, product))
            throw new ProductHasAcceptedOrderException("You cannot order this product because you already have an accepted order for it. Please contact the seller to settle your order.");
        if (product.isDeleted())
            throw new ResourceNotFoundException("You cannot order this product because it does not exist or may have been deleted.");
        if (product.isSold())
            throw new ProductAlreadySoldException("You cannot order this product because it has already been sold.");
        if (!product.isListed())
            throw new ProductNotListedException("You cannot order this product because it has not yet been listed for sale.");
        if (product.isExceedingToAvailableQuantity(cartItem.getOrderQuantity()))
            throw new OrderQuantiantyExceedsException("You cannot order this product because the order quantity exceeds the available amount.");
        if (buyerOrderChecker.isBuyerAlreadyBeenRejected(currentUser, product))
            throw new BuyerAlreadyRejectedException("You cannot order this product because the seller has previously rejected your order request. Please wait for at least 1 day before attempting to reorder this product.");
        return cartItemService.moveToOrderItem(currentUser, cartItem);
    }

    @Override
    public List<OrderItem> moveAllToOrderItem(User currentUser, List<CartItem> cartItems) throws ResourceNotFoundException {
        return cartItems.stream()
                .map(cartItem -> this.moveToOrderItem(currentUser, cartItem))
                .toList();
    }

    @Override
    public CartItem getCartItemById(int cartItemId) throws ResourceNotFoundException {
        return cartItemService.getCartItemById(cartItemId);
    }

    @Override
    public CartItem getByProduct(User buyer, Product product) throws ResourceNotFoundException {
        return cartItemService.getByProduct(buyer, product);
    }

    @Override
    public List<CartItem> getAllById(List<Integer> cartItemIds) {
        return cartItemService.getAllById(cartItemIds);
    }
}
