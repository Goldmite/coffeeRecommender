package org.recsys.service;

import java.util.List;

import org.recsys.dto.coffee.CoffeeBeanRequest;
import org.recsys.dto.coffee.CoffeeVectorizationDto;
import org.recsys.mapper.CoffeeMapper;
import org.recsys.model.CoffeeBean;
import org.recsys.model.Shop;
import org.recsys.repository.CoffeeRepository;
import org.recsys.repository.ShopRepository;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoffeeService {

    private final CoffeeRepository coffeeRepository;
    private final CoffeeMapper mapper;
    private final CoffeeVectorService vectorService;
    private final ShopRepository shopRepository;

    public CoffeeBean getCoffeeById(Long id) throws NotFoundException {
        return coffeeRepository.findById(id).orElseThrow(() -> new NotFoundException());
    }

    public List<CoffeeBean> getAllCoffees() {
        return coffeeRepository.findAll();
    }

    @Transactional
    public CoffeeBean addCoffee(CoffeeBeanRequest req) {
        CoffeeBean newCoffee = mapper.toEntity(req);

        Shop shopProxy = shopRepository.getReferenceById(req.getShopId());
        newCoffee.setShop(shopProxy);

        if (newCoffee.getFeatures() != null) {
            newCoffee.getFeatures().setCoffeeBean(newCoffee);
        }

        return coffeeRepository.save(newCoffee);
    }

    @Transactional
    public CoffeeBean updateCoffee(Long id, CoffeeBeanRequest req) throws NotFoundException {
        CoffeeBean bean = coffeeRepository.findById(id).orElseThrow(() -> new NotFoundException());

        mapper.updateEntityFromDto(req, bean);

        if (req.getShopId() != null) {
            Shop shopProxy = shopRepository.getReferenceById(req.getShopId());
            bean.setShop(shopProxy);
        }

        return coffeeRepository.save(bean);
    }

    @Transactional
    public void deleteCoffeeById(Long id) {
        coffeeRepository.deleteById(id);
    }

    @Transactional
    public List<CoffeeBean> batchUpdateCoffeeVectors() {
        List<CoffeeBean> all = coffeeRepository.findAll();

        for (CoffeeBean coffee : all) {
            CoffeeVectorizationDto dto = mapper.toVectorDto(coffee);
            float[] vector = vectorService.createFlavorVector(dto);
            coffee.getFeatures().setFlavorVector(vector);
        }
        return coffeeRepository.saveAll(all);
    }
}
