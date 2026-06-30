-- ===========================================================================
--  Default storage configurations
--  This script seeds the sys_storage_config table with one row per backend.
--  Replace the secrets with the actual values used in your environment, or
--  rely on the xarch.storage.* properties to override them at runtime.
-- ===========================================================================

INSERT INTO sys_storage_config
    (id, storage_code, storage_name, storage_type, enabled, is_default, endpoint, access_key, secret_key,
     bucket_name, region, cname, sort, del_flag, create_time, update_time, remark)
VALUES
    (1, 'local', 'Local Filesystem', 'local', 1, 1, NULL, NULL, NULL, 'xarch', NULL, '/files', 1, 0, NOW(), NOW(), 'Default local storage'),
    (2, 'minio', 'MinIO', 'minio', 0, 0, 'http://localhost:9000', 'minioadmin', 'minioadmin', 'xarch', 'us-east-1', NULL, 2, 0, NOW(), NOW(), 'MinIO object storage'),
    (3, 'aliyun_oss', 'Aliyun OSS', 'aliyun_oss', 0, 0, 'https://oss-cn-hangzhou.aliyuncs.com', '', '', 'xarch', NULL, NULL, 3, 0, NOW(), NOW(), 'Aliyun Object Storage Service'),
    (4, 's3', 'AWS S3', 's3', 0, 0, NULL, '', '', 'xarch', 'us-east-1', NULL, 4, 0, NOW(), NOW(), 'Amazon S3 or S3-compatible')
ON DUPLICATE KEY UPDATE
    storage_name = VALUES(storage_name),
    storage_type = VALUES(storage_type),
    enabled      = VALUES(enabled),
    is_default   = VALUES(is_default),
    endpoint     = VALUES(endpoint),
    bucket_name  = VALUES(bucket_name),
    region       = VALUES(region),
    cname        = VALUES(cname),
    sort         = VALUES(sort),
    update_time  = NOW();
