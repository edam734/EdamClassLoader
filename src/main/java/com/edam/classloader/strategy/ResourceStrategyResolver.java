package com.edam.classloader.strategy;

import com.edam.classloader.strategy.source.HttpResourceStrategy;
import com.edam.classloader.strategy.source.LocalResourceStrategy;
import com.edam.classloader.strategy.source.ResourceStrategy;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolves the resource strategy responsible for handling a given URI.
 *
 * <p>Strategies are evaluated in registration order, and the first
 * supporting strategy is selected.
 */
public final class ResourceStrategyResolver {
    private final List<ResourceStrategy> strategies;

    /**
     * Creates a resolver with local filesystem and HTTP/HTTPS strategies.
     */
    public ResourceStrategyResolver() {
        this(List.of(new LocalResourceStrategy(), new HttpResourceStrategy()));
    }

    /**
     * Creates a resolver with the specified resource strategies.
     *
     * @param strategies the available resource strategies
     */
    public ResourceStrategyResolver(List<ResourceStrategy> strategies) {
        this.strategies = List.copyOf(Objects.requireNonNull(strategies, "strategies"));
    }

    /**
     * Finds the first strategy that supports the specified URI.
     *
     * @param uri the resource location
     * @return an Optional containing the matching strategy,
     *         or an empty Optional if none supports the URI
     */
    public Optional<ResourceStrategy> findSupportingStrategy(URI uri) {
        return strategies.stream().filter(strategy -> strategy.supports(uri)).findFirst();
    }
}