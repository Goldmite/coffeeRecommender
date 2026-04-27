package org.recsys.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.recsys.model.FlavorCategory;
import org.recsys.repository.CoffeeRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlavorVectorService {

    private final CoffeeRepository repository;

    private static final Map<FlavorCategory, Set<String>> FLAVOR_WHEEL = new LinkedHashMap<>() {
        {
            put(FlavorCategory.FRUITY, Set.of("fruit", "berry", "citrus", "peach", "cherry"));
            put(FlavorCategory.FLORAL, Set.of("floral", "jasmine", "rose", "tea"));
            put(FlavorCategory.SWEET, Set.of("sweet", "caramel", "honey", "vanilla"));
            put(FlavorCategory.NUTTYCOCOA, Set.of("nut", "chocolate", "cocoa", "almond", "hazelnut"));
            put(FlavorCategory.SPICES, Set.of("spice", "cinnamon", "clove"));
            put(FlavorCategory.VEGETAL, Set.of("herb", "grass", "vegetal"));
            put(FlavorCategory.SOUR, Set.of("sour", "winey", "fermented"));
        }
    };
    private final Map<FlavorCategory, Double> idfMap = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void initInverseDocumentFrequency() {
        List<String> descriptions = repository.findAllDescriptions();
        if (descriptions.isEmpty())
            return;

        int size = descriptions.size();
        Map<FlavorCategory, Integer> categoryDF = new HashMap<>();

        for (String desc : descriptions) {
            List<String> tokens = tokenize(desc);

            for (FlavorCategory category : FLAVOR_WHEEL.keySet()) {
                Set<String> keywords = FLAVOR_WHEEL.get(category);
                if (tokens.stream().anyMatch(keywords::contains)) {
                    categoryDF.merge(category, 1, (oldVal, newVal) -> oldVal + newVal);
                }
            }
        }

        for (FlavorCategory category : FlavorCategory.values()) {
            double df = categoryDF.getOrDefault(category, 0);
            double idf = Math.log((double) size / (df + 1.0)) + 1.0;
            idfMap.put(category, idf);
        }
    }

    public float[] getUnifiedFlavorVector(List<String> flavorNotes, String description) {
        FlavorCategory[] categories = FlavorCategory.values();
        float[] vector = new float[categories.length];

        List<String> tokens = tokenize(description == null ? "" : description.toLowerCase());
        List<String> normalizedNotes = flavorNotes == null ? List.of()
                : flavorNotes.stream().map(String::toLowerCase).toList();
        // Fill in using flavor notes, fallback to description-derived features
        for (int i = 0; i < categories.length; i++) {
            FlavorCategory category = categories[i];
            Set<String> flavorKeywords = FLAVOR_WHEEL.get(category);

            boolean foundInNotes = normalizedNotes.stream()
                    .anyMatch(note -> flavorKeywords.stream().anyMatch(note::contains));

            if (foundInNotes) {
                vector[i] = 1.0f; // explicit flavor note
            } else {
                double tf = calculateCategoryTermFrequency(tokens, flavorKeywords);
                double idf = idfMap.getOrDefault(category, 1.0);
                vector[i] = (float) (tf * idf);
            }
        }

        return vector;
    }

    private double calculateCategoryTermFrequency(List<String> tokens, Set<String> keywords) {
        if (tokens.isEmpty())
            return 0;
        long count = tokens.stream().filter(keywords::contains).count();
        return (double) count / tokens.size();
    }

    private List<String> tokenize(String description) {
        List<String> result = new ArrayList<>();
        try (Analyzer analyzer = new StandardAnalyzer();
                TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(description))) {

            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                result.add(attr.toString());
            }
            tokenStream.end();
        } catch (IOException e) {
            // TODO: log error or return empty
        }
        return result;
    }
}
