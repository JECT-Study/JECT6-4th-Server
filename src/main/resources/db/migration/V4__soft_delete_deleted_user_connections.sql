UPDATE user_oauth_accounts account
SET deleted_at = users.deleted_at
FROM users
WHERE account.user_id = users.id
  AND users.deleted_at IS NOT NULL
  AND account.deleted_at IS NULL;

UPDATE user_categories user_category
SET deleted_at = users.deleted_at
FROM users
WHERE user_category.user_id = users.id
  AND users.deleted_at IS NOT NULL
  AND user_category.deleted_at IS NULL;

UPDATE user_activity_types user_activity_type
SET deleted_at = users.deleted_at
FROM users
WHERE user_activity_type.user_id = users.id
  AND users.deleted_at IS NOT NULL
  AND user_activity_type.deleted_at IS NULL;

UPDATE user_activity_channels user_activity_channel
SET deleted_at = users.deleted_at
FROM users
WHERE user_activity_channel.user_id = users.id
  AND users.deleted_at IS NOT NULL
  AND user_activity_channel.deleted_at IS NULL;

UPDATE user_regions user_region
SET deleted_at = users.deleted_at
FROM users
WHERE user_region.user_id = users.id
  AND users.deleted_at IS NOT NULL
  AND user_region.deleted_at IS NULL;

UPDATE blog_analysis_results blog_analysis_result
SET deleted_at = users.deleted_at
FROM users
WHERE blog_analysis_result.user_id = users.id
  AND users.deleted_at IS NOT NULL
  AND blog_analysis_result.deleted_at IS NULL;
