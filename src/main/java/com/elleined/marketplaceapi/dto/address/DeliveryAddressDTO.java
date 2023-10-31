package com.elleined.marketplaceapi.dto.address;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class DeliveryAddressDTO extends AddressDTO {

    @NotBlank(message = "Please provide a value for the title!")
    private String title;

    @Builder(builderMethodName = "deliveryAddressBuilder")
    public DeliveryAddressDTO(int id, @NotBlank(message = "Details cannot be null, empty, or blank") String details, @NotBlank(message = "Region name cannot be null, empty, or blank") String regionName, @NotBlank(message = "Province name cannot be null, empty, or blank") String provinceName, @NotBlank(message = "City name cannot be null, empty, or blank") String cityName, @NotBlank(message = "Baranggay name cannot be null, empty, or blank") String baranggayName, String title) {
        super(id, details, regionName, provinceName, cityName, baranggayName);
        this.title = title;
    }
}
