package com.elleined.marketplaceapi.service.cart.retail;

import com.elleined.marketplaceapi.dto.cart.RetailCartItemDTO;
import com.elleined.marketplaceapi.exception.order.OrderQuantiantyExceedsException;
import com.elleined.marketplaceapi.exception.product.*;
import com.elleined.marketplaceapi.exception.resource.AlreadyExistException;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.exception.resource.ResourceOwnedException;
import com.elleined.marketplaceapi.exception.user.buyer.BuyerAlreadyRejectedException;
import com.elleined.marketplaceapi.mapper.cart.RetailCartItemMapper;
import com.elleined.marketplaceapi.model.Crop;
import com.elleined.marketplaceapi.model.address.DeliveryAddress;
import com.elleined.marketplaceapi.model.cart.RetailCartItem;
import com.elleined.marketplaceapi.model.order.RetailOrder;
import com.elleined.marketplaceapi.model.product.Product;
import com.elleined.marketplaceapi.model.product.Product.State;
import com.elleined.marketplaceapi.model.product.RetailProduct;
import com.elleined.marketplaceapi.model.unit.RetailUnit;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.cart.RetailCartItemRepository;
import com.elleined.marketplaceapi.repository.order.RetailOrderRepository;
import com.elleined.marketplaceapi.service.address.AddressService;
import com.elleined.marketplaceapi.service.product.retail.RetailProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.elleined.marketplaceapi.model.order.Order.Status.*;
import static com.elleined.marketplaceapi.model.product.Product.Status.ACTIVE;
import static com.elleined.marketplaceapi.model.product.Product.Status.INACTIVE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetailCartItemServiceImplTest {
    @Mock
    private RetailProductServiceImpl retailProductService;

    @Mock
    private RetailCartItemMapper retailCartItemMapper;
    @Mock
    private RetailCartItemRepository retailCartItemRepository;

    @Mock
    private RetailOrderRepository retailOrderRepository;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private RetailCartItemServiceImpl retailCartItemService;

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "SOLD", "REJECTED", "EXPIRED"})
    void getAll(String productStatus) {
        User user = new User();

        RetailCartItem activeAndListed = RetailCartItem.retailCartItemBuilder()
                .retailProduct(RetailProduct.retailProductBuilder()
                        .id(1)
                        .status(Product.Status.ACTIVE)
                        .state(State.LISTING)
                        .build())
                .build();

        RetailCartItem activeAndNotListed = RetailCartItem.retailCartItemBuilder()
                .retailProduct(RetailProduct.retailProductBuilder()
                        .id(1)
                        .state(State.valueOf(productStatus))
                        .status(Product.Status.ACTIVE)
                        .build())
                .build();

        RetailCartItem inActiveAndNotListed = RetailCartItem.retailCartItemBuilder()
                .retailProduct(RetailProduct.retailProductBuilder()
                        .id(1)
                        .state(State.valueOf(productStatus))
                        .status(INACTIVE)
                        .build())
                .build();
        List<RetailCartItem> rawRetailCartItems = Arrays.asList(activeAndListed, activeAndNotListed, inActiveAndNotListed);
        user.setRetailCartItems(rawRetailCartItems);

        List<RetailCartItem> actual = retailCartItemService.getAll(user);
        List<RetailCartItem> expected = Collections.singletonList(activeAndListed);

        assertEquals(expected.size(), actual.size());
        assertIterableEquals(expected, actual);
    }

    @Test
    void delete() {
        User user = User.builder()
                .retailCartItems(new ArrayList<>())
                .build();
        RetailCartItem retailCartItem = RetailCartItem.retailCartItemBuilder()
                .retailProduct(RetailProduct.retailProductBuilder()
                        .id(1)
                        .build())
                .build();
        user.getRetailCartItems().add(retailCartItem);

        doAnswer(i -> user.getRetailCartItems().remove(retailCartItem))
                .when(retailCartItemRepository)
                .delete(retailCartItem);

        retailCartItemService.delete(retailCartItem);
        List<RetailCartItem> actual = user.getRetailCartItems();
        List<RetailCartItem> expected = Collections.emptyList();

        verify(retailCartItemRepository).delete(retailCartItem);
        assertIterableEquals(expected, actual);
    }

    @Test
    void shouldThrowProductAlreadyInCartException() {
        User user = User.builder()
                .retailCartItems(new ArrayList<>())
                .build();

        RetailCartItemDTO retailCartItemDTO = RetailCartItemDTO.retailCartItemDTOBuilder()
                .productId(1)
                .build();

        RetailProduct retailProduct = mock(RetailProduct.class);
        retailProduct.setId(1);

        RetailCartItem retailCartItem = RetailCartItem.retailCartItemBuilder()
                .retailProduct(retailProduct)
                .build();
        user.getRetailCartItems().add(retailCartItem);

        when(retailProductService.getById(1)).thenReturn(retailProduct);

        assertThrows(AlreadyExistException.class, () -> retailCartItemService.save(user, retailCartItemDTO));
        verifyNoInteractions(retailCartItemMapper, retailCartItemRepository, retailOrderRepository);
    }

    @Test
    void shouldThrowProductExpiredException() {
        User user = spy(User.class);
        user.setRetailProducts(new ArrayList<>());
        user.setRetailCartItems(new ArrayList<>());
        user.setRetailOrders(new ArrayList<>());

        RetailCartItemDTO retailCartItemDTO = RetailCartItemDTO.retailCartItemDTOBuilder()
                .productId(1)
                .build();

        RetailProduct expiredRetailProduct = RetailProduct.retailProductBuilder()
                .id(1)
                .expirationDate(LocalDate.now().minusDays(1))
                .build();

        RetailCartItem retailCartItem = RetailCartItem.retailCartItemBuilder()
                .retailProduct(expiredRetailProduct)
                .build();

        when(retailProductService.getById(1)).thenReturn(expiredRetailProduct);
        when(user.isProductAlreadyInCart(expiredRetailProduct)).thenReturn(false);

        assertThrows(ProductExpiredException.class, () -> retailCartItemService.save(user, retailCartItemDTO));
        assertThrows(ProductExpiredException.class, () -> retailCartItemService.orderCartItem(user, retailCartItem));
        verifyNoInteractions(retailCartItemMapper, retailCartItemRepository, retailOrderRepository);
    }

    @Test
    void shouldThrowProductHasPendingOrderException() {
        User user = spy(User.class);
        user.setRetailCartItems(new ArrayList<>());

        RetailProduct retailProduct = mock(RetailProduct.class);
        retailProduct.setId(1);

        RetailCartItem retailCartItem = RetailCartItem.retailCartItemBuilder()
                .retailProduct(retailProduct)
                .build();

        RetailCartItemDTO retailCartItemDTO = RetailCartItemDTO.retailCartItemDTOBuilder()
                .productId(1)
                .build();

        List<RetailOrder> retailOrders = Arrays.asList(
                RetailOrder.retailOrderBuilder()
                        .retailProduct(retailProduct)
                        .status(PENDING)
                        .build(),
                RetailOrder.retailOrderBuilder()
                        .retailProduct(retailProduct)
                        .status(SOLD)
                        .build(),
                RetailOrder.retailOrderBuilder()
                        .retailProduct(retailProduct)
                        .status(CANCELLED)
                        .build(),
                RetailOrder.retailOrderBuilder()
                        .retailProduct(retailProduct)
                        .status(REJECTED)
                        .build()
        );
        when(user.getRetailOrders()).thenReturn(retailOrders);

        when(retailProductService.getById(1)).thenReturn(retailProduct);
        when(user.isProductAlreadyInCart(retailProduct)).thenReturn(false);
        when(retailProduct.isExpired()).thenReturn(false);

        assertThrowsExactly(ProductHasPendingOrderException.class, () -> retailCartItemService.save(user, retailCartItemDTO));
        assertThrows(ProductHasPendingOrderException.class, () -> retailCartItemService.orderCartItem(user, retailCartItem));
        verifyNoInteractions(retailCartItemMapper, retailCartItemRepository, retailOrderRepository);
    }

    @Test
    void shouldThrowProductHasAcceptedOrderException() {
        User user = spy(User.class);
        user.setRetailCartItems(new ArrayList<>());

        RetailProduct retailProduct = mock(RetailProduct.class);
        retailProduct.setId(1);

        RetailCartItem retailCartItem = RetailCartItem.retailCartItemBuilder()
                .retailProduct(retailProduct)
                .build();

        RetailCartItemDTO retailCartItemDTO = RetailCartItemDTO.retailCartItemDTOBuilder()
                .productId(1)
                .build();

        List<RetailOrder> retailOrders = Arrays.asList(
                RetailOrder.retailOrderBuilder()
                        .retailProduct(retailProduct)
                        .status(ACCEPTED)
                        .build(),
                RetailOrder.retailOrderBuilder()
                        .retailProduct(retailProduct)
                        .status(SOLD)
                        .build(),
                RetailOrder.retailOrderBuilder()
                        .retailProduct(retailProduct)
                        .status(CANCELLED)
                        .build(),
                RetailOrder.retailOrderBuilder()
                        .retailProduct(retailProduct)
                        .status(REJECTED)
                        .build()
        );
        when(user.getRetailOrders()).thenReturn(retailOrders);

        when(retailProductService.getById(1)).thenReturn(retailProduct);
        when(retailProduct.isExpired()).thenReturn(false);
        when(user.isProductAlreadyInCart(retailProduct)).thenReturn(false);
        when(user.hasOrder(retailProduct, PENDING)).thenReturn(false);

        assertThrowsExactly(ProductHasAcceptedOrderException.class, () -> retailCartItemService.save(user, retailCartItemDTO));
        assertThrowsExactly(ProductHasAcceptedOrderException.class, () -> retailCartItemService.orderCartItem(user, retailCartItem));
        verifyNoInteractions(retailCartItemMapper, retailCartItemRepository, retailOrderRepository);
    }

    @Test
    void shouldThrowResourceOwnedException() {
        User user = spy(User.class);
        user.setRetailProducts(new ArrayList<>());
        user.setRetailCartItems(new ArrayList<>());
        user.setRetailOrders(new ArrayList<>());

        RetailProduct retailProduct = spy(RetailProduct.class);
        retailProduct.setId(1);
        retailProduct.setExpirationDate(LocalDate.now().plusDays(10));
        retailProduct.setStatus(INACTIVE);
        user.getRetailProducts().add(retailProduct);

        RetailCartItem retailCartItem = RetailCartItem.retailCartItemBuilder()
                .retailProduct(retailProduct)
                .build();

        RetailCartItemDTO retailCartItemDTO = RetailCartItemDTO.retailCartItemDTOBuilder()
                .productId(1)
                .build();

        when(retailProductService.getById(1)).thenReturn(retailProduct);
        when(retailProduct.isExpired()).thenReturn(false);
        when(user.isProductAlreadyInCart(retailProduct)).thenReturn(false);
        when(user.hasOrder(retailProduct, PENDING)).thenReturn(false);
        when(user.hasOrder(retailProduct, ACCEPTED)).thenReturn(false);

        assertThrowsExactly(ResourceOwnedException.class, () -> retailCartItemService.save(user, retailCartItemDTO));
        assertThrowsExactly(ResourceOwnedException.class, () -> retailCartItemService.orderCartItem(user, retailCartItem));
        verifyNoInteractions(retailCartItemMapper, retailCartItemRepository, retailOrderRepository);
    }

    @Test
    void shouldThrowProductDeletedException() {
        User user = spy(User.class);
        user.setRetailProducts(new ArrayList<>());
        user.setRetailCartItems(new ArrayList<>());
        user.setRetailOrders(new ArrayList<>());

        RetailProduct retailProduct = spy(RetailProduct.class);
        retailProduct.setId(1);
        retailProduct.setExpirationDate(LocalDate.now().plusDays(10));
        retailProduct.setStatus(INACTIVE);

        RetailCartItem retailCartItem = RetailCartItem.retailCartItemBuilder()
                .retailProduct(retailProduct)
                .build();

        RetailCartItemDTO retailCartItemDTO = RetailCartItemDTO.retailCartItemDTOBuilder()
                .productId(1)
                .build();

        when(retailProductService.getById(1)).thenReturn(retailProduct);
        when(retailProduct.isExpired()).thenReturn(false);
        when(user.isProductAlreadyInCart(retailProduct)).thenReturn(false);
        when(user.hasOrder(retailProduct, PENDING)).thenReturn(false);
        when(user.hasOrder(retailProduct, ACCEPTED)).thenReturn(false);
        when(user.hasProduct(retailProduct)).thenReturn(false);

        assertThrowsExactly(ResourceNotFoundException.class, () -> retailCartItemService.save(user, retailCartItemDTO));
        assertThrowsExactly(ResourceNotFoundException.class, () -> retailCartItemService.orderCartItem(user, retailCartItem));
        verifyNoInteractions(retailCartItemMapper, retailCartItemRepository, retailOrderRepository);
    }

    @Test
    void shouldThrowProductAlreadySoldException() {
        User user = spy(User.class);
        user.setRetailProducts(new ArrayList<>());
        user.setRetailCartItems(new ArrayList<>());
        user.setRetailOrders(new ArrayList<>());

        RetailProduct retailProduct = spy(RetailProduct.class);
        retailProduct.setId(1);
        retailProduct.setState(State.SOLD);
        retailProduct.setExpirationDate(LocalDate.now().plusDays(10));

        RetailCartItem retailCartItem = RetailCartItem.retailCartItemBuilder()
                .retailProduct(retailProduct)
                .build();

        RetailCartItemDTO retailCartItemDTO = RetailCartItemDTO.retailCartItemDTOBuilder()
                .productId(1)
                .build();

        when(retailProductService.getById(1)).thenReturn(retailProduct);
        when(retailProduct.isExpired()).thenReturn(false);
        when(user.isProductAlreadyInCart(retailProduct)).thenReturn(false);
        when(user.hasOrder(retailProduct, PENDING)).thenReturn(false);
        when(user.hasOrder(retailProduct, ACCEPTED)).thenReturn(false);
        when(user.hasProduct(retailProduct)).thenReturn(false);
        when(retailProduct.isDeleted()).thenReturn(false);

        assertThrowsExactly(ProductAlreadySoldException.class, () -> retailCartItemService.save(user, retailCartItemDTO));
        assertThrowsExactly(ProductAlreadySoldException.class, () -> retailCartItemService.orderCartItem(user, retailCartItem));
        verifyNoInteractions(retailCartItemMapper, retailCartItemRepository, retailOrderRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SOLD", "PENDING", "REJECTED", "EXPIRED"})
    void shouldThrowProductNotListedException(String productState) {
        User user = spy(User.class);
        user.setRetailProducts(new ArrayList<>());
        user.setRetailCartItems(new ArrayList<>());
        user.setRetailOrders(new ArrayList<>());

        RetailProduct retailProduct = spy(RetailProduct.class);
        retailProduct.setId(1);
        retailProduct.setState(State.valueOf(productState));
        retailProduct.setExpirationDate(LocalDate.now().plusDays(10));

        RetailCartItem retailCartItem = RetailCartItem.retailCartItemBuilder()
                .retailProduct(retailProduct)
                .build();

        RetailCartItemDTO retailCartItemDTO = RetailCartItemDTO.retailCartItemDTOBuilder()
                .productId(1)
                .build();

        when(retailProductService.getById(1)).thenReturn(retailProduct);
        when(retailProduct.isExpired()).thenReturn(false);
        when(user.isProductAlreadyInCart(retailProduct)).thenReturn(false);
        when(user.hasOrder(retailProduct, PENDING)).thenReturn(false);
        when(user.hasOrder(retailProduct, ACCEPTED)).thenReturn(false);
        when(user.hasProduct(retailProduct)).thenReturn(false);
        when(retailProduct.isDeleted()).thenReturn(false);
        when(retailProduct.isSold()).thenReturn(false);

        assertThrowsExactly(ProductNotListedException.class, () -> retailCartItemService.save(user, retailCartItemDTO));
        assertThrowsExactly(ProductNotListedException.class, () -> retailCartItemService.orderCartItem(user, retailCartItem));

        verifyNoInteractions(retailCartItemMapper, retailCartItemRepository, retailOrderRepository);
    }

    @Test
    void shouldThrowOrderQuantiantyExceedsException() {
        User user = spy(User.class);
        user.setRetailProducts(new ArrayList<>());
        user.setRetailCartItems(new ArrayList<>());
        user.setRetailOrders(new ArrayList<>());

        RetailProduct retailProduct = spy(RetailProduct.class);
        retailProduct.setId(1);
        retailProduct.setAvailableQuantity(10);
        retailProduct.setState(State.REJECTED);// not listed
        retailProduct.setExpirationDate(LocalDate.now().plusDays(10));

        RetailCartItem retailCartItem = RetailCartItem.retailCartItemBuilder()
                .retailProduct(retailProduct)
                .orderQuantity(20)
                .build();

        RetailCartItemDTO retailCartItemDTO = RetailCartItemDTO.retailCartItemDTOBuilder()
                .productId(1)
                .orderQuantity(20)
                .build();

        when(retailProductService.getById(1)).thenReturn(retailProduct);
        when(retailProduct.isExpired()).thenReturn(false);
        when(user.isProductAlreadyInCart(retailProduct)).thenReturn(false);
        when(user.hasOrder(retailProduct, PENDING)).thenReturn(false);
        when(user.hasOrder(retailProduct, ACCEPTED)).thenReturn(false);
        when(user.hasProduct(retailProduct)).thenReturn(false);
        when(retailProduct.isDeleted()).thenReturn(false);
        when(retailProduct.isSold()).thenReturn(false);
        when(retailProduct.isListed()).thenReturn(true);

        assertThrowsExactly(OrderQuantiantyExceedsException.class, () -> retailCartItemService.save(user, retailCartItemDTO));
        assertThrowsExactly(OrderQuantiantyExceedsException.class, () -> retailCartItemService.orderCartItem(user, retailCartItem));

        verifyNoInteractions(retailCartItemMapper, retailCartItemRepository, retailOrderRepository);
    }

    @Test
    void shouldThrowBuyerAlreadyRejectedException() {
        User user = spy(User.class);
        user.setRetailProducts(new ArrayList<>());
        user.setRetailCartItems(new ArrayList<>());
        user.setRetailOrders(new ArrayList<>());

        RetailProduct retailProduct = spy(RetailProduct.class);
        retailProduct.setId(1);
        retailProduct.setRetailOrders(new ArrayList<>());
        retailProduct.setExpirationDate(LocalDate.now().plusDays(10));

        RetailCartItem retailCartItem = RetailCartItem.retailCartItemBuilder()
                .retailProduct(retailProduct)
                .build();

        RetailCartItemDTO retailCartItemDTO = RetailCartItemDTO.retailCartItemDTOBuilder()
                .productId(1)
                .build();

        RetailOrder retailOrder = RetailOrder.retailOrderBuilder()
                .retailProduct(retailProduct)
                .orderDate(LocalDateTime.now())
                .status(REJECTED)
                .updatedAt(LocalDateTime.now())
                .build();
        user.getRetailOrders().add(retailOrder);
        retailProduct.getRetailOrders().add(retailOrder);

        when(retailProductService.getById(1)).thenReturn(retailProduct);
        when(retailProduct.isExpired()).thenReturn(false);
        when(user.isProductAlreadyInCart(retailProduct)).thenReturn(false);
        when(user.hasOrder(retailProduct, PENDING)).thenReturn(false);
        when(user.hasOrder(retailProduct, ACCEPTED)).thenReturn(false);
        when(user.hasProduct(retailProduct)).thenReturn(false);
        when(retailProduct.isDeleted()).thenReturn(false);
        when(retailProduct.isSold()).thenReturn(false);
        when(retailProduct.isListed()).thenReturn(true);
        when(retailProduct.isExceedingToAvailableQuantity(retailCartItemDTO.getOrderQuantity())).thenReturn(false);
        when(retailProductService.isRejectedBySeller(user, retailProduct)).thenCallRealMethod();

        assertThrowsExactly(BuyerAlreadyRejectedException.class, () -> retailCartItemService.save(user, retailCartItemDTO));
        assertThrowsExactly(BuyerAlreadyRejectedException.class, () -> retailCartItemService.orderCartItem(user, retailCartItem));

        verify(retailProductService, atMost(2)).isRejectedBySeller(user, retailProduct);
        verifyNoInteractions(retailCartItemMapper, retailCartItemRepository, retailOrderRepository);
    }


    @Test
    void save() {
        User user = new User();
        user.setRetailProducts(new ArrayList<>());
        user.setRetailCartItems(new ArrayList<>());
        user.setRetailOrders(new ArrayList<>());
        user.setDeliveryAddresses(new ArrayList<>());

        RetailProduct retailProduct = RetailProduct.retailProductBuilder()
                .id(1)
                .expirationDate(LocalDate.now().plusDays(20))
                .status(ACTIVE)
                .state(State.LISTING)
                .availableQuantity(100)
                .pricePerUnit(20)
                .quantityPerUnit(5)
                .crop(Crop.builder()
                        .name("Crop")
                        .build())
                .retailUnit(RetailUnit.retailUnitBuilder()
                        .name("Retail unit")
                        .build())
                .picture("Picture")
                .build();

        RetailCartItemDTO dto = RetailCartItemDTO.retailCartItemDTOBuilder()
                .productId(1)
                .orderQuantity(20)
                .deliveryAddressId(1)
                .build();

        RetailCartItem retailCartItem = new RetailCartItem();

        DeliveryAddress deliveryAddress = DeliveryAddress.deliveryAddressBuilder()
                .id(1)
                .build();
        user.getDeliveryAddresses().add(deliveryAddress);

        double price = 50_000.00;
        when(retailProductService.getById(dto.getProductId())).thenReturn(retailProduct);
        when(retailProductService.calculateOrderPrice(retailProduct, dto.getOrderQuantity())).thenReturn(price);
        when(addressService.getDeliveryAddressById(user, dto.getDeliveryAddressId())).thenReturn(deliveryAddress);
        when(retailCartItemMapper.toEntity(dto, user, deliveryAddress, price, retailProduct)).thenReturn(retailCartItem);
        when(retailCartItemRepository.save(retailCartItem)).thenReturn(retailCartItem);

        retailCartItemService.save(user, dto);

        verify(retailProductService).isRejectedBySeller(user, retailProduct);
        verify(retailProductService).calculateOrderPrice(retailProduct, dto.getOrderQuantity());
        verify(addressService).getDeliveryAddressById(user, dto.getDeliveryAddressId());
        verify(retailCartItemMapper).toEntity(dto, user, deliveryAddress, price, retailProduct);
        verify(retailCartItemRepository).save(retailCartItem);
        assertDoesNotThrow(() -> retailCartItemService.save(user, dto));
    }

    @Test
    void orderCartItem() {
        User user = new User();
        user.setRetailProducts(new ArrayList<>());
        user.setRetailCartItems(new ArrayList<>());
        user.setRetailOrders(new ArrayList<>());

        RetailProduct retailProduct = RetailProduct.retailProductBuilder()
                .id(1)
                .expirationDate(LocalDate.now().plusDays(20))
                .status(ACTIVE)
                .state(State.LISTING)
                .availableQuantity(100)
                .pricePerUnit(20)
                .quantityPerUnit(5)
                .crop(Crop.builder()
                        .name("Crop")
                        .build())
                .retailUnit(RetailUnit.retailUnitBuilder()
                        .name("Retail unit")
                        .build())
                .picture("Picture")
                .build();

        RetailCartItem retailCartItem = RetailCartItem.retailCartItemBuilder()
                .id(1)
                .retailProduct(retailProduct)
                .build();
        user.getRetailCartItems().add(retailCartItem);

        RetailOrder retailOrder = new RetailOrder();

        when(retailCartItemMapper.cartItemToOrder(retailCartItem)).thenReturn(retailOrder);
        doAnswer(i -> user.getRetailCartItems().remove(retailCartItem))
                .when(retailCartItemRepository)
                .delete(retailCartItem);
        when(retailOrderRepository.save(retailOrder)).thenReturn(retailOrder);

        retailCartItemService.orderCartItem(user, retailCartItem);

        assertFalse(user.getRetailCartItems().contains(retailCartItem));
        verify(retailCartItemMapper).cartItemToOrder(retailCartItem);
        verify(retailCartItemRepository).delete(retailCartItem);
        verify(retailOrderRepository).save(retailOrder);
        assertDoesNotThrow(() -> retailCartItemService.orderCartItem(user, retailCartItem));
    }

    @Test
    void getByProduct() {
        User user = new User();

        RetailProduct retailProduct1 = RetailProduct.retailProductBuilder()
                .id(1)
                .build();

        RetailProduct retailProduct2 = RetailProduct.retailProductBuilder()
                .id(2)
                .build();

        RetailCartItem expected = RetailCartItem.retailCartItemBuilder()
                .id(1)
                .retailProduct(retailProduct1)
                .build();
        List<RetailCartItem> retailCartItems = Arrays.asList(
                expected,
                RetailCartItem.retailCartItemBuilder()
                        .id(2)
                        .retailProduct(retailProduct2)
                        .build()
        );
        user.setRetailCartItems(retailCartItems);

        RetailCartItem actual = retailCartItemService.getByProduct(user, retailProduct1);

        assertEquals(expected, actual);
        assertDoesNotThrow(() -> retailCartItemService.getByProduct(user, retailProduct1));
    }

}