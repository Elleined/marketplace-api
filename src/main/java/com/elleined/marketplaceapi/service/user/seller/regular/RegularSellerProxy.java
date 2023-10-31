package com.elleined.marketplaceapi.service.user.seller.regular;

import com.elleined.marketplaceapi.dto.ProductDTO;
import com.elleined.marketplaceapi.exception.atm.InsufficientFundException;
import com.elleined.marketplaceapi.exception.field.FieldException;
import com.elleined.marketplaceapi.exception.field.NotValidBodyException;
import com.elleined.marketplaceapi.exception.order.MaxOrderRejectionException;
import com.elleined.marketplaceapi.exception.product.*;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.exception.user.InsufficientBalanceException;
import com.elleined.marketplaceapi.exception.user.NotOwnedException;
import com.elleined.marketplaceapi.exception.user.NotVerifiedException;
import com.elleined.marketplaceapi.exception.user.seller.SellerMaxAcceptedOrderException;
import com.elleined.marketplaceapi.exception.user.seller.SellerMaxListingException;
import com.elleined.marketplaceapi.exception.user.seller.SellerMaxPendingOrderException;
import com.elleined.marketplaceapi.model.Product;
import com.elleined.marketplaceapi.model.item.OrderItem;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.OrderItemRepository;
import com.elleined.marketplaceapi.repository.ProductRepository;
import com.elleined.marketplaceapi.service.fee.FeeService;
import com.elleined.marketplaceapi.service.product.ProductService;
import com.elleined.marketplaceapi.service.user.seller.SellerService;
import com.elleined.marketplaceapi.service.user.seller.premium.PremiumSellerProxy;
import com.elleined.marketplaceapi.utils.Formatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

@Service
@Slf4j
@Transactional
@Primary
public class RegularSellerProxy implements SellerService, RegularSellerRestriction {
    public final static float LISTING_FEE_PERCENTAGE = 2;
    public final static float SUCCESSFUL_TRANSACTION_FEE = 2;
    private final SellerService sellerService;

    private final OrderItemRepository orderItemRepository;

    private final ProductRepository productRepository;
    private final ProductService productService;

    private final FeeService feeService;

    public RegularSellerProxy(@Qualifier("sellerServiceImpl") SellerService sellerService,
                              OrderItemRepository orderItemRepository,
                              ProductRepository productRepository,
                              ProductService productService,
                              FeeService feeService) {
        this.sellerService = sellerService;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.feeService = feeService;
    }

    @Override
    public boolean isBalanceNotEnoughToPayListingFee(User seller, double listingFee) {
        return seller.getBalance().compareTo(new BigDecimal(listingFee)) <= 0;
    }

    @Override
    public boolean isBalanceNotEnoughToPaySuccessfulTransactionFee(User seller, double successfulTransactionFee) {
        return sellerService.isBalanceNotEnoughToPaySuccessfulTransactionFee(seller, successfulTransactionFee);
    }

    @Override
    public boolean isExceedsToMaxListingPerDay(User seller) {
        final LocalDateTime currentDateTimeMidnight = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        final LocalDateTime tomorrowMidnight = currentDateTimeMidnight.plusDays(1);
        return productRepository.fetchSellerProductListingCount(
                currentDateTimeMidnight,
                tomorrowMidnight,
                seller
        ) >= MAX_LISTING_PER_DAY;
    }

    @Override
    public boolean isExceedsToMaxRejectionPerDay(User seller) {
        final LocalDateTime currentDateTimeMidnight = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        final LocalDateTime tomorrowMidnight = currentDateTimeMidnight.plusDays(1);
        return orderItemRepository.fetchSellerRejectedOrderCount(
                currentDateTimeMidnight,
                tomorrowMidnight,
                seller,
                OrderItem.OrderItemStatus.REJECTED
        ) >= MAX_ORDER_REJECTION_PER_DAY;
    }

    @Override
    public boolean isExceedsToMaxAcceptedOrder(User seller) {
        return seller.getProducts().stream()
                .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                .map(Product::getOrders)
                .flatMap(Collection::stream)
                .filter(orderItem -> orderItem.getOrderItemStatus() == OrderItem.OrderItemStatus.ACCEPTED)
                .count() >= MAX_ACCEPTED_ORDER;
    }

