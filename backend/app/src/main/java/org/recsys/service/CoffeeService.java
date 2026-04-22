package org.recsys.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.recsys.dto.coffee.CoffeeBeanRequest;
import org.recsys.dto.coffee.CoffeeBeanResponse;
import org.recsys.dto.coffee.CoffeeVectorizationDto;
import org.recsys.dto.recommendation.CoffeeCandidate;
import org.recsys.dto.recommendation.CoffeeRecommendationResponse;
import org.recsys.mapper.CoffeeMapper;
import org.recsys.model.CoffeeBean;
import org.recsys.repository.CoffeeRepository;
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

    public CoffeeBean getCoffeeById(Long id) throws NotFoundException {
        return coffeeRepository.findById(id).orElseThrow(() -> new NotFoundException());
    }

    public List<CoffeeBean> getAllCoffees() {
        return coffeeRepository.findAll();
    }

    @Transactional
    public CoffeeBean addCoffee(CoffeeBeanRequest req) {
        CoffeeBean newCoffee = mapper.toEntity(req);
        if (newCoffee.getFeatures() != null) {
            newCoffee.getFeatures().setCoffeeBean(newCoffee);
        }

        return coffeeRepository.save(newCoffee);
    }

    @Transactional
    public CoffeeBean updateCoffee(Long id, CoffeeBeanRequest req) throws NotFoundException {
        CoffeeBean bean = coffeeRepository.findById(id).orElseThrow(() -> new NotFoundException());

        mapper.updateEntityFromDto(req, bean);

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

    public List<CoffeeRecommendationResponse> getSimilarCoffees(float[] target, int n) {
        List<CoffeeCandidate> candidates = coffeeRepository.findTopSimilarCoffeeCandidates(target, n);
        // TODO: later just return candidates since this wont be passed to response from
        // here. Remove from here and keep one in rec service
        List<Long> candidateIds = candidates.stream().map(CoffeeCandidate::getId).toList();
        List<CoffeeBean> beans = coffeeRepository.findAllById(candidateIds);
        Map<Long, CoffeeBeanResponse> beanMap = beans.stream()
                .collect(Collectors.toMap(CoffeeBean::getId, b -> mapper.toResponse(b)));

        return candidates.stream()
                .map(c -> new CoffeeRecommendationResponse(
                        beanMap.get(c.getId()),
                        c.getSimilarityScore()))
                .toList();
    }
}
