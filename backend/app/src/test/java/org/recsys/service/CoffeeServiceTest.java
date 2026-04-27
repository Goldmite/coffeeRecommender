package org.recsys.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.dto.coffee.CoffeeBeanRequest;
import org.recsys.dto.coffee.CoffeeVectorizationDto;
import org.recsys.mapper.CoffeeMapper;
import org.recsys.model.CoffeeBean;
import org.recsys.model.CoffeeFeatures;
import org.recsys.repository.CoffeeRepository;
import org.recsys.repository.ShopRepository;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;

@ExtendWith(MockitoExtension.class)
public class CoffeeServiceTest {

    @Mock
    private CoffeeRepository coffeeRepository;

    @Mock
    private CoffeeMapper mapper;

    @Mock
    private CoffeeVectorService vectorService;

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private CoffeeService coffeeService;

    @Test
    void shouldGetCoffeeById() throws NotFoundException {
        // given
        Long id = 1L;
        CoffeeBean mockBean = new CoffeeBean();
        when(coffeeRepository.findById(id)).thenReturn(Optional.of(mockBean));
        // when
        CoffeeBean foundBean = coffeeService.getCoffeeById(id);
        // then
        assertNotNull(foundBean);
        verify(coffeeRepository).findById(id);
    }

    @Test
    void shouldThrowNotFound_whenNoCoffeeFound() {
        // given
        when(coffeeRepository.findById(anyLong())).thenReturn(Optional.empty());
        // when, then
        assertThrows(NotFoundException.class, () -> coffeeService.getCoffeeById(404L));
    }

    @Test
    void shouldGetAllCoffeeList() {
        // given
        List<CoffeeBean> beans = List.of(new CoffeeBean(), new CoffeeBean());
        when(coffeeRepository.findAll()).thenReturn(beans);
        // when
        coffeeService.getAllCoffees();
        // then
        verify(coffeeRepository, times(1)).findAll();
    }

    @Test
    void shouldAddCoffeeWithFeatures() {
        // given
        CoffeeBeanRequest req = new CoffeeBeanRequest();
        CoffeeBean entity = new CoffeeBean();
        CoffeeFeatures features = new CoffeeFeatures();
        entity.setFeatures(features);
        when(mapper.toEntity(req)).thenReturn(entity);
        when(coffeeRepository.save(entity)).thenReturn(entity);
        // when
        coffeeService.addCoffee(req);
        // then
        assertEquals(entity, features.getCoffeeBean());
        verify(coffeeRepository).save(entity);
    }

    @Test
    void shouldUpdateCoffeeByRequest() throws NotFoundException {
        // given
        Long id = 1L;
        CoffeeBeanRequest req = new CoffeeBeanRequest();
        CoffeeBean existingBean = new CoffeeBean();
        when(coffeeRepository.findById(id)).thenReturn(Optional.of(existingBean));
        when(coffeeRepository.save(existingBean)).thenReturn(existingBean);
        // when
        coffeeService.updateCoffee(id, req);
        // then
        verify(mapper).updateEntityFromDto(req, existingBean);
        verify(coffeeRepository).save(existingBean);
    }

    @Test
    void shouldDeleteCoffeeById() {
        // given
        Long id = 2L;
        // when
        coffeeService.deleteCoffeeById(id);
        // then
        verify(coffeeRepository, times(1)).deleteById(id);
    }

    @Test
    void batchUpdateCoffeeVectors_ShouldUpdateAllBeans() {
        // given
        CoffeeBean bean1 = new CoffeeBean();
        bean1.setFeatures(new CoffeeFeatures());
        CoffeeBean bean2 = new CoffeeBean();
        bean2.setFeatures(new CoffeeFeatures());
        List<CoffeeBean> mockBeans = Arrays.asList(bean1, bean2);

        CoffeeVectorizationDto mockDto = CoffeeVectorizationDto.builder().build();
        float[] mockVector = { 0.1f, 0.2f, 0.3f };

        when(coffeeRepository.findAll()).thenReturn(mockBeans);
        when(mapper.toVectorDto(any(CoffeeBean.class))).thenReturn(mockDto);
        when(vectorService.createFlavorVector(mockDto)).thenReturn(mockVector);
        when(coffeeRepository.saveAll(anyList())).thenAnswer(i -> i.getArguments()[0]);
        // when
        List<CoffeeBean> result = coffeeService.batchUpdateCoffeeVectors();
        // then
        assertEquals(2, result.size());
        assertArrayEquals(mockVector, result.get(0).getFeatures().getFlavorVector());
        assertArrayEquals(mockVector, result.get(1).getFeatures().getFlavorVector());
        verify(coffeeRepository, times(1)).findAll();
        verify(mapper, times(2)).toVectorDto(any(CoffeeBean.class));
        verify(vectorService, times(2)).createFlavorVector(mockDto);
        verify(coffeeRepository, times(1)).saveAll(mockBeans);
    }

    @Test
    void batchUpdateCoffeeVectors_WhenNoBeansExist_ShouldReturnEmptyList() {
        // given
        when(coffeeRepository.findAll()).thenReturn(List.of());
        when(coffeeRepository.saveAll(anyList())).thenReturn(List.of());
        // when
        List<CoffeeBean> result = coffeeService.batchUpdateCoffeeVectors();
        // then
        assertTrue(result.isEmpty());
        verify(mapper, never()).toVectorDto(any());
        verify(vectorService, never()).createFlavorVector(any());
    }
}
