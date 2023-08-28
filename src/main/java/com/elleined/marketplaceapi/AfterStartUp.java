package com.elleined.marketplaceapi;

import com.elleined.marketplaceapi.model.AppWallet;
import com.elleined.marketplaceapi.populator.Populator;
import com.elleined.marketplaceapi.repository.AppWalletRepository;
import com.elleined.marketplaceapi.repository.CropRepository;
import com.elleined.marketplaceapi.service.moderator.ModeratorService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;

@Transactional
@Component
@Slf4j
public class AfterStartUp {

    private final Populator cropPopulator;
    private final Populator unitPopulator;

    private final CropRepository cropRepository;
    private final AppWalletRepository appWalletRepository;
    private final ModeratorService moderatorService;

    public AfterStartUp(Populator cropPopulator,
                        @Qualifier("unitPopulator") Populator unitPopulator,
                        CropRepository cropRepository,
                        AppWalletRepository appWalletRepository, ModeratorService moderatorService) {
        this.cropPopulator = cropPopulator;
        this.unitPopulator = unitPopulator;
        this.cropRepository = cropRepository;
        this.appWalletRepository = appWalletRepository;
        this.moderatorService = moderatorService;
    }

    private final static String CROPS_JSON = "/json/crops.json";
    private final static String UNITS_JSON = "/json/units.json";

    @PostConstruct
    public void init() throws IOException {
        if (cropRepository.existsById(1)) {
            log.debug("Returning... because initial values of crops and units are already saved!");
            return;
        }
        log.debug("Saving crops and units initial values! Please wait....");
        cropPopulator.populate(CROPS_JSON);
        unitPopulator.populate(UNITS_JSON);
        log.debug("Saving crops and units are successful. Thank you!...");

        AppWallet appWallet = AppWallet.builder()
                .appWalletBalance(new BigDecimal(0))
                .build();
        appWalletRepository.save(appWallet);
        log.debug("Saving app wallet success...");

        moderatorService.save(1, "Sample moderator name", "sampleModeratorEmail@gmail.com", "sampleModeratorPassword");
    }
}
