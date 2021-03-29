FROM debezium/postgres:11

COPY inventory.sql /docker-entrypoint-initdb.d/