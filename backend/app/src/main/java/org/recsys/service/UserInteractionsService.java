package org.recsys.service;

import java.time.Instant;

import org.recsys.dto.coffee.PurchasedCoffeeDto;
import org.recsys.dto.user.InteractionRequest;
import org.recsys.mapper.CoffeeMapper;
import org.recsys.model.CoffeeBean;
import org.recsys.model.User;
import org.recsys.model.UserInteractions;
import org.recsys.model.keys.UserInteractionId;
import org.recsys.repository.CoffeeRepository;
import org.recsys.repository.UserInteractionsRepository;
import org.recsys.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInteractionsService {

    private final UserInteractionsRepository interactionsRepository;
    private final UserRepository userRepository;
    private final CoffeeRepository coffeeRepository;
    private final CoffeeMapper mapper;

    public Page<PurchasedCoffeeDto> findPurchasedCoffeesByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return coffeeRepository.findPurchasedCoffeesByUserId(userId, pageable)
                .map(i -> new PurchasedCoffeeDto(i.getRating(), i.getPurchaseDate(),
                        mapper.toResponse(i.getCoffeeBean())));
    }

    public UserInteractions addInteraction(InteractionRequest request) {
        Long userId = request.userId();
        Long coffeeId = request.coffeeId();
        UserInteractionId id = new UserInteractionId(userId, coffeeId);

        UserInteractions interaction = interactionsRepository.findById(id)
                .orElseGet(() -> createNewInteraction(userId, coffeeId));

        interaction.setIsClicked(true);

        if (request.purchased() != null && request.purchased()) {
            interaction.setIsPurchased(true);
            interaction.setPurchaseDate(Instant.now());
        }

        if (request.rating() != null) {
            interaction.setRating(request.rating());
        }

        return interactionsRepository.save(interaction);
    }

    private UserInteractions createNewInteraction(Long userId, Long coffeeId) {
        User user = userRepository.getReferenceById(userId);
        CoffeeBean coffee = coffeeRepository.getReferenceById(coffeeId);

        return UserInteractions.builder()
                .userId(userId)
                .coffeeId(coffeeId)
                .user(user)
                .coffeeBean(coffee)
                .isClicked(false)
                .isPurchased(false)
                .build();
    }

}
