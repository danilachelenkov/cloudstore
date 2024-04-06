alter table files_cloudstore
    add column IF NOT EXISTS broken_file boolean NOT NULL DEFAULT false;

