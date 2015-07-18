delimiter $$

CREATE TABLE `object_types` (
  `object_type_id` bigint(20) unsigned NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `parent_type_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`object_type_id`),
  KEY `_idx` (`parent_type_id`),
  CONSTRAINT `object_type_to_parent_type` FOREIGN KEY (`parent_type_id`) REFERENCES `object_types` (`object_type_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

delimiter $$

CREATE TABLE `attributes` (
  `attribute_id` bigint(20) unsigned NOT NULL,
  `type` int(2) unsigned DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `multiple` int(1) unsigned DEFAULT NULL,
  `reference_type_id` bigint(20) unsigned DEFAULT NULL,
  `object_type_id` bigint(20) unsigned DEFAULT NULL,
  `object_type_order` int(6) unsigned DEFAULT NULL,
  PRIMARY KEY (`attribute_id`),
  KEY `attr_to_object_type_idx` (`object_type_id`),
  KEY `attr_to_reference_type_idx` (`reference_type_id`),
  CONSTRAINT `attr_to_object_type` FOREIGN KEY (`object_type_id`) REFERENCES `object_types` (`object_type_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `attr_to_reference_type` FOREIGN KEY (`reference_type_id`) REFERENCES `object_types` (`object_type_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

delimiter $$

CREATE TABLE `list_entries` (
  `list_entry_id` bigint(20) unsigned NOT NULL,
  `entry_value` varchar(100) DEFAULT NULL,
  `attribute_id` bigint(20) unsigned DEFAULT NULL,
  `attribute_order` int(6) unsigned DEFAULT NULL,
  PRIMARY KEY (`list_entry_id`),
  KEY `list_value_to_attr_idx` (`attribute_id`),
  CONSTRAINT `list_value_to_attr` FOREIGN KEY (`attribute_id`) REFERENCES `attributes` (`attribute_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$
delimiter $$

CREATE TABLE `data_objects` (
  `object_id` bigint(20) unsigned NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `object_type_id` bigint(20) unsigned DEFAULT NULL,
  `children_group_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`object_id`),
  KEY `object_to_type_idx` (`object_type_id`),
  KEY `object_to_children_group_idx` (`children_group_id`),
  CONSTRAINT `object_to_type` FOREIGN KEY (`object_type_id`) REFERENCES `object_types` (`object_type_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$
delimiter $$

CREATE TABLE `children_groups` (
  `group_id` bigint(20) unsigned NOT NULL,
  `parent_id` bigint(20) unsigned DEFAULT NULL,
  `object_type_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`group_id`),
  KEY `children_group_to_object_type_idx` (`object_type_id`),
  KEY `children_group_to_parent_idx` (`parent_id`),
  CONSTRAINT `children_group_to_object_type` FOREIGN KEY (`object_type_id`) REFERENCES `object_types` (`object_type_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `children_group_to_parent` FOREIGN KEY (`parent_id`) REFERENCES `data_objects` (`object_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

delimiter $$
ALTER TABLE `data_objects` 
ADD CONSTRAINT `object_to_children_group` FOREIGN KEY (`children_group_id`) REFERENCES `children_groups` (`group_id`) ON DELETE SET NULL ON UPDATE CASCADE$$
delimiter $$

CREATE TABLE `parameters` (
  `parameter_id` bigint(20) unsigned NOT NULL,
  `attribute_id` bigint(20) unsigned DEFAULT NULL,
  `entity_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`parameter_id`),
  KEY `parameter_to_attribute_idx` (`attribute_id`),
  KEY `parameter_to_object_idx` (`entity_id`),
  CONSTRAINT `parameter_to_object` FOREIGN KEY (`entity_id`) REFERENCES `data_objects` (`object_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `parameter_to_attribute` FOREIGN KEY (`attribute_id`) REFERENCES `attributes` (`attribute_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$
delimiter $$

CREATE TABLE `parameter_values` (
  `value_id` bigint(20) unsigned NOT NULL,
  `type` int(1) unsigned DEFAULT NULL,
  `text_value` varchar(500) DEFAULT NULL,
  `date_value` timestamp NULL DEFAULT NULL,
  `reference_id` bigint(20) unsigned DEFAULT NULL,
  `list_entry_id` bigint(20) unsigned DEFAULT NULL,
  `parameter_id` bigint(20) unsigned DEFAULT NULL,
  `parameter_order` int(6) unsigned DEFAULT NULL,
  PRIMARY KEY (`value_id`),
  KEY `param_to_reference_idx` (`reference_id`),
  KEY `param_to_list_value_idx` (`list_entry_id`),
  KEY `param_value_to_param_idx` (`parameter_id`),
  CONSTRAINT `param_value_to_param` FOREIGN KEY (`parameter_id`) REFERENCES `parameters` (`parameter_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `param_value_to_list_value` FOREIGN KEY (`list_entry_id`) REFERENCES `list_entries` (`list_entry_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `param_value_to_reference` FOREIGN KEY (`reference_id`) REFERENCES `data_objects` (`object_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

delimiter $$

CREATE TABLE `id_generation_sequences` (
  `entity_set` varchar(20) NOT NULL,
  `last_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`entity_set`),
  UNIQUE KEY `idid_generation_sequences_UNIQUE` (`entity_set`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$
