## Reconciliation Engine - gRPC Server

Matches incoming internal and external trades from a gRPC client and returns the results.

Proto definitions [here](src/main/proto).

### Usage

```sh
mvn clean package

java -jar target/engine-1.0-SNAPSHOT.jar
```
