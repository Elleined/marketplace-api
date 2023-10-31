package com.elleined.marketplaceapi.dto.address;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private int id;

    @NotBlank(message = "Please provide a value for the Address details")
    private String details;

    @NotBlank(message = "Please provide a value for the Region")
    private String regionName;

    @NotBlank(message = "Please provide a value for the Province")
    private String provinceName;

    @NotBlank(message = "Please provide a value for the City")
    private String cityName;

    @NotBlank(message = "Please provide a value for the Baranggay")
    private String baranggayName;
}
