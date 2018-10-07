-- Adds the timestamp of the last activity to the groups
ALTER TABLE `war_group`
	ADD COLUMN `last_activity` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `group_features`;