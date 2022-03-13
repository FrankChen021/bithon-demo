CREATE DATABASE IF NOT EXISTS `bithon_demo` DEFAULT CHARSET utf8mb4;
USE `bithon_demo`;

DROP TABLE user;
CREATE TABLE user
(
    `id`       BIGINT AUTO_INCREMENT,
    `name`     VARCHAR(32) NOT NULL,
    `password` VARCHAR(32) NOT NULL,
    PRIMARY KEY pk_id (`id`),
    UNIQUE KEY uq_name (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='';


