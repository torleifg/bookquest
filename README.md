# Semantic & Full-Text Search Engine for Books

This repository contains code and resources to run a hybrid (semantic and full-text) search engine for books. 
It utilizes text embeddings and supports harvesting book metadata from various sources, using international standards 
like MARC21 and ONIX 3.

The application leverages **Multilingual-E5-small** for generating text embeddings and **PostgreSQL** with **pgvector**
as database and vector store. This provides multilingual semantic search capabilities.

## Technologies

- **[Multilingual-E5-small](https://huggingface.co/intfloat/multilingual-e5-small)**: This pre-trained model is used for
  generating text embeddings.
- **[pgvector](https://github.com/pgvector/pgvector)**: A PostgreSQL extension for storing and querying vectors, used as
  the vector store in the search engine.

## Getting Started

Follow these steps to set up and run the application:

### 1. Create and run PostgreSQL database

Run the following command in the project directory:

```shell
docker compose up
```

This will start the PostgreSQL database with pgvector enabled.

### 2. Configure the Gateway

Select and configure the appropriate gateway and service-uri for harvesting metadata by editing 
```runner/src/main/resources/application.yaml```.

Available options:

- oai-pmh
- bibbi
- bokbasen

**Example:**

```yaml
scheduler:
  enabled: true
  initial-delay: 5
  fixed-delay: 3600

gateway:
  type: bibbi

bibbi:
  service-uri: https://bibliografisk.bs.no/v1/publications/harvest
  ttl: 5
  mapper: default
  limit: 100
  query: type:(audiobook OR book)
```

### 3. Start the Application

The first run may take some time as it will download the necessary embedding models. Once the models are in place, the
application will be ready for use.

```shell
./gradlew bootRun
```

### 4. Use the search engine

Visit ```http://localhost:8080``` in the browser and watch the results as the metadata harvesting progresses. Enter a 
query for hybrid search or leave it blank for semantic similarity search (the first search hit will be a random choice 
and the rest will be semantically similar books). The hybrid search is based on Reciprocal Rank Fusion (RRF), an 
algorithm used for combining multiple ranked lists of search results to improve the overall ranking quality, in this
case to combine full-text and vector search results.


Modify weights etc. for full-text search by editing the repeatable migration located at:

```shell
adapter/persistence/src/main/resources/db/migration/R__search_function.sql
```

## Gateway

The gateway abstracts away the details of the external services and transforms metadata from the external services into
a common model. The application supports three gateways: OAI-PMH (MARC21), Bokbasen (ONIX) and Bibbi. Custom mappers can
be implemented as needed, configured in the config class of the respective gateway and activated by configuring the 
appropriate values in ```runner/src/main/resources/application.yaml```.

### OAI-PMH

The OAI-PMH gateway harvests metadata using the Open Archives Initiative Protocol for Metadata Harvesting (OAI-PMH). It
supports retrieving bibliographic data in MARC21 format.

- **[OAI-PMH](https://www.openarchives.org/pmh/)**
- **[MARC21](https://www.loc.gov/marc/bibliographic/)**

Additional documentation for OAI-PMH from Biblioteksentralen (https://www.bibsent.no/):

- **[Ája OAI-PMH API](https://doc.aja.bs.no/hente/bibliografiske-data/oai-pmh.html)** (requires no authentication)

Additional documentation for OAI-PMH from Nasjonalbiblioteket (https://www.nb.no/):

- **[Nasjonalbibliografien og spesialbibliografiene OAI-PMH API](https://bibliotekutvikling.no/kunnskapsorganisering/metadata-fra-nasjonalbiblioteket/hosting-av-nasjonalbibliografien-og-spesialbibliografien/)** (requires no authentication)


### Bokbasen

The Bokbasen gateway uses the ONIX format for metadata, commonly employed in the publishing industry. This is
particularly useful for harvesting data from large-scale book vendors.

- **[ONIX 3.0](https://www.editeur.org/93/Release-3.0-and-3.1-Downloads/)**

Additional documentation for ONIX from Bokbasen (https://www.bokbasen.no/):

- **[Bokbasen ONIX API](https://bokbasen.jira.com/wiki/spaces/api/pages/67993632/ONIX)** (requires authentication)

### Bibbi

The Bibbi gateway is used for integrating with the Bibbi metadata service. The gateway uses a format based on
Schema.org.

- **[Schema.org](https://schema.org/)**

Additional documentation for Bibbi from Biblioteksentralen (https://www.bibsent.no/):

- **[Bibbi Metadata REST API](https://bibliografisk.bs.no/#/)** (requires no authentication)

## Text classification

Instructions for extracting a dataset for fine-tuning a BERT-based model for multi-label classification of book
reviews: https://github.com/torleifg/book-reviews-genre-classification

```shell
psql -h localhost -p 5433 -U username -d postgres
```

Extract example dataset using genre and form as labels.

```postgresql
create temp table temp_export as
select
  concat(metadata->>'title', '. ', metadata->>'description') as text,
  jsonb_agg(distinct genre_terms->>'term' order by genre_terms->>'term') as labels
from
  book,
  lateral jsonb_array_elements(metadata->'genreAndForm') as genre_terms
where
  metadata->>'description' is not null
  and metadata->>'description' <> ''
  and length(metadata->>'description') > 200
  and metadata->'genreAndForm' is not null
  and jsonb_array_length(metadata->'genreAndForm') > 0
  and genre_terms->>'language' = 'nob'
group by text;
```

```shell
\copy temp_export to '~/dataset.csv' with csv header delimiter ';';
```