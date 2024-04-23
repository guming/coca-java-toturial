package coca.bulkhead.internal;

import coca.bulkhead.ThreadPoolBulkhead;
import coca.bulkhead.ThreadPoolBulkheadConfig;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FixedThreadPoolBulkheadTest {

    private ThreadPoolBulkhead bulkhead;
    private FixedThreadPoolBulkhead fixedThreadPoolBulkhead;

    @Before
    public void setUp() {
        ThreadPoolBulkheadConfig config = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(2)
                .coreThreadPoolSize(1)
                .queueCapacity(10)
                .keepAliveDuration(Duration.ofMillis(10))
                .build();
        bulkhead = ThreadPoolBulkhead.of("test", config);
        fixedThreadPoolBulkhead = new FixedThreadPoolBulkhead("testPool", config);
    }

    @Test
    public void testToString() {
        String result = bulkhead.toString();

        assertThat(result).isEqualTo("FixedThreadPoolBulkhead 'test'");
    }

    @Test
    public void testCustomSettings() {
        assertThat(bulkhead.getBulkheadConfig().getMaxThreadPoolSize()).isEqualTo(2);
        assertThat(bulkhead.getBulkheadConfig().getQueueCapacity()).isEqualTo(10);
        assertThat(bulkhead.getBulkheadConfig().getCoreThreadPoolSize()).isEqualTo(1);
        assertThat(bulkhead.getBulkheadConfig().getKeepAliveDuration())
                .isEqualTo(Duration.ofMillis(10));
    }

    @Test
    public void testCreateWithDefaults() {
        ThreadPoolBulkhead bulkhead = ThreadPoolBulkhead.ofDefaults("test");

        assertThat(bulkhead).isNotNull();
        assertThat(bulkhead.getBulkheadConfig()).isNotNull();
        assertThat(bulkhead.getBulkheadConfig().getMaxThreadPoolSize())
                .isEqualTo(ThreadPoolBulkheadConfig.DEFAULT_MAX_THREAD_POOL_SIZE);
        assertThat(bulkhead.getBulkheadConfig().getCoreThreadPoolSize())
                .isEqualTo(ThreadPoolBulkheadConfig.DEFAULT_CORE_THREAD_POOL_SIZE);
        assertThat(bulkhead.getBulkheadConfig().getQueueCapacity())
                .isEqualTo(ThreadPoolBulkheadConfig.DEFAULT_QUEUE_CAPACITY);
    }

    @Test
    public void shouldCreateThreadPoolBulkheadRegistryWithRegistryStore() {
        RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumer = getNoOpsRegistryEventConsumer();
        List<RegistryEventConsumer<ThreadPoolBulkhead>> registryEventConsumers = new ArrayList<>();
        registryEventConsumers.add(registryEventConsumer);
        Map<String, ThreadPoolBulkheadConfig> configs = new HashMap<>();
        final ThreadPoolBulkheadConfig defaultConfig = ThreadPoolBulkheadConfig.ofDefaults();
        configs.put("default", defaultConfig);
        final InMemoryThreadPoolBulkheadRegistry inMemoryThreadPoolBulkheadRegistry =
                new InMemoryThreadPoolBulkheadRegistry(configs, registryEventConsumers,
                        Map.of("Tag1", "Tag1Value"), new InMemoryRegistryStore<>());

        AssertionsForClassTypes.assertThat(inMemoryThreadPoolBulkheadRegistry).isNotNull();
        AssertionsForClassTypes.assertThat(inMemoryThreadPoolBulkheadRegistry.getDefaultConfig()).isEqualTo(defaultConfig);
        AssertionsForClassTypes.assertThat(inMemoryThreadPoolBulkheadRegistry.getConfiguration("testNotFound")).isEmpty();
        inMemoryThreadPoolBulkheadRegistry.addConfiguration("testConfig", defaultConfig);
        AssertionsForClassTypes.assertThat(inMemoryThreadPoolBulkheadRegistry.getConfiguration("testConfig")).isNotNull();
    }

    private RegistryEventConsumer<ThreadPoolBulkhead> getNoOpsRegistryEventConsumer() {
        return new RegistryEventConsumer<ThreadPoolBulkhead>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<ThreadPoolBulkhead> entryAddedEvent) {
            }
            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<ThreadPoolBulkhead> entryRemoveEvent) {
            }
            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<ThreadPoolBulkhead> entryReplacedEvent) {
            }
        };
    }

}