    @Override
    public boolean isExceedsToMaxPendingOrder(User seller) {
        return seller.getProducts().stream()
                .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                .map(Product::getOrders)
                .flatMap(Collection::stream)
                .filter(orderItem -> orderItem.getOrderItemStatus() == OrderItem.OrderItemStatus.PENDING)
                .count() >= MAX_PENDING_ORDER;
    }

    @Override
    public Product saleProduct(User seller, Product product, int salePercentage) throws NotOwnedException, ProductSaleException, FieldException, ProductNotListedException {
        // Add more validation for regular seller here for future
        return sellerService.saleProduct(seller, product, salePercentage);
    }

    @Override
    public Product saveProduct(User seller, ProductDTO productDTO, MultipartFile productPicture)
            throws NotVerifiedException, InsufficientFundException, ProductExpirationLimitException, IOException {

        if (isExceedsToMaxAcceptedOrder(seller))
            throw new SellerMaxAcceptedOrderException("You cannot save this product because you have already exceeded the maximum number of accepted orders, which is set at " + MAX_ACCEPTED_ORDER + ". To proceed, consider either rejecting some accepted orders or marking the accepted orders as sold. You may also explore the option of purchasing a premium account to remove this restriction!");
        if (isExceedsToMaxPendingOrder(seller))
            throw new SellerMaxPendingOrderException("You cannot save this product because you have already exceeded the maximum number of pending orders, which is set at " + MAX_PENDING_ORDER + ". To proceed, please accept some pending orders. You may also explore the option of purchasing a premium account to remove this restriction!");
        if (isExceedsToMaxListingPerDay(seller))
            throw new SellerMaxListingException("You cannot save this product because you have reached the daily limit for product listings, which is set at " + MAX_LISTING_PER_DAY + ". To proceed, you may consider purchasing a premium account to remove this restriction!");
        // Add more validation for regular seller here for future

        double totalPrice = productService.calculateTotalPrice(productDTO.getPricePerUnit(), productDTO.getQuantityPerUnit(), productDTO.getAvailableQuantity());
        double listingFee = getListingFee(totalPrice);
        if (isBalanceNotEnoughToPayListingFee(seller, listingFee))
            throw new InsufficientBalanceException("You cannot save this product because you do not have a sufficient balance to cover the listing fee. The fee amounts to " + Formatter.formatDouble(listingFee) + ", which is " + LISTING_FEE_PERCENTAGE + "% of the total price of " + Formatter.formatDouble(totalPrice) + ". Consider purchasing a premium account to eliminate the listing fee.");

        feeService.deductListingFee(seller, listingFee);
        return sellerService.saveProduct(seller, productDTO, productPicture);
    }

    @Override
    public void updateProduct(User seller, Product product, ProductDTO productDTO, MultipartFile productPicture)
            throws NotOwnedException,
            NotVerifiedException,
            ProductAlreadySoldException,
            ResourceNotFoundException,
            ProductHasAcceptedOrderException,
            ProductHasPendingOrderException,
            SellerMaxAcceptedOrderException,
            SellerMaxPendingOrderException, IOException {

        if (isExceedsToMaxAcceptedOrder(seller))
            throw new SellerMaxAcceptedOrderException("You cannot update this product because you have already exceeded the maximum number of accepted orders, which is set at " + MAX_ACCEPTED_ORDER + ". To proceed, consider either rejecting some accepted orders or marking the accepted orders as sold. You may also explore the option of purchasing a premium account to remove this restriction.");
        if (isExceedsToMaxPendingOrder(seller))
            throw new SellerMaxPendingOrderException("You cannot update this product because you have already exceeded the maximum number of pending orders, which is set at " + MAX_PENDING_ORDER + ". To proceed, please accept some pending orders. You may also explore the option of purchasing a premium account to remove this restriction.");
        // Add more validation for regular seller here for future

        double totalPrice = productService.calculateTotalPrice(productDTO.getPricePerUnit(), productDTO.getQuantityPerUnit(), productDTO.getAvailableQuantity());
        double listingFee = getListingFee(totalPrice);
        if (isBalanceNotEnoughToPayListingFee(seller, listingFee))
            throw new InsufficientBalanceException("You cannot update this product because you do not have a sufficient balance to cover the listing fee. The fee amounts to " + Formatter.formatDouble(listingFee) + ", which is " + LISTING_FEE_PERCENTAGE + "% of the total price of " + Formatter.formatDouble(totalPrice) + ". Consider purchasing a premium account to eliminate the listing fee.");

        sellerService.updateProduct(seller, product, productDTO, productPicture);
    }

