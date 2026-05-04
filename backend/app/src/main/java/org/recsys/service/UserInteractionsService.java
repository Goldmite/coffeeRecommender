package org.recsys.service;

import java.time.Instant;

import org.recsys.dto.coffee.PurchasedCoffeeDto;
import org.recsys.dto.user.InteractionRequest;
import org.recsys.mapper.CoffeeMapper;
import org.recsys.model.CoffeeBean;
import org.recsys.model.User;
import org.recsys.model.UserInteractions;
import org.recsys.model.UserPreferences;
import org.recsys.model.keys.UserInteractionId;
import org.recsys.repository.CoffeeRepository;
import org.recsys.repository.UserInteractionsRepository;
import org.recsys.repository.UserPreferencesRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInteractionsService {

    private final FlavorVectorService flavorVectorService;
    private final UserInteractionsRepository interactionsRepository;
    private final CoffeeRepository coffeeRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final CoffeeMapper mapper;

    public Page<PurchasedCoffeeDto> findPurchasedCoffeesByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return coffeeRepository.findPurchasedCoffeesByUserId(userId, pageable)
                .map(i -> new PurchasedCoffeeDto(i.getRating(), i.getPurchaseDate(),
                        mapper.toResponse(i.getCoffeeBean())));
    }

    @Transactional
    public UserInteractions addInteraction(InteractionRequest request) {
        Long userId = request.userId();
        Long coffeeId = request.coffeeId();
        UserPreferences userPref = preferencesRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException());
        CoffeeBean coffee = coffeeRepository.findById(coffeeId).orElseThrow(() -> new EntityNotFoundException());
        UserInteractionId id = new UserInteractionId(userId, coffeeId);

        UserInteractions interaction = interactionsRepository.findById(id)
                .orElseGet(() -> createNewInteraction(userPref.getUser(), coffee));

        interaction.setIsClicked(true);

        float shiftStrength = 0;
        if (request.purchased() != null && request.purchased()) {
            interaction.setIsPurchased(true);
            interaction.setPurchaseDate(Instant.now());
            shiftStrength = 0.1f;
        }

        if (request.rating() != null) {
            interaction.setRating(request.rating());
            // rating - shift alpha
            // 1&2: -0.03f; 3: 0f; 4: 0.05f; 5: 0.1f
            shiftStrength = (request.rating() >= 3) ? (request.rating() - 3.0f) * 0.5f : -0.03f;
        }
        UserInteractions saved = interactionsRepository.save(interaction);
        // shift user flavor profile towards coffee vector by strength (alpha) amount
        float[] shiftedProfile = flavorVectorService.calculateVectorShift(userPref.getTasteProfile(),
                coffee.getFeatures().getFlavorVector(), shiftStrength);
        userPref.setTasteProfile(shiftedProfile);
        preferencesRepository.save(userPref);

        return saved;
    }

    private UserInteractions createNewInteraction(User user, CoffeeBean coffee) {

        return UserInteractions.builder()
                .userId(user.getId())
                .coffeeId(coffee.getId())
                .user(user)
                .coffeeBean(coffee)
                .isClicked(false)
                .isPurchased(false)
                .build();
    }

}
