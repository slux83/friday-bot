ALTER TABLE `war_summoner`
	ALTER `name` DROP DEFAULT;
ALTER TABLE `war_summoner`
	CHANGE COLUMN `name` `name` VARCHAR(128) NOT NULL COLLATE 'utf8_unicode_ci' AFTER `group_id`;