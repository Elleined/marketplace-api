package com.elleined.marketplaceapi.service.product.retail;

import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.model.product.Product;
import com.elleined.marketplaceapi.model.product.RetailProduct;
import com.elleined.marketplaceapi.model.user.Premium;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.PremiumRepository;
import com.elleined.marketplaceapi.repository.UserRepository;
import com.elleined.marketplaceapi.repository.product.RetailProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RetailProductServiceImpl implements RetailProductService {
    private final RetailProductRepository retailProductRepository;

    private final UserRepository userRepository;
    private final PremiumRepository premiumRepository;

    @Override
    public RetailProduct getById(int productId) throws ResourceNotFoundException {
        return retailProductRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Retail product with id of " + productId + " doesn't exists!"));
    }

    @Override
    public List<RetailProduct> getAllExcept(User currentUser) {
        List<RetailProduct> userProducts = currentUser.getRetailProducts();

        List<RetailProduct> premiumUserProducts = premiumRepository.findAll().stream()
                .map(Premium::getUser)
                .filter(User::isVerified)
                .filter(User::hasShopRegistration)
                .map(User::getRetailProducts)
                .flatMap(Collection::stream)
                .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                .filter(product -> product.getState() == Product.State.LISTING)
                .toList();

        List<RetailProduct> regularUserProducts = userRepository.findAll().stream()
                .filter(user -> !user.isPremium())
                .filter(User::isVerified)
                .filter(User::hasShopRegistration)
                .map(User::getRetailProducts)
                .flatMap(Collection::stream)
                .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                .filter(product -> product.getState() == Product.State.LISTING)
                .toList();

        List<RetailProduct> products = new ArrayList<>();
        products.addAll(premiumUserProducts);
        products.addAll(regularUserProducts);
        products.removeAll(userProducts);
        return products;
    }

    @Override
    public Set<RetailProduct> getAllById(Set<Integer> productsToBeListedId) {
        return new HashSet<>(retailProductRepository.findAllById(productsToBeListedId));
    }

    @Override
    public void deleteExpiredProducts() {
        List<RetailProduct> expiredProducts = retailProductRepository.findAll().stream()
                .filter(Product::isExpired)
                .toList();

        // Pending products
        expiredProducts.stream()
                .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                .filter(product -> product.getState() == Product.State.PENDING)
                .forEach(product -> {
                    product.setState(Product.State.EXPIRED);
                    updatePendingAndAcceptedOrderStatus(product.getOrders());
                });

        // Listing products
        expiredProducts.stream()
                .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                .filter(product -> product.getState() == Product.State.LISTING)
                .forEach(product -> {
                    product.setState(Product.State.EXPIRED);
                    updatePendingAndAcceptedOrderStatus(product.getOrders());
                });
        retailProductRepository.saveAll(expiredProducts);
    }

    @Override
    public List<RetailProduct> searchProductByCropName(String cropName) {
        return retailProductRepository.searchProductByCropName(cropName).stream()
                .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                .filter(product -> product.getState() == Product.State.LISTING)
                .toList();
    }

    @Override
    public double calculateOrderPrice(RetailProduct retailProduct, int userOrderQuantity) {
        return retailProduct.getPricePerUnit() * userOrderQuantity;
    }

    @Override
    public double calculateTotalPrice(RetailProduct retailProduct) {
        int availableQuantity = retailProduct.getAvailableQuantity();
        int quantityPerUnit = retailProduct.getQuantityPerUnit();
        double pricePerUnit = retailProduct.getPricePerUnit();

        int counter = 0;
        while (availableQuantity > 0) {
            if (availableQuantity <= quantityPerUnit) counter++;
            else if (availableQuantity % quantityPerUnit == 0) counter++;
            availableQuantity -= quantityPerUnit;
        }
        log.trace("Counter {}", counter);
        double totalPrice = counter * pricePerUnit;
        log.trace("Total price {}", totalPrice);
        return totalPrice;
    }
}