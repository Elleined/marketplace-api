package com.elleined.marketplaceapi.service.user.seller.premium;

import com.elleined.marketplaceapi.dto.ProductDTO;
import com.elleined.marketplaceapi.exception.atm.InsufficientFundException;
import com.elleined.marketplaceapi.exception.field.FieldException;
import com.elleined.marketplaceapi.exception.field.NotValidBodyException;
import com.elleined.marketplaceapi.exception.product.*;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.exception.user.InsufficientBalanceException;
import com.elleined.marketplaceapi.exception.user.NotOwnedException;
import com.elleined.marketplaceapi.exception.user.NotVerifiedException;
import com.elleined.marketplaceapi.model.Product;
import com.elleined.marketplaceapi.model.item.OrderItem;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.service.fee.FeeService;
import com.elleined.marketplaceapi.service.user.seller.SellerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
@Transactional
@Qualifier("premiumSellerProxy")
public class PremiumSellerProxy implements SellerService {
    public static final float SUCCESSFUL_TRANSACTION_FEE_PERCENTAGE = 1;
    private final SellerService sellerService;
    private final FeeService feeService;

    public PremiumSellerProxy(@Qualifier("sellerServiceImpl") SellerService sellerService, FeeService feeService) {
        this.sellerService = sellerService;
        this.feeService = feeService;
    }

    @Override
    public Product saleProduct(User seller, Product product, int salePercentage) throws NotOwnedException, ProductSaleException, FieldException, ProductNotListedException {
        // add validation for here for premium seller for future
        return sellerService.saleProduct(seller, product, salePercentage);
    }

    @Override
    public Product saveProduct(User seller, ProductDTO productDTO, MultipartFile productPicture)
            throws NotVerifiedException, InsufficientFundException, ProductExpirationLimitException, IOException {
        // add validation for here for premium seller for future
        return sellerService.saveProduct(seller, productDTO, productPicture);
    }

    @Override
    public void updateProduct(User seller, Product product, ProductDTO productDTO, MultipartFile productPicture)
            throws NotOwnedException,
            NotVerifiedException,
            ProductAlreadySoldException,
            ResourceNotFoundException,
            ProductHasAcceptedOrderException,
            ProductHasPendingOrderException, IOException {
        // add validation for here for premium seller for future
        sellerService.updateProduct(seller, product, productDTO, productPicture);
    }

    @Override
    public void deleteProduct(User seller, Product product) throws NotOwnedException, NotVerifiedException, ProductAlreadySoldException, ProductHasPendingOrderException, ProductHasAcceptedOrderException {
        // add validation for here for premium seller for future
        sellerService.deleteProduct(seller, product);
    }

    @Override
    public void acceptOrder(User seller, OrderItem orderItem, String messageToBuyer)
            throws NotOwnedException,
            ProductRejectedException,
            NotValidBodyException {
        sellerService.acceptOrder(seller, orderItem, messageToBuyer);
    }

    @Override
    public void rejectOrder(User seller, OrderItem orderItem, String messageToBuyer) throws NotOwnedException, NotValidBodyException {
        sellerService.rejectOrder(seller, orderItem, messageToBuyer);
    }

    @Override
    public void soldOrder(User seller, OrderItem orderItem) throws NotOwnedException, InsufficientFundException, InsufficientBalanceException {
        double orderPrice = orderItem.getPrice();
        double successfulTransactionFee = getSuccessfulTransactionFee(orderPrice);
        if (isBalanceNotEnoughToPaySuccessfulTransactionFee(seller, successfulTransactionFee))
            throw new InsufficientBalanceException("You cannot complete the sale of this order because you do not have a sufficient balance to cover the successful transaction fee. The fee amounts to " + successfulTransactionFee + ", which is " + SUCCESSFUL_TRANSACTION_FEE_PERCENTAGE + "% of the order's total price of " + orderPrice);
        feeService.deductSuccessfulTransactionFee(seller, successfulTransactionFee);

        sellerService.soldOrder(seller, orderItem);
    }

    @Override
    public boolean isBalanceNotEnoughToPaySuccessfulTransactionFee(User seller, double successfulTransactionFee) {
        return sellerService.isBalanceNotEnoughToPaySuccessfulTransactionFee(seller, successfulTransactionFee);
    }

    @Override
    public double getSuccessfulTransactionFee(double orderPrice) {
        return (orderPrice * (SUCCESSFUL_TRANSACTION_FEE_PERCENTAGE / 100f));
    }
}
