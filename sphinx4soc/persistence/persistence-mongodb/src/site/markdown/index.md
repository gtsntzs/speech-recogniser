
## Purpose

Using MongoDB as the persistence layer. MongoDB is the easiest way to get things up and working. It requires to import all data like the speech database, trained models along with their dictionaries and model definitions.

## Storing the speech database

MongoDB offers a place to store files in a binary form using the GridFS class, the files are stored in the collection fs.files. The schema spesifies the metadata stored along with the files. Importing the data to the database can be done either using the ... or with a custom script written in a language supported by mongoDB. The database schema has to be preserved, though.

## Usage

In order to building this module it is needed a mongoDB instance up and running. The MongoDB instance has to run with host IP "172.17.0.3" at 27017 port, otherwise one could change the properties file in test resources. Best way of starting a mongoDB instance is to use the <a href="https://www.docker.com/" target="_blank">docker</a> platform. Hopefully, by the end of this year there will be an official maven plugin to make integration tests of this kind much simpler. Another option is to skip the integration tests with the command

```sh
  mvn install -DskipITs
```

## Schema

![mongodbSchema1](images/sphinx4socMongodb.png "Schema MongoDB")

