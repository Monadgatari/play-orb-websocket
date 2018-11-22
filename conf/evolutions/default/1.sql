# --- !Ups

CREATE TABLE counters(id SERIAL PRIMARY KEY, x integer NOT NULL);
INSERT INTO counters(x) VALUES(0);

# --- !Downs

DROP TABLE counters;
