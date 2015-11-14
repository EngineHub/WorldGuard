-- Blacklist table for MySQL.
-- You must still configure WorldGuard to use your database.
-- If you do not plan on using a database for logging blacklist events,
-- you do not need to do anything with this file.

CREATE TABLE `blacklist_events` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `event` varchar(25) NOT NULL,
  `world` varchar(32) NOT NULL,
  `player` varchar(16) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `z` int(11) NOT NULL,
  `item` int(11) NOT NULL,
  `time` int(11) NOT NULL,
  `comment` varchar(255) NULL,
  PRIMARY KEY (`id`)
);

-- Required update if you have an older version of the table:

ALTER TABLE `blacklist_events` ADD `comment` VARCHAR( 255 ) NULL 

ALTER TABLE `blacklist_events` ADD `world` VARCHAR( 32 ) NOT NULL 