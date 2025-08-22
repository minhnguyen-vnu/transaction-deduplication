package com.dedupservice.infrastructure.store;

import com.dedupservice.core.port.store.IdempotencyStore;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.BooleanOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.ProtocolKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCuckooIdempotencyStore implements IdempotencyStore {

    private final StatefulRedisConnection<String, String> connection;
    private final StringRedisTemplate redis;

    @Value("${dedup.ttl-seconds:600}")
    private long ttlSeconds;

    @Value("${dedup.cuckoo.filter-name:dedup:cf:v1}")
    private String filterName;

    @Value("${dedup.cuckoo.key-prefix:dedup:cf:key:}")
    private String keyPrefix;

    private RedisCommands<String, String> syncCommands() {
        return connection.sync();
    }

    // Custom ProtocolKeyword cho CF commands
    private static final ProtocolKeyword CF_ADD = () -> "CF.ADD".getBytes();
    private static final ProtocolKeyword CF_EXISTS = () -> "CF.EXISTS".getBytes();
    private static final ProtocolKeyword CF_DEL = () -> "CF.DEL".getBytes();

    @Override
    public boolean isDuplicate(String idemKey) {
        CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
                .add(filterName)
                .add(idemKey);

        try {
            Boolean inFilter = syncCommands().dispatch(
                    CF_EXISTS,
                    new BooleanOutput<>(StringCodec.UTF8),
                    args
            );
            if (!Boolean.TRUE.equals(inFilter)) {
                log.info("CF.EXISTS filter={} key={} -> false", filterName, idemKey);
                return false;
            }

            // 2. Xác nhận bằng Redis key TTL
            Boolean exists = redis.hasKey(idemKey);
            boolean duplicate = exists != null && exists;
            log.info("Idempotency check key={} exists={} -> duplicate={}", idemKey, exists, duplicate);
            return duplicate;
        } catch (RedisCommandExecutionException e) {
            log.error("Redis isDuplicate failed filter={} key={}", filterName, idemKey, e);
            throw e;
        }
    }

    @Override
    public void recordProcessed(String idemKey) {
        try {
            // 1. Đặt key TTL trong Redis KV
            redis.opsForValue().set(idemKey, "1", Duration.ofSeconds(ttlSeconds));
            log.info("Set Redis key={} with ttlSeconds={}", idemKey, ttlSeconds);

            // 2. Thêm vào filter
            CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
                    .add(filterName)
                    .add(idemKey);

            Boolean added = syncCommands().dispatch(
                    CF_ADD,
                    new BooleanOutput<>(StringCodec.UTF8),
                    args
            );
            log.info("CF.ADD filter={} key={} -> {}", filterName, idemKey, added);
        } catch (RedisCommandExecutionException e) {
            log.error("Redis recordProcessed failed filter={} key={}", filterName, idemKey, e);
            throw e;
        }
    }

    @Override
    public void remove(String idemKey) {
        CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
                .add(filterName)
                .add(idemKey);

        try {
            Boolean removed = syncCommands().dispatch(
                    CF_DEL,
                    new BooleanOutput<>(StringCodec.UTF8),
                    args
            );
            log.info("CF.DEL filter={} key={} -> {}", filterName, idemKey, removed);
        } catch (RedisCommandExecutionException e) {
            log.error("Redis CF.DEL failed for filter={} key={}", filterName, idemKey, e);
            throw e;
        }
    }
}
