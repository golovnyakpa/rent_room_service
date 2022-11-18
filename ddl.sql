create table rooms (
	room_id serial PRIMARY KEY,
	room_number VARCHAR(50) NOT NULL UNIQUE
);

create table future_rents (
	rent_id serial PRIMARY KEY,
	room VARCHAR(50) NOT NULL,
	dttm_start TIMESTAMP NOT NULL,
	dttm_end TIMESTAMP NOT NULL,
	renter VARCHAR (70),
	FOREIGN KEY (room)
      REFERENCES rooms (room_number)
);

create table past_rents (
	rent_id serial PRIMARY KEY,
    room VARCHAR(50) NOT NULL,
   	dttm_start TIMESTAMP NOT NULL,
   	dttm_end TIMESTAMP NOT NULL,
   	renter VARCHAR (70),
   	FOREIGN KEY (room)
   	REFERENCES rooms (room_number)
);

create table users (
	user_id serial PRIMARY KEY,
	user_login VARCHAR(50) NOT NULL UNIQUE
);

create table users (
	id serial PRIMARY KEY,
	login VARCHAR(70) NOT NULL UNIQUE,
	password VARCHAR(350) NOT NULL
);
