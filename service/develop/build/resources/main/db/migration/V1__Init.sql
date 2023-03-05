create table IF NOT EXISTS app_data(
    id bigserial primary key,
    date_created varchar(10) NOT NULL,
    last_modified_date varchar(50) NOT NULL,
    "name" varchar(255) NOT NULL,
    token varchar(255) NOT NULL,
    logo_id varchar(255) NOT NULL,
    owner_id bigserial NOT NULL
);