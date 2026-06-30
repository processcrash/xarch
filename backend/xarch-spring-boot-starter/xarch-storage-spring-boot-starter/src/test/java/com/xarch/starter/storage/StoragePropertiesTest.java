package com.xarch.starter.storage;

import com.xarch.starter.storage.core.StorageType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StorageProperties}.
 */
class StoragePropertiesTest {

    @Test
    void defaultTypeIsLocal() {
        StorageProperties props = new StorageProperties();
        assertThat(props.getDefaultType()).isEqualTo("local");
        assertThat(props.resolvedDefaultType()).isEqualTo(StorageType.LOCAL);
    }

    @Test
    void resolvedDefaultTypeHandlesNullAndUnknown() {
        StorageProperties props = new StorageProperties();
        props.setDefaultType(null);
        assertThat(props.resolvedDefaultType()).isEqualTo(StorageType.LOCAL);

        props.setDefaultType("unknown-backend");
        assertThat(props.resolvedDefaultType()).isEqualTo(StorageType.LOCAL);

        props.setDefaultType("minio");
        assertThat(props.resolvedDefaultType()).isEqualTo(StorageType.MINIO);

        props.setDefaultType("Aliyun_OSS");
        assertThat(props.resolvedDefaultType()).isEqualTo(StorageType.ALIYUN_OSS);

        props.setDefaultType("S3");
        assertThat(props.resolvedDefaultType()).isEqualTo(StorageType.S3);
    }

    @Test
    void configsMapDefaultsToEmpty() {
        StorageProperties props = new StorageProperties();
        assertThat(props.getConfigs()).isNotNull().isEmpty();
    }

    @Test
    void backendConfigDefaultsAreSane() {
        StorageProperties.BackendConfig cfg = new StorageProperties.BackendConfig();
        assertThat(cfg.isEnabled()).isTrue();
        assertThat(cfg.getDefaultBucket()).isNull();
        assertThat(cfg.getCname()).isNull();
        assertThat(cfg.getKeyPrefix()).isNull();
    }
}
