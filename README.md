# audit-box

Audit-box is a Provenance Collector Rest API that transform data send to the api(JSON) form into a RDF datasets. Audit-box use RML to map data source into a RDF datasets.

## Installation

before running the program, you need to replace model.ttl and JSONReceiver.rml in /src/main/resources into your own based on your use cases.

go to the /Docker folder and type


```console
docker-compose build
```

and then run it with

```console
docker-compose up
```

Dont forget to change the config.yaml file in /src/main/resources/config.yaml based on your use cases.

