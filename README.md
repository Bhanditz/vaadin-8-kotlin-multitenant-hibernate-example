Multitenancy with Hibernate, Postgres, Vaadin 8 & Kotlin
=============================


This example application uses Postgres Table Inheritance to setup a Mutli-Tenant Database with seperate Schemas for each Tenant.

It is built using Kotlin, Hibernate, Spring Boot, and Vaadin8 for the UI and includes several Selenium Tests that could be used as a starter for testing Vaadin applications.

Hibernate switches tenants with a proxied EntityManagerFactory/Datasource and has an additional TenantFilter Column to allow cross-tenant selects from the master schema.

The example app allows you to register for an account, login and has a simple user management.

There are examples for mapping Pojos to Postgres JSON Columns for Hibernate and JOOQ inside.

There is a docker postgres image included. 

Run it with docker run -p5433:5432 eiswind/postgresql:9.6 to execute the Integration Tests or just to try the app.

There is an initial user demo@eiswind.de with password demo.

