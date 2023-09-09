package org.example.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.example.entity.Cartoon;
import org.springframework.stereotype.Repository;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

// The CartoonSearchRepository is responsible for interacting with ElasticSearch to perform search operations related to cartoons.
@Repository
public class CartoonSearchRepository {

    // The RestHighLevelClient is the main component to interact with an Elasticsearch cluster.
    private final RestHighLevelClient client;

    // Constructor to initialize the RestHighLevelClient.
    public CartoonSearchRepository(RestHighLevelClient client) {
        this.client = client;
    }

    // This method performs a search on the cartoons index using the provided query.
    public List<Cartoon> search(QueryBuilder query) {
        // Create a new search request for the cartoons index.
        SearchRequest searchRequest = new SearchRequest("cartoons");
        // Set the query for the search request.
        searchRequest.source().query(query);

        try {
            // Execute the search request.
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            // Parse the response to extract cartoon data and return the results.
            return parseResponse(response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute search", e);
        }
    }

    // This method retrieves distinct values for a given attribute from the cartoons index.
    public List<String> getAttributeValues(String attribute) {
        // Use the keyword field for aggregations.
        String attributeKeyword = attribute + ".keyword";
        int size = 1000; // Maximum number of distinct values to retrieve.
        SearchRequest searchRequest = new SearchRequest("cartoons");
        // Create a terms aggregation to get distinct values.
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("distinct_values")
                .field(attributeKeyword)
                .size(size);
        searchRequest.source().aggregation(aggregation);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            // Extract the distinct values from the aggregation results.
            Terms terms = response.getAggregations().get("distinct_values");
            return terms.getBuckets().stream().map(Bucket::getKeyAsString).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to get attribute values", e);
        }
    }

    // This method searches for distinct values of a given attribute that match a specific query.
    // Note: This method is not used in the current implementation.
    public List<String> searchAttributeValues(String attribute, QueryBuilder queryBuilder) {
        SearchRequest searchRequest = new SearchRequest("cartoons");
        searchRequest.source().query(queryBuilder);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = response.getHits().getHits();

            // Use a Set to ensure distinct values.
            Set<String> distinctValues = new HashSet<>();

            // Extract the values of the specified attribute from the search hits.
            for (SearchHit hit : searchHits) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                Object value = sourceAsMap.get(attribute);
                if (value instanceof List) {
                    List<?> values = (List<?>) value;
                    for (Object v : values) {
                        distinctValues.add(v.toString());
                    }
                } else if (value != null) {
                    distinctValues.add(value.toString());
                }
            }

            return new ArrayList<>(distinctValues);
        } catch (IOException e) {
            throw new RuntimeException("Failed to find attribute values", e);
        }
    }

    // Helper method to parse the search response and convert it to a list of Cartoon objects.
    private List<Cartoon> parseResponse(SearchResponse response) throws JsonProcessingException {
        SearchHit[] searchHits = response.getHits().getHits();
        List<Cartoon> cartoons = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        // Configure the ObjectMapper to ignore properties that are not defined in the Cartoon class.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        for (SearchHit hit : searchHits) {
            String sourceAsString = hit.getSourceAsString();
            Cartoon cartoon = objectMapper.readValue(sourceAsString, Cartoon.class);
            cartoons.add(cartoon);
        }
        return cartoons;
    }
}
