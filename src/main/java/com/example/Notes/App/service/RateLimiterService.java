package com.example.Notes.App.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import java.time.Duration;
import java.util.function.Supplier;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private final ProxyManager<String> proxyManager;
    private final long capacity;
    private final long refillTokens;
    private final long durationMinutes;

    public RateLimiterService(
            RedissonClient redissonClient,
            @Value("${rate.limit.capacity:10}") long capacity,
            @Value("${rate.limit.refill-tokens:10}") long refillTokens,
            @Value("${rate.limit.refill-duration-minutes:1}") long durationMinutes) {
        CommandAsyncExecutor commandExecutor = ((org.redisson.Redisson) redissonClient).getCommandExecutor();
        this.proxyManager = RedissonBasedProxyManager.builderFor(commandExecutor).build();
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.durationMinutes = durationMinutes;
    }

    public boolean tryConsume(String key) {
        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(refillTokens, Duration.ofMinutes(durationMinutes))
                        .build())
                .build();

        return proxyManager.builder().build(key, configSupplier).tryConsume(1);
    }
}
