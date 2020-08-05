/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MariaDB
 Source Server Version : 100410
 Source Host           : localhost:3306
 Source Schema         : qicz

 Target Server Type    : MariaDB
 Target Server Version : 100410
 File Encoding         : 65001

 Date: 05/08/2020 15:47:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for demo
-- ----------------------------
DROP TABLE IF EXISTS `demo`;
CREATE TABLE `demo` (
  `demo_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(30) DEFAULT NULL,
  `age` int(2) unsigned DEFAULT NULL,
  `addr` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`demo_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of demo
-- ----------------------------
BEGIN;
INSERT INTO `demo` VALUES (1, 'qicz', 12, 'addr');
INSERT INTO `demo` VALUES (2, 'qicz12', 12, 'addr12');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
