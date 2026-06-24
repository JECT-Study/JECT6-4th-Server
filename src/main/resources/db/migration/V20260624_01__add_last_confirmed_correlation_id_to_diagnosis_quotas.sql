ALTER TABLE diagnosis_quotas
    ADD COLUMN IF NOT EXISTS last_confirmed_correlation_id VARCHAR(64);

COMMENT ON COLUMN diagnosis_quotas.last_confirmed_correlation_id
    IS 'Last confirmed analysis correlation id for quota idempotency guard';
