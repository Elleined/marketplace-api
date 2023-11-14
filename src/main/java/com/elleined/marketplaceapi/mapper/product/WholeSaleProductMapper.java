package com.elleined.marketplaceapi.mapper.product;

import com.elleined.marketplaceapi.dto.product.WholeSaleProductDTO;
import com.elleined.marketplaceapi.model.Crop;
import com.elleined.marketplaceapi.model.product.Product;
import com.elleined.marketplaceapi.model.product.WholeSaleProduct;
import com.elleined.marketplaceapi.model.unit.WholeSaleUnit;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.service.CropService;
import com.elleined.marketplaceapi.service.unit.WholeSaleUnitService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Mapper(componentModel = "spring",
        imports = {Product.State.class,
                Product.Status.class,
                Product.SaleStatus.class}
)
public interface WholeSaleProductMapper extends ProductMapper<WholeSaleProductDTO, WholeSaleProduct> {

    @Override
    @Mappings({
            @Mapping(target = "state", source = "wholeSaleProduct.state"),
            @Mapping(target = "sellerId", source = "wholeSaleProduct.seller.id"),
            @Mapping(target = "sellerName", expression = "java(wholeSaleProduct.getSeller().getFullName())"),
            @Mapping(target = "cropName", source = "wholeSaleProduct.crop.name"),
            @Mapping(target = "shopName", source = "wholeSaleProduct.seller.shop.name"),
            @Mapping(target = "listingDate", expression = "java(wholeSaleProduct.getListingDate().toLocalDate())"),
            @Mapping(target = "unitId", source = "wholeSaleProduct.wholeSaleUnit.id"),
            @Mapping(target = "unitName", source = "wholeSaleProduct.wholeSaleUnit.name"),
            @Mapping(target = "totalPrice", source = "price")
    })
    WholeSaleProductDTO toDTO(WholeSaleProduct wholeSaleProduct);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "picture", ignore = true),

            @Mapping(target = "listingDate", expression = "java(java.time.LocalDateTime.now())"),
            @Mapping(target = "state", expression = "java(State.PENDING)"),
            @Mapping(target = "saleStatus", expression = "java(SaleStatus.NOT_ON_SALE)"),
            @Mapping(target = "status", expression = "java(Status.ACTIVE)"),
            @Mapping(target = "crop", expression = "java(crop)"),
            @Mapping(target = "wholeSaleUnit", expression = "java(wholeSaleUnit)"),
            @Mapping(target = "seller", expression = "java(seller)"),
            @Mapping(target = "price", source = "totalPrice"),

            @Mapping(target = "privateChatRooms", expression = "java(new java.util.ArrayList<>())"),
            @Mapping(target = "wholeSaleCartItems", expression = "java(new java.util.ArrayList<>())"),
            @Mapping(target = "wholeSaleOrders", expression = "java(new java.util.ArrayList<>())"),
    })
    WholeSaleProduct toEntity(WholeSaleProductDTO dto,
                              @Context User seller,
                              @Context Crop crop,
                              @Context WholeSaleUnit wholeSaleUnit);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "listingDate", ignore = true),
            @Mapping(target = "state", ignore = true),
            @Mapping(target = "seller", ignore = true),
            @Mapping(target = "privateChatRooms", ignore = true),
            @Mapping(target = "wholeSaleCartItems", ignore = true),
            @Mapping(target = "wholeSaleOrders", ignore = true),
            @Mapping(target = "picture", ignore = true),
            @Mapping(target = "saleStatus", ignore = true),

            @Mapping(target = "wholeSaleUnit", expression = "java(wholeSaleUnit)"),
            @Mapping(target = "crop", expression = "java(crop)"),
            @Mapping(target = "price", source = "dto.totalPrice")
    })
    WholeSaleProduct toUpdate(@MappingTarget WholeSaleProduct wholeSaleProduct,
                              WholeSaleProductDTO dto,
                              @Context Crop crop,
                              @Context WholeSaleUnit wholeSaleUnit);
}