    @Override
    public void deleteProduct(User seller, Product product)
            throws NotOwnedException,
            NotVerifiedException,
            ProductAlreadySoldException,
            ProductHasPendingOrderException,
            ProductHasAcceptedOrderException,
            SellerMaxAcceptedOrderException,
            SellerMaxPendingOrderException {

        if (isExceedsToMaxAcceptedOrder(seller))
            throw new SellerMaxAcceptedOrderException("You cannot delete this product because you have already exceeded the maximum number of accepted orders, which is set at " + MAX_ACCEPTED_ORDER + ". To proceed, consider either rejecting some accepted orders or marking the accepted orders as sold. You may also explore the option of purchasing a premium account to remove this restriction.");
        if (isExceedsToMaxPendingOrder(seller))
            throw new SellerMaxPendingOrderException("You cannot delete this product because you have already exceeded the maximum number of pending orders, which is set at " + MAX_PENDING_ORDER + ". To proceed, please accept some pending orders. You may also explore the option of purchasing a premium account to remove this restriction.");
        // Add more validation for regular seller here for future

        sellerService.deleteProduct(seller, product);
    }

    @Override
    public void acceptOrder(User seller, OrderItem orderItem, String messageToBuyer)
            throws NotOwnedException,
            NotValidBodyException,
            ProductRejectedException,
            SellerMaxAcceptedOrderException {

        if (isExceedsToMaxAcceptedOrder(seller))
            throw new SellerMaxAcceptedOrderException("You cannot accept this order because you have already exceeded the maximum number of accepted orders, which is set at " + MAX_ACCEPTED_ORDER + ". To proceed, consider either rejecting some accepted orders or marking the accepted orders as sold. You may also explore the option of purchasing a premium account to remove this restriction.");
        // Add more validation for regular seller here for future

        sellerService.acceptOrder(seller, orderItem, messageToBuyer);
    }

    @Override
    public void rejectOrder(User seller, OrderItem orderItem, String messageToBuyer)
            throws NotOwnedException,
            NotValidBodyException,
            MaxOrderRejectionException,
            SellerMaxPendingOrderException {

        if (isExceedsToMaxPendingOrder(seller))
            throw new SellerMaxPendingOrderException("You cannot reject this order because you have already exceeded the maximum number of pending orders, which is set at " + MAX_PENDING_ORDER + ". To proceed, please accept some pending orders. You may also explore the option of purchasing a premium account to remove this restriction.");
        if (isExceedsToMaxRejectionPerDay(seller))
            throw new MaxOrderRejectionException("You cannot reject this order because you have reached the rejection limit per day, which is set at " + MAX_ORDER_REJECTION_PER_DAY + ". Please try again tomorrow. You may also consider buying a premium account to remove this restriction.");
        // Add more validation for regular seller here for future

        sellerService.rejectOrder(seller, orderItem, messageToBuyer);
    }

    @Override
    public void soldOrder(User seller, OrderItem orderItem)
            throws NotOwnedException, InsufficientFundException, InsufficientBalanceException {

        double orderPrice = orderItem.getPrice();
        double successfulTransactionFee = getSuccessfulTransactionFee(orderPrice);
        if (isBalanceNotEnoughToPaySuccessfulTransactionFee(seller, successfulTransactionFee))
            throw new InsufficientBalanceException("You cannot complete the sale of this order because your balance is insufficient to cover the successful transaction fee, which amounts to " + successfulTransactionFee + ". This fee represents " + SUCCESSFUL_TRANSACTION_FEE + "% of the order's total price of [orderPrice]. Consider purchasing a premium account to reduce the successful transaction fee to " + PremiumSellerProxy.SUCCESSFUL_TRANSACTION_FEE_PERCENTAGE);
        feeService.deductSuccessfulTransactionFee(seller, successfulTransactionFee);

        sellerService.soldOrder(seller, orderItem);
    }

    private double getListingFee(double productTotalPrice) {
        return (productTotalPrice * (LISTING_FEE_PERCENTAGE / 100f));
    }

    @Override
    public double getSuccessfulTransactionFee(double orderPrice) {
        return (orderPrice * (SUCCESSFUL_TRANSACTION_FEE / 100f));
    }
}
