DROP TABLE IF EXISTS `country`;
CREATE TABLE `country` (
  `id` int(15) NOT NULL AUTO_INCREMENT,
  `countryname` varchar(255) NOT NULL,
  `countrycode` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
