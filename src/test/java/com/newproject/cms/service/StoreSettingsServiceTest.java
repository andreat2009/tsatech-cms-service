package com.newproject.cms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.newproject.cms.domain.StoreSettings;
import com.newproject.cms.dto.StoreSettingsRequest;
import com.newproject.cms.dto.StoreSettingsResponse;
import com.newproject.cms.repository.StoreSettingsRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StoreSettingsServiceTest {

    @Mock
    private StoreSettingsRepository repository;

    @InjectMocks
    private StoreSettingsService service;

    private StoreSettings settings;

    @BeforeEach
    void setUp() {
        settings = new StoreSettings();
        settings.setId(1L);
        settings.setSiteName("TSATech Store");
        settings.setLogoMaxHeightPx(96);
        settings.setSiteNameFontSizePx(28);
        when(repository.findById(1L)).thenReturn(Optional.of(settings));
        when(repository.save(any(StoreSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void updateAcceptsLargeLogoHeightsWithinNewRange() {
        StoreSettingsRequest request = new StoreSettingsRequest();
        request.setLogoMaxHeightPx(320);

        StoreSettingsResponse response = service.update(request);

        assertThat(response.getLogoMaxHeightPx()).isEqualTo(320);
    }

    @Test
    void updateClampsLogoHeightsAboveMaximum() {
        StoreSettingsRequest request = new StoreSettingsRequest();
        request.setLogoMaxHeightPx(500);

        StoreSettingsResponse response = service.update(request);

        assertThat(response.getLogoMaxHeightPx()).isEqualTo(360);
    }
}
