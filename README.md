# Cloud file storage
Multi-user file cloud. Users of the service can use it to upload and store files.

## Functionality
### Users:
- sign up
- log in
- log out
  
## Actions you can do with file/directory
* Upload to server
* Download from server
* Rename
* Delete

## Project Stack

* Java 17
* AWS S3 Minio
* Spring Boot
* Spring Security
* Spring Data JPA
* Redis
* PostgreSQL
* Session Management
* Docker
* Docker compose
* Logging
* TestContainers, Integration tests
* Thymeleaf, HTML, CSS, JS

## Local deployment guide
1. Clone repository

```shell
git clone https://github.com/TurboGoose/cloud-file-storage.git
```

2. `cd` to the root folder of the cloned repository 
3. Run Docker compose stack for local development (Docker have to be installed and running)
4. Create database cloud_drive

```shell
docker-compose up -d
```

5. Run application

```shell
./mvnw spring-boot:run
```
