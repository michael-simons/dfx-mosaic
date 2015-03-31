create table images (
    id integer not null auto_increment,
    absolute_file_name varchar(4096) not null,
    taken_on date not null,
    average_color integer not null,
    PRIMARY KEY (id)
);