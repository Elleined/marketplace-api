package com.elleined.marketplaceapi.service.cart.wholesale;

import com.elleined.marketplaceapi.dto.cart.WholeSaleCartItemDTO;
import com.elleined.marketplaceapi.exception.order.OrderQuantiantyExceedsException;
import com.elleined.marketplaceapi.exception.product.ProductAlreadySoldException;
import com.elleined.marketplaceapi.exception.product.ProductExpiredException;
import com.elleined.marketplaceapi.exception.product.ProductNotListedException;
import com.elleined.marketplaceapi.exception.product.order.ProductOrderAcceptedException;
import com.elleined.marketplaceapi.exception.product.order.ProductOrderPendingException;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.exception.resource.ResourceOwnedException;
import com.elleined.marketplaceapi.exception.user.buyer.BuyerAlreadyRejectedException;
import com.elleined.marketplaceapi.model.cart.WholeSaleCartItem;
import com.elleined.marketplaceapi.model.order.WholeSaleOrder;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.service.cart.CartItemService;

import java.util.List;

public interface WholeSaleCartItemService extends CartItemService<WholeSaleCartItem, WholeSaleCartItemDTO> {
    List<WholeSaleOrder> orderAllCartItems(User currentUser, List<WholeSaleCartItem> wholeSaleCartItems);

    @Override
    WholeSaleOrder orderCartItem(User currentUser, WholeSaleCartItem wholeSaleCartItem) throws ResourceNotFoundException, ResourceOwnedException, ProductOrderAcceptedException, ProductOrderPendingException, ProductAlreadySoldException, ProductNotListedException, ProductExpiredException, OrderQuantiantyExceedsException, BuyerAlreadyRejectedException;
}
