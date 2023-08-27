package com.elleined.marketplaceapi.service.user.seller.regular;

import com.elleined.marketplaceapi.dto.ProductDTO;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.exception.field.NotValidBodyException;
import com.elleined.marketplaceapi.exception.order.MaxOrderRejectionException;
import com.elleined.marketplaceapi.exception.product.ProductAlreadySoldException;
import com.elleined.marketplaceapi.exception.product.ProductHasAcceptedOrderException;
import com.elleined.marketplaceapi.exception.product.ProductHasPendingOrderException;
import com.elleined.marketplaceapi.exception.user.InsufficientBalanceException;
import com.elleined.marketplaceapi.exception.user.NotOwnedException;
import com.elleined.marketplaceapi.exception.user.NotVerifiedException;
import com.elleined.marketplaceapi.exception.user.seller.SellerMaxAcceptedOrderException;
import com.elleined.marketplaceapi.exception.user.seller.SellerMaxListingException;
import com.elleined.marketplaceapi.model.Product;
import com.elleined.marketplaceapi.model.item.OrderItem;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.OrderItemRepository;
import com.elleined.marketplaceapi.repository.ProductRepository;
import com.elleined.marketplaceapi.service.fee.FeeService;
import com.elleined.marketplaceapi.service.product.ProductService;
import com.elleined.marketplaceapi.service.user.seller.SellerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@Transactional
@Primary
public class RegularSellerProxy implements SellerService, RegularSellerRestriction {
    private final float LISTING_FEE_PERCENTAGE = 5;
    private final float SUCCESSFUL_TRANSACTION_FEE = 5;
    private final SellerService sellerService;

    private final OrderItemRepository orderItemRepository;

    private final ProductRepository productRepository;
    private final ProductService productService;

    private final FeeService feeService;

    public RegularSellerProxy(@Qualifier("sellerServiceImpl") SellerService sellerService,
                              OrderItemRepository orderItemRepository,
                              ProductRepository productRepository,
                              ProductService productService, FeeService feeService) {
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
    public Product saveProduct(ProductDTO productDTO, User seller)
            throws NotVerifiedException,
            SellerMaxAcceptedOrderException,
            SellerMaxListingException,
            InsufficientBalanceException {

        if (isExceedsToMaxAcceptedOrder(seller)) throw new SellerMaxAcceptedOrderException("Cannot proceed because seller with id of " + seller.getId() + " exceeds to max accepted order which is " + MAX_ACCEPTED_ORDER + " please either reject the accepted order or set the accepted orders to sold to proceed!");
        if (isExceedsToMaxListingPerDay(seller)) throw new SellerMaxListingException("You already reached the limit of product listing per day which is " + MAX_LISTING_PER_DAY);
        // Add more validation for regular seller here for future

        double totalPrice = productService.calculateTotalPrice(productDTO.getPricePerUnit(), productDTO.getQuantityPerUnit(), productDTO.getAvailableQuantity());
        double listingFee = getListingFee(totalPrice);
        if (isBalanceNotEnoughToPayListingFee(seller, listingFee)) throw new InsufficientBalanceException("Seller with id of " + seller.getId() + " doesn't have enough balance to pay for the listing fee of " + listingFee + " which is " + LISTING_FEE_PERCENTAGE + "%  of total price " + totalPrice);
        feeService.deductListingFee(seller, listingFee);
        return sellerService.saveProduct(productDTO, seller);
    }

    @Override
    public void updateProduct(User seller, Product product, ProductDTO productDTO)
            throws NotOwnedException,
            NotVerifiedException,
            ProductAlreadySoldException,
            ResourceNotFoundException,
            SellerMaxAcceptedOrderException {

        if (isExceedsToMaxAcceptedOrder(seller)) throw new SellerMaxAcceptedOrderException("Cannot proceed because seller with id of " + seller.getId() + " exceeds to max accepted order which is " + MAX_ACCEPTED_ORDER + " please either reject the accepted order or set the accepted orders to sold to proceed!");
        // Add more validation for regular seller here for future

        sellerService.updateProduct(seller, product, productDTO);
    }

    @Override
    public void deleteProduct(User seller, Product product)
            throws NotOwnedException,
            NotVerifiedException,
            ProductAlreadySoldException,
            ProductHasPendingOrderException,
            ProductHasAcceptedOrderException,
            SellerMaxAcceptedOrderException {

        if (isExceedsToMaxAcceptedOrder(seller)) throw new SellerMaxAcceptedOrderException("Cannot proceed because seller with id of " + seller.getId() + " exceeds to max accepted order which is " + MAX_ACCEPTED_ORDER + " please either reject the accepted order or set the accepted orders to sold to proceed!");
        // Add more validation for regular seller here for future

        sellerService.deleteProduct(seller, product);
    }

    @Override
    public void acceptOrder(User seller, OrderItem orderItem, String messageToBuyer)
            throws NotOwnedException,
            NotValidBodyException,
            SellerMaxAcceptedOrderException {

        if (isExceedsToMaxAcceptedOrder(seller)) throw new SellerMaxAcceptedOrderException("Cannot proceed because seller with id of " + seller.getId() + " exceeds to max accepted order which is " + MAX_ACCEPTED_ORDER + " please either reject the accepted order or set the accepted orders to sold to proceed!");
        // Add more validation for regular seller here for future

        sellerService.acceptOrder(seller, orderItem, messageToBuyer);
    }

    @Override
    public void rejectOrder(User seller, OrderItem orderItem, String messageToBuyer)
            throws NotOwnedException,
            NotValidBodyException,
            MaxOrderRejectionException {

        if (isExceedsToMaxRejectionPerDay(seller)) throw new MaxOrderRejectionException("Cannot reject order anymore! Because seller with id of " + seller.getId() + " already reached the rejection limit per day which is " + MAX_ORDER_REJECTION_PER_DAY + " come back again tomorrow... Thanks");
        // Add more validation for regular seller here for future

        sellerService.rejectOrder(seller, orderItem, messageToBuyer);
    }

    @Override
    public void soldOrder(User seller, OrderItem orderItem)
            throws NotOwnedException,
            InsufficientBalanceException {

        double orderPrice = orderItem.getPrice();
        double successfulTransactionFee = getSuccessfulTransactionFee(orderPrice);
        if (isBalanceNotEnoughToPaySuccessfulTransactionFee(seller, successfulTransactionFee)) throw new InsufficientBalanceException("Seller with id of " + seller.getId() + " doesn't have enough balance to pay for the successful transaction fee of " + successfulTransactionFee + " which is the " + SUCCESSFUL_TRANSACTION_FEE + "% of order price of " + orderPrice);
        feeService.deductSuccessfulTransactionFee(seller, successfulTransactionFee);

        sellerService.soldOrder(seller, orderItem);
    }

    @Override
    public List<Product> getAllProductByState(User seller, Product.State state) {
        return sellerService.getAllProductByState(seller, state);
    }

    @Override
    public List<OrderItem> getAllSellerProductOrderByStatus(User seller, OrderItem.OrderItemStatus orderItemStatus) {
        return sellerService.getAllSellerProductOrderByStatus(seller, orderItemStatus);
    }

    private double getListingFee(double productTotalPrice) {
        return (productTotalPrice * (LISTING_FEE_PERCENTAGE / 100f));
    }

    @Override
    public double getSuccessfulTransactionFee(double orderPrice) {
        return (orderPrice * (SUCCESSFUL_TRANSACTION_FEE / 100f));
    }
}
