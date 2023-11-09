package com.elleined.marketplaceapi.service.product;

import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.model.order.Order;
import com.elleined.marketplaceapi.model.order.RetailOrder;
import com.elleined.marketplaceapi.model.product.Product;
import com.elleined.marketplaceapi.model.product.RetailProduct;
import com.elleined.marketplaceapi.model.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


public interface ProductService<T extends Product> {

    T getById(int productId) throws ResourceNotFoundException;

    // Use this to get all the product listing available
    List<T> getAllExcept(User currentUser);

    Set<T> getAllById(Set<Integer> productsToBeListedId);

    List<T> getAllByState(User seller, Product.State state);

    List<T> searchProductByCropName(String cropName);

    List<T> getByDateRange(User seller, LocalDateTime start, LocalDateTime end);

    void cancelAllPendingAndAcceptedOrders(T t);
}
