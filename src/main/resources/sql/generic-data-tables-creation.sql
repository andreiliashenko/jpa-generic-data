set foreign_key_checks = 0;

drop table if exists 
    parameter_values, parameters, children_groups, 
    data_objects, list_entries, attributes, object_types;

set foreign_key_checks = 1;

create table object_types (
    object_type_id bigint(20) unsigned not null,
    name varchar(100) default null,
    parent_type_id bigint(20) unsigned default null,
    primary key (object_type_id),
    key _idx (parent_type_id),
    constraint object_type_to_parent_type foreign key (parent_type_id) references object_types (object_type_id) 
        on delete set null on update cascade
) engine=innodb default charset=utf8;

create table attributes (
    attribute_id bigint(20) unsigned not null,
    type int(2) unsigned default null,
    name varchar(100) default null,
    multiple int(1) unsigned default null,
    reference_type_id bigint(20) unsigned default null,
    object_type_id bigint(20) unsigned default null,
    object_type_order int(6) unsigned default null,
    primary key (attribute_id),
    key attr_to_object_type_idx (object_type_id),
    key attr_to_reference_type_idx (reference_type_id),
    constraint attr_to_object_type foreign key (object_type_id) references object_types (object_type_id) 
        on delete set null on update cascade,
    constraint attr_to_reference_type foreign key (reference_type_id) references object_types (object_type_id)
        on delete set null on update cascade
) engine=innodb default charset=utf8;

create table list_entries (
    list_entry_id bigint(20) unsigned not null,
    entry_value varchar(100) default null,
    attribute_id bigint(20) unsigned default null,
    attribute_order int(6) unsigned default null,
    primary key (list_entry_id),
    key list_value_to_attr_idx (attribute_id),
    constraint list_value_to_attr foreign key (attribute_id) references attributes (attribute_id) 
        on delete set null on update cascade
) engine=innodb default charset=utf8;

create table data_objects (
    object_id bigint(20) unsigned not null,
    name varchar(100) default null,
    description varchar(1000) default null,
    object_type_id bigint(20) unsigned default null,
    children_group_id bigint(20) unsigned default null,
    primary key (object_id),
    key object_to_type_idx (object_type_id),
    key object_to_children_group_idx (children_group_id),
    constraint object_to_type foreign key (object_type_id) references object_types (object_type_id)
        on delete set null on update cascade
    ) engine=innodb default charset=utf8;

create table children_groups (
    group_id bigint(20) unsigned not null,
    parent_id bigint(20) unsigned default null,
    object_type_id bigint(20) unsigned default null,
    primary key (group_id),
    key children_group_to_object_type_idx (object_type_id),
    key children_group_to_parent_idx (parent_id),
    constraint children_group_to_object_type foreign key (object_type_id) references object_types (object_type_id) 
        on delete set null on update cascade,
    constraint children_group_to_parent foreign key (parent_id) references data_objects (object_id) 
        on delete set null on update cascade
) engine=innodb default charset=utf8;

alter table data_objects 
    add constraint object_to_children_group foreign key (children_group_id) references children_groups (group_id)
    on delete set null on update cascade;

create table parameters (
    parameter_id bigint(20) unsigned not null,
    attribute_id bigint(20) unsigned default null,
    entity_id bigint(20) unsigned default null,
    primary key (parameter_id),
    key parameter_to_attribute_idx (attribute_id),
    key parameter_to_object_idx (entity_id),
    constraint parameter_to_object foreign key (entity_id) references data_objects (object_id) 
        on delete set null on update cascade,
    constraint parameter_to_attribute foreign key (attribute_id) references attributes (attribute_id)
        on delete set null on update cascade
) engine=innodb default charset=utf8;

create table parameter_values (
    value_id bigint(20) unsigned not null,
    type int(1) unsigned default null,
    text_value varchar(500) default null,
    date_value timestamp null default null,
    reference_id bigint(20) unsigned default null,
    list_entry_id bigint(20) unsigned default null,
    parameter_id bigint(20) unsigned default null,
    parameter_order int(6) unsigned default null,
    primary key (value_id),
    key param_to_reference_idx (reference_id),
    key param_to_list_value_idx (list_entry_id),
    key param_value_to_param_idx (parameter_id),
    constraint param_value_to_param foreign key (parameter_id) references parameters (parameter_id) 
        on delete set null on update cascade,
    constraint param_value_to_list_value foreign key (list_entry_id) references list_entries (list_entry_id) 
        on delete set null on update cascade,
    constraint param_value_to_reference foreign key (reference_id) references data_objects (object_id) 
        on delete set null on update cascade
) engine=innodb default charset=utf8;

create table if not exists id_generation_sequences (
    entity_set varchar(20) not null,
    last_id bigint(20) unsigned default null,
    primary key (entity_set),
    unique key idid_generation_sequences_unique (entity_set)
) engine=innodb default charset=utf8;

insert into
    id_generation_sequences (entity_set, last_id)
values
    ('data', 1000000000000000000)
on duplicate key update
    last_id = 1000000000000000000;
