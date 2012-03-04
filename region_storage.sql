-- MySQL dump 10.13  Distrib 5.1.49, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: worldguard
-- ------------------------------------------------------
-- Server version	5.1.49-1ubuntu8.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `worldguard`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `worldguard` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_bin */;

USE `worldguard`;

--
-- Table structure for table `group`
--

DROP TABLE IF EXISTS `group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `region`
--

DROP TABLE IF EXISTS `region`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `region` (
  `id` varchar(128) COLLATE utf8_bin NOT NULL,
  `world_id` int(10) unsigned NOT NULL,
  `type` enum('cuboid','poly2d','global') COLLATE utf8_bin NOT NULL,
  `priority` smallint(6) NOT NULL DEFAULT '0',
  `parent_region_id` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `parent_world_id` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`,`world_id`),
  KEY `fk_region_world` (`world_id`),
  KEY `parent` (`parent_region_id`,`parent_world_id`),
  CONSTRAINT `fk_region_world1` FOREIGN KEY (`world_id`) REFERENCES `world` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `parent` FOREIGN KEY (`parent_region_id`, `parent_world_id`) REFERENCES `region` (`id`, `world_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `region_cuboid`
--

DROP TABLE IF EXISTS `region_cuboid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `region_cuboid` (
  `region_id` varchar(128) COLLATE utf8_bin NOT NULL,
  `world_id` int(10) unsigned NOT NULL,
  `min_z` bigint(20) NOT NULL,
  `min_y` bigint(20) NOT NULL,
  `min_x` bigint(20) NOT NULL,
  `max_z` bigint(20) NOT NULL,
  `max_y` bigint(20) NOT NULL,
  `max_x` bigint(20) NOT NULL,
  PRIMARY KEY (`region_id`,`world_id`),
  KEY `fk_region_cuboid_region` (`region_id`),
  CONSTRAINT `fk_region_cuboid_region` FOREIGN KEY (`region_id`, `world_id`) REFERENCES `region` (`id`, `world_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `region_flag`
--

DROP TABLE IF EXISTS `region_flag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `region_flag` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `region_id` varchar(128) COLLATE utf8_bin NOT NULL,
  `world_id` int(10) unsigned NOT NULL,
  `flag` varchar(45) COLLATE utf8_bin NOT NULL,
  `value` varchar(256) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_flags_region` (`region_id`,`world_id`),
  CONSTRAINT `fk_flags_region` FOREIGN KEY (`region_id`, `world_id`) REFERENCES `region` (`id`, `world_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2455 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `region_groups`
--

DROP TABLE IF EXISTS `region_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `region_groups` (
  `region_id` varchar(128) COLLATE utf8_bin NOT NULL,
  `world_id` int(10) unsigned NOT NULL,
  `group_id` int(10) unsigned NOT NULL,
  `owner` tinyint(1) NOT NULL,
  PRIMARY KEY (`region_id`,`world_id`,`group_id`),
  KEY `fk_region_groups_region` (`region_id`),
  KEY `fk_region_groups_group` (`group_id`),
  CONSTRAINT `fk_region_groups_group` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_region_groups_region` FOREIGN KEY (`region_id`, `world_id`) REFERENCES `region` (`id`, `world_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `region_poly2d`
--

DROP TABLE IF EXISTS `region_poly2d`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `region_poly2d` (
  `region_id` varchar(128) COLLATE utf8_bin NOT NULL,
  `world_id` int(10) unsigned NOT NULL,
  `max_y` int(11) NOT NULL,
  `min_y` int(11) NOT NULL,
  PRIMARY KEY (`region_id`,`world_id`),
  KEY `fk_region_poly2d_region` (`region_id`),
  CONSTRAINT `fk_region_poly2d_region` FOREIGN KEY (`region_id`, `world_id`) REFERENCES `region` (`id`, `world_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `region_poly2d_point`
--

DROP TABLE IF EXISTS `region_poly2d_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `region_poly2d_point` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `region_id` varchar(128) COLLATE utf8_bin NOT NULL,
  `world_id` int(10) unsigned NOT NULL,
  `z` bigint(20) NOT NULL,
  `x` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_region_poly2d_point_region_poly2d` (`region_id`,`world_id`),
  CONSTRAINT `fk_region_poly2d_point_region_poly2d` FOREIGN KEY (`region_id`, `world_id`) REFERENCES `region_poly2d` (`region_id`, `world_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `region_users`
--

DROP TABLE IF EXISTS `region_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `region_users` (
  `region_id` varchar(128) COLLATE utf8_bin NOT NULL,
  `world_id` int(10) unsigned NOT NULL,
  `user_id` int(10) unsigned NOT NULL,
  `owner` tinyint(1) NOT NULL,
  PRIMARY KEY (`region_id`,`world_id`,`user_id`),
  KEY `fk_region_players_region` (`region_id`),
  KEY `fk_region_users_user` (`user_id`),
  CONSTRAINT `fk_region_users_region` FOREIGN KEY (`region_id`, `world_id`) REFERENCES `region` (`id`, `world_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_region_users_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `world`
--

DROP TABLE IF EXISTS `world`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `world` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(128) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-03-04 15:28:58
