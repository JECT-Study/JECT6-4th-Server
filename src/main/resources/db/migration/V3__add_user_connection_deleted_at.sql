ALTER TABLE user_oauth_accounts
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE user_categories
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE user_activity_types
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE user_activity_channels
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE user_regions
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE blog_analysis_results
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE user_oauth_accounts
    DROP CONSTRAINT IF EXISTS uk_user_oauth_accounts_provider_user;

ALTER TABLE user_oauth_accounts
    DROP CONSTRAINT IF EXISTS uk_user_oauth_accounts_user_provider;

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_oauth_accounts_provider_user_active
    ON user_oauth_accounts (provider, provider_user_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_oauth_accounts_user_provider_active
    ON user_oauth_accounts (user_id, provider)
    WHERE deleted_at IS NULL;
