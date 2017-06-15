drop database DoingTogether;
create database DoingTogether;

use DoingTogether;

create table user_info (
	user_key int not null auto_increment primary key,
    id varchar(20) not null,
    pw varchar(40) not null,
    name varchar(20) not null,
    email varchar(40) not null,
    phone varchar(20) not null
);

create table sharing_info (
	sharing_key int not null auto_increment primary key,
    writer_key varchar(20) not null,
    write_time char(12) not null,
    title varchar(40) not null,
    priority int not null,
    people text not null,
    due_date char(12) not null,
    memo char(200) not null,
    images text not null,
    cheerup_key text
);

create table image_info (
	image_key int not null auto_increment primary key,
    encoded_image text not null
);

create table reply_info(
	reply_key int not null auto_increment primary key,
    sharing_key int not null,
    reply varchar(200) not null,
    writer_key int not null
);
