package com.elleined.marketplaceapi.service.address;

import com.elleined.marketplaceapi.dto.address.AddressDTO;
import com.elleined.marketplaceapi.dto.address.DeliveryAddressDTO;
import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.exception.user.DeliveryAddressLimitException;
import com.elleined.marketplaceapi.mapper.AddressMapper;
import com.elleined.marketplaceapi.model.address.DeliveryAddress;
import com.elleined.marketplaceapi.model.address.UserAddress;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Override
    public void saveUserAddress(User registeringUser, AddressDTO addressDTO) {
        UserAddress userAddress = addressMapper.toUserAddressEntity(addressDTO, registeringUser);
        registeringUser.setAddress(userAddress);
        addressRepository.save(userAddress);
        log.debug("User with id of {} successfully registered with address id of {}", registeringUser.getId(), userAddress.getId());
    }

    @Override
    public DeliveryAddress saveDeliveryAddress(User orderingUser, DeliveryAddressDTO deliveryAddressDTO) throws DeliveryAddressLimitException {
        if (orderingUser.hasReachedDeliveryAddressLimit())
            throw new DeliveryAddressLimitException("Cannot save another delivery address! Because you have reached the maximum limit of " + DELIVERY_ADDRESS_LIMIT + " delivery addresses. To add a new address, you must first remove one of your existing addresses.");
        DeliveryAddress deliveryAddress = addressMapper.toDeliveryAddressEntity(deliveryAddressDTO, orderingUser);
        orderingUser.getDeliveryAddresses().add(deliveryAddress);
        addressRepository.save(deliveryAddress);
        log.debug("User with id of {} successfully saved delivery address with id of {}", orderingUser.getId(), deliveryAddress.getId());
        return deliveryAddress;
    }

    @Override
    public List<DeliveryAddress> getAllDeliveryAddress(User currentUser) {
        return currentUser.getDeliveryAddresses();
    }

    @Override
    public DeliveryAddress getDeliveryAddressById(User user, int deliveryAddressId) throws ResourceNotFoundException {
        return user.getDeliveryAddresses().stream()
                .filter(deliveryAddress -> deliveryAddress.getId() == deliveryAddressId)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cannot save another delivery address because you already reached the delivery address limit which is " + DELIVERY_ADDRESS_LIMIT));
    }
}
