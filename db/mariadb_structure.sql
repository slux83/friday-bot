-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               10.3.7-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             9.4.0.5125
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Dumping database structure for jarvis
CREATE DATABASE IF NOT EXISTS `jarvis` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `jarvis`;

-- Dumping structure for table jarvis.war_death
CREATE TABLE IF NOT EXISTS `war_death` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` int(11) NOT NULL DEFAULT 0,
  `node` int(11) NOT NULL DEFAULT 0,
  `num_deaths` int(11) NOT NULL DEFAULT 0,
  `champion` varchar(128) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `player` varchar(128) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_war_death_war_group` (`group_id`),
  CONSTRAINT `FK_war_death_war_group` FOREIGN KEY (`group_id`) REFERENCES `war_group` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=145 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.
-- Dumping structure for table jarvis.war_group
CREATE TABLE IF NOT EXISTS `war_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `group_name` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.
-- Dumping structure for table jarvis.war_history
CREATE TABLE IF NOT EXISTS `war_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `war_date` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `opponent_tag` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `group_id` int(11) NOT NULL DEFAULT 0,
  `node` int(11) NOT NULL DEFAULT 0,
  `num_deaths` int(11) NOT NULL DEFAULT 0,
  `champion` varchar(128) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `player` varchar(128) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_war_history_war_group` (`group_id`),
  CONSTRAINT `FK_war_history_war_group` FOREIGN KEY (`group_id`) REFERENCES `war_group` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=138 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;