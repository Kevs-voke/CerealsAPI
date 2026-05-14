package com.gkev.spring_redis.service;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Duration; import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RateLimitingService {
    private static final int REQ_PER_MINUTE =30;
    private final ProxyManager<String> proxyManager;

    public Bucket resolveBucket(String key){
        Supplier<BucketConfiguration> configSupplier = this::getConfig;
        return proxyManager .builder() .build(key,configSupplier);
    }
    private BucketConfiguration getConfig(){
        var limit = Bandwidth.builder()
                .capacity(REQ_PER_MINUTE)
                .refillIntervally(REQ_PER_MINUTE,
                        Duration.ofMinutes(1)) .build();
        return BucketConfiguration.builder() .addLimit(limit) .build(); } }
