-- Um banco por microsserviço (database-per-service).
-- Executado automaticamente apenas na primeira inicialização do volume.
CREATE DATABASE orders;
CREATE DATABASE inventory;
CREATE DATABASE payments;
CREATE DATABASE notifications;
