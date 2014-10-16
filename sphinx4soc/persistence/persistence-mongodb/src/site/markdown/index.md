----

## Purpose

Using MongoDB as the persistence layer. MongoDB is the easiest

## Storing the speech database

MongoDB offers a place to store files in a binary form using the GridFS class, the files are stored in the collection fs.files. The schema spesifies the metadata store along with a file.

## Schema

![mongodbSchema1](images/sphinx4socMongodb.png "Schema MongoDB")

## API

*Storing Sphinx Data object*
**Arguments**:
+ **dataFrame** as Data object --> the
+ **headers** as Map<String, Object> object --> contains the metadata to  store.
  + **experiment** as Experiment object --> the current experiment
  + **streamName** as String --> the name of a file
  + **endpointName** as String --> the name of the endpoint created the data.
+ **Exception** --> when the database or collection does not exist.

