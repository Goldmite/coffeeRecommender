package org.recsys.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.recsys.config.FeatureWeights;
import org.recsys.repository.CoffeeRepository;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DescriptionVectorService {

    private final CoffeeRepository repository;
    private final FeatureWeights settings;

    private static final List<String> VOCABULARY = Arrays.asList(
            "chocolate", "berries", "nuts", "fruits", "citrus",
            "flowers", "caramel", "spices", "sweet", "herbal", "earthy");

    private final Map<String, Double> idfMap = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void initInverseDocumentFrequency() {
        List<String> descriptions = repository.findAllDescriptions();
        if (descriptions.isEmpty())
            return;

        calculateGlobalIdf(descriptions);
    }

    private void calculateGlobalIdf(List<String> allDescriptions) {
        Map<String, Integer> df = new HashMap<>();
        int size = allDescriptions.size();

        for (String desc : allDescriptions) {
            Set<String> uniqueTerms = new HashSet<>(tokenize(desc));
            for (String term : uniqueTerms) {
                df.put(term, df.getOrDefault(term, 0) + 1);
            }
        }

        for (String term : VOCABULARY) {
            double idf = Math.log((double) size / (df.getOrDefault(term, 0) + 1)) + 1.0;
            idfMap.put(term, idf);
        }
    }

    public float[] getDescriptionTfIdfVector(String description) {
        float[] vector = new float[VOCABULARY.size()];

        List<String> tokens = tokenize(description);
        // Calculate term frequency
        Map<String, Integer> termCounts = new HashMap<>();
        for (String token : tokens) {
            termCounts.put(token, termCounts.getOrDefault(token, 0) + 1);
        }
        // Construct vector
        for (int i = 0; i < VOCABULARY.size(); i++) {
            String term = VOCABULARY.get(i);
            if (termCounts.containsKey(term)) {
                double tf = (double) termCounts.get(term) / tokens.size();
                double idf = idfMap.getOrDefault(term, 1.0);

                vector[i] = (float) (tf * idf) * settings.softWeight();
            } else {
                vector[i] = 0.0f;
            }
        }
        return vector;
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
            e.printStackTrace();
        }
        return result;
    }
}
