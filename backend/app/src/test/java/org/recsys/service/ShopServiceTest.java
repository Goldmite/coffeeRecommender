package org.recsys.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.dto.shop.ShopRequest;
import org.recsys.dto.shop.ShopUpdateRequest;
import org.recsys.model.Shop;
import org.recsys.repository.ShopRepository;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    ShopService shopService;

    private static final String MOCK_SHOPURL = "https://mock.shopurl.mock";

    @Test
    void getAllByActivity_shouldReturnShops() {
        // given
        List<Shop> mockShops = List.of(new Shop(1, "Shop A", MOCK_SHOPURL, true),
                new Shop(2, "Shop B", MOCK_SHOPURL, true));
        when(shopRepository.findAllByIsActive(true)).thenReturn(mockShops);
        // when
        List<Shop> res = shopService.getAllByActivity(true);
        // then
        assertEquals(2, res.size());
        verify(shopRepository, times(1)).findAllByIsActive(true);
    }

    @Test
    void createShops_shouldReturnSavedShops() {
        // given
        ShopRequest req = new ShopRequest("New shop", MOCK_SHOPURL, true);
        List<ShopRequest> requests = List.of(req);
        when(shopRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        // when
        List<Shop> res = shopService.createShops(requests);
        // then
        assertEquals(1, res.size());
        assertEquals(req.getName(), res.getFirst().getName());
        assertEquals(req.getIsActive(), res.getFirst().getIsActive());
        verify(shopRepository).saveAll(anyList());
    }

    @Test
    void updateShops_ShouldUpdateOnlyProvidedFields() {
        // given
        Integer existingId = 1;
        Shop existingShop = new Shop(existingId, "Cofee Sohp", MOCK_SHOPURL, false);
        ShopUpdateRequest updateReq = new ShopUpdateRequest(existingId, "Coffee Shop", null, true);
        when(shopRepository.findAllById(List.of(existingId))).thenReturn(List.of(existingShop));
        when(shopRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        // when
        List<Shop> result = shopService.updateShops(List.of(updateReq));
        // then
        Shop updated = result.getFirst();
        assertEquals("Coffee Shop", updated.getName());
        assertEquals(MOCK_SHOPURL, updated.getShopUrl());
        assertTrue(updated.getIsActive());
        verify(shopRepository).saveAll(anyList());
    }

    @Test
    void deleteShops_ShouldInvokeRepository() {
        // given
        List<Integer> ids = List.of(1, 2, 3);
        // when
        shopService.deleteShops(ids);
        // then
        verify(shopRepository, times(1)).deleteAllById(ids);
    }
}
