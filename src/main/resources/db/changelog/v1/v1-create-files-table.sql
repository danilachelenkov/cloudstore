CREATE TABLE IF NOT EXISTS files_cloudstore
(
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    deleted boolean DEFAULT false,
    file_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    full_path character varying(255) COLLATE pg_catalog."default" NOT NULL,
    size bigint NOT NULL,
    updated_at timestamp(6) without time zone,
    user_id bigint NOT NULL,
    broken_file boolean NOT NULL DEFAULT false,
    CONSTRAINT files_cloudstore_pkey PRIMARY KEY (id),
    CONSTRAINT composit_file_index_1 UNIQUE (file_name, user_id, deleted),
    CONSTRAINT uk_17g8io6o7odr3ruowbd68fdn5 UNIQUE (full_path),
    CONSTRAINT fktjvrgt1vstgguumx5wu0m4tir FOREIGN KEY (user_id)
    REFERENCES users (id) MATCH SIMPLE
                            ON UPDATE NO ACTION
                            ON DELETE NO ACTION
    );



CREATE SEQUENCE IF NOT EXISTS files_cloudstore_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1
    OWNED BY files_cloudstore.id;


ALTER SEQUENCE files_cloudstore_id_seq
    OWNER TO postgres;

ALTER TABLE files_cloudstore ALTER COLUMN id SET DEFAULT nextval('files_cloudstore_id_seq');