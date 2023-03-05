create table IF NOT EXISTS user_data(
                          id bigserial primary key,
                          first_name varchar(255) NOT NULL,
                          last_name varchar(255) NOT NULL,
                          password varchar(255) NOT NULL,
                          birthday varchar(10) NOT NULL,
                          gender varchar(5) NOT NULL,
                          role varchar(10) NOT NULL,
                          date_created varchar(10) NOT NULL,
                          last_modified_date varchar(50) NOT NULL
);

create table IF NOT EXISTS user_phone(
                          id bigserial primary key,
                          date_created varchar(10) NOT NULL,
                          last_modified_date varchar(50) NOT NULL,
                          phone_number varchar(255) NOT NULL,
                          confirmed boolean NOT NULL,
                          owner_id bigserial NOT NULL,
                          CONSTRAINT "owner_to_user_FK" FOREIGN KEY (owner_id)
                              REFERENCES user_data (id)
);

create table IF NOT EXISTS user_email(
                           id bigserial primary key,
                           date_created varchar(10) NOT NULL,
                           last_modified_date varchar(50) NOT NULL,
                           email varchar(255) NOT NULL,
                           confirmed boolean NOT NULL,
                           owner_id bigserial NOT NULL,
                           CONSTRAINT "owner_to_user_FK" FOREIGN KEY (owner_id)
                            REFERENCES user_data (id)
);

create table IF NOT EXISTS user_avatar(
                            id bigserial primary key,
                            date_created varchar(10) NOT NULL,
                            last_modified_date varchar(50) NOT NULL,
                            file_id varchar(255) NOT NULL,
                            owner_id bigserial NOT NULL,
                            active boolean not null,
                            CONSTRAINT "owner_to_user_FK" FOREIGN KEY (owner_id)
                                REFERENCES user_data (id)
);
