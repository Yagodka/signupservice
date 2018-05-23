# Distributed SignUp service

## Languages and technologies 
* Scala
* Akka Actors, Akka HTTP, Akka Streams, Akka Remoting 
* MongoDB
* Kafka
* Docker
    
### How to run?
* You must have [Docker](https://www.docker.com/community-edition) and [Docker-Compose](https://docs.docker.com/compose/) installed.
* Clone this repository
* Open terminal, navigate to the target folder
* Run: `docker-compose run`
    * It will download necessary images of Mongo, Kafka, and both, SignUp service and Persistence service
    * Run them and bind to the local ports (SignUp service - localhost:8080) 

### Validation
To validate this service locally I recommend to use [Postman](https://www.getpostman.com/apps).
After installation open the [collection link]((https://www.getpostman.com/collections/85eaf2296d4318e7c3e0)) to import predefined basic calls.

**cURL basic queries**
* SignUp
```bash
curl -X POST \
  http://localhost:8080/api/v1/sign-up \
  -H 'content-type: application/json' \
  -d '{
	"email": "example@mail.com",
	"password": "Passw0rd"
}'
```
* Query
```bash
curl -X GET \
  'http://localhost:8080/api/v1/query?uuid={{uuid}}'
```

