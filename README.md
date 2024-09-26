# Semantic Search on Book Reviews

This repository contains code and resources to run a semantic (and fulltext) search engine specifically designed for
book reviews. It enables efficient searching by utilizing text embeddings and supports harvesting book metadata from
various sources, using international standards like MARC21 and ONIX.

The application leverages **Multilingual-E5-small** for generating text embeddings and **PostgreSQL** with **pgvector**
as vector store. This combination provides multilingual semantic search capabilities.

## Technologies

- **[Multilingual-E5-small](https://huggingface.co/intfloat/multilingual-e5-small)**: This pre-trained model is used for
  generating text embeddings in multiple languages.
- **[pgvector](https://github.com/pgvector/pgvector)**: A PostgreSQL extension for storing and querying vectors, used as
  the vector store in the search engine.
- **[Spring Boot 3](https://spring.io/projects/spring-boot)**: Application framework.

## Getting Started

Follow these steps to set up and run the application:

### 1. Create and run PostgreSQL database

Run the following command in the project directory:

```bash
docker compose up
```

This will start the PostgreSQL database with pgvector enabled.

### 2. Configure the Gateway

Select and configure the appropriate gateway for harvesting metadata by editing the application.yaml file. Available
options:

- oai-pmh
- bibbi
- bokbasen

### 3. Start the Application

The first run may take some time as it will download the necessary embedding models. Once the models are in place, the
application will be ready for use.

```bash
./mvnw spring-boot:run
```

### 4. Use the search engine

#### Semantic search

Visit ```http://localhost:8080/semantic``` in the browser and watch the results as the metadata harvesting progresses.
Enter a search query or leave it blank for a random choice (the first search hit will be the random choice and the rest
will be semantically similar books).

#### Fulltext search

Visit ```http://localhost:8080/fulltext``` in the browser and watch the results as the metadata harvesting progresses.

## Gateway Configuration

The gateway component is responsible for:

- Translation: It translates the metadata from the external service into a format the host application can process.
- Abstraction: It abstracts away the details of the remote service, handling the transformation of parameters and
  results.

The application supports three gateways. Custom mappers can be implemented as needed and activated by configuring the
appropriate values in the application.yaml file.

## Supported Gateways

### OAI-PMH

The OAI-PMH gateway harvests metadata using the Open Archives Initiative Protocol for Metadata Harvesting (OAI-PMH). It
supports retrieving bibliographic data in MARC21 format.

- **[OAI-PMH](https://www.openarchives.org/pmh/)**
- **[MARC21](https://www.loc.gov/marc/bibliographic/)**

Additional documentation for OAI-PMH from Biblioteksentralen (https://www.bibsent.no/):

- **[Ája OAI-PMH API](https://doc.aja.bs.no/hente/bibliografiske-data/oai-pmh.html)** (requires no authentication)

### ONIX

The ONIX gateway uses the ONIX format for metadata, commonly employed in the publishing industry. This is particularly
useful for harvesting data from large-scale book vendors.

- **[ONIX 3.0](https://www.editeur.org/93/Release-3.0-and-3.1-Downloads/)**

Additional documentation for ONIX from Bokbasen (https://www.bokbasen.no/):

- **[Bokbasen ONIX API](https://bokbasen.jira.com/wiki/spaces/api/pages/67993632/ONIX)** (requires authentication)

### Bibbi

The Bibbi gateway is used for integrating with the Bibbi metadata service. The gateway uses a format based on
Schema.org.

- **[Schema.org](https://schema.org/)**

Additional documentation for Bibbi from Biblioteksentralen (https://www.bibsent.no/):

- **[Bibbi Metadata REST API](https://bibliografisk.bs.no/#/)** (requires no authentication)
