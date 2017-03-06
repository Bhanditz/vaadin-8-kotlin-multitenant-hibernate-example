revoke connect on DATABASE xino3 from demo;
revoke ALL PRIVILEGES ON master.user_user from demo;
drop schema demo cascade;
drop schema master cascade;
drop EXTENSION pgcrypto CASCADE ;
drop FUNCTION create_tenant_tables(varchar,varchar);
drop table databasechangeloglock;
drop table databasechangelog;
drop role demo;