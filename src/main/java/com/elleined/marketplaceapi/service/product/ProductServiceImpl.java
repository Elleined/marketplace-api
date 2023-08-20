package com.elleined.marketplaceapi.service.product;

import com.elleined.marketplaceapi.dto.ProductDTO;
import com.elleined.marketplaceapi.exception.ResourceNotFoundException;
import com.elleined.marketplaceapi.mapper.ProductMapper;
import com.elleined.marketplaceapi.model.Product;
import com.elleined.marketplaceapi.model.user.VerifiedUser;
import com.elleined.marketplaceapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public void delete(Product product) {
        product.setStatus(Product.Status.INACTIVE);
        productRepository.save(product);

        log.debug("Product with id of {} are now inactive", product.getId());
    }

    @Override
    public void delete(int id) throws ResourceNotFoundException {
        Product product = getById(id);
        product.setStatus(Product.Status.INACTIVE);
        productRepository.save(product);

        log.debug("Product with id of {} are now inactive", product.getId());
    }


    @Override
    public Product getById(int id) throws ResourceNotFoundException {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product with id of " + id + " does not exists!"));
    }

    @Override
    public boolean isExists(int id) {
        return productRepository.existsById(id);
    }


    @Override
    public Product saveByDTO(ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);
        productRepository.save(product);
        log.debug("Product saved successfully with id of {}", product.getId());
        return product;
    }

    @Override
    public Product save(Product product) {
        productRepository.save(product);
        log.debug("Product saved successfully with id of {}", product.getId());
        return product;
    }

    @Override
    public void update(Product product, ProductDTO productDTO) throws ResourceNotFoundException {
        Product updatedProduct = productMapper.toUpdate(product, productDTO);
        productRepository.save(updatedProduct);
        log.debug("Product with id of {} updated successfully!", updatedProduct.getId());
    }


    @Override
    public List<Product> getAllExcept(VerifiedUser verifiedUser) {
        List<Product> userProducts = verifiedUser.getProducts();

        List<Product> products = new ArrayList<>(productRepository.findAll().stream()
                .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                .filter(product -> product.getState() == Product.State.LISTING)
                .toList());
        products.removeAll(userProducts);

        return products;
    }

    @Override
    public List<Product> getAllProductByState(VerifiedUser verifiedUser, com.elleined.marketplaceapi.model.Product.State state) {
        return verifiedUser.getProducts().stream()
                .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                .filter(product -> product.getState() == state)
                .toList();
    }
}
