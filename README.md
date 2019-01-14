# Volcano
Simple API for campsite booking.
## Documentation

### Setup

#### Requirements
 - Docker [https://docs.docker.com/install/]
 - Docker Compose [https://docs.docker.com/compose/install/]

#### Run the application

In order tu run the application you'll need to generate a jar file containing the application so you can deploy it and start using the endpoints. The jar generation can be achieved by running `mvn clean package` and it will generate the jar file into `./target` folder. Once we have that done, we can build a docker image and expose the API inside the container. For that process a `Dockerfile` and a `docker-compose` file were created. All you need to do is to run `docker-compose build` and `docker-compose run`.

Note that the Dockerfile does not create the jar file, since that is not a good practice. It's recommended to generate the jar file (or any kind of bundle in any programming language) by using a Continous Integration tool. For simplicity reasons I've created the jar and I kept the `./target` folder in this repo even if that is not a good practice (is highly recommended to ignore that kind of files and folders), so you don't need to install Maven locally. So, as a recap, you need to run the following commands to keep the application up and running:

 - `docker-compose build`
 - `docker-compose up`

If you want to delete the `./target` folder, you need to run `mvn clean package` first and then run the previous commands.

This application uses JPA + H2 (in-memory database because its simplicity), spring-boot and mockito. 12 unit tests are available in this project.

By default, I've setted the configuration to map the docker port to your local port `8080`. You can reach the API at `http://localhost:8080/booking`. Note that the root '/' doesn't do anything, since I wanted to give to the booking resource an specific URI, starting at `/booking` for semantic reasons. You can apply several methods to this resource, which are listed below.

**NOTE**: all responses have the same format: `{ error: ..., payload: ... }`. If the system returns an error status code, then `error` will have some error message and `payload` will be `null`. If the system process the request succesfully, `payload` *may* have some content, and error will be `null`.

### Get

Two options are available when applying the GET method:

 - `booking/` returns all registered bookings from a range of dates. Those dates `from` (beginning) and `to` (end) should be passed as parameters to the endpoint. If a date is not present, the API will assume you want the bookings from the next day and one month ahead.
 - `booking/{id}` returns the data for an specific booking. Id is the `uuid` that was returned when a booking is created via a `POST` method. It returns a list of bookings if you select a booking with more than 1 day.

#### Examples:
 - **Request #1:** *http://localhost:8080/booking/?from=2019-01-16&to=2019-01-17*
 - **Response #1:**
     ```
     {
        "error": null,
        "payload": [
            {
                "uuid": "f0693b89-2e4e-4aba-abfe-117fa254817e",
                "from": "2019-01-17",
                "to": "2019-01-17"
            },
            {
                "uuid": "f0693b89-2e4e-4aba-abfe-117fa254817e",
                "from": "2019-01-18",
                "to": "2019-01-18"
            },
            {
                "uuid": "f0693b89-2e4e-4aba-abfe-117fa254817e",
                "from": "2019-01-19",
                "to": "2019-01-19"
            }
        ]
    }
     ```
 - **Request #2:** *http://localhost:8080/booking/f0693b89-2e4e-4aba-abfe-117fa254817e*
 - **Response #2:**
     ```
     {
    "error": null,
    "payload": [
        {
            "uuid": "f0693b89-2e4e-4aba-abfe-117fa254817e",
            "from": "2019-01-17",
            "to": "2019-01-17"
        },
        {
            "uuid": "f0693b89-2e4e-4aba-abfe-117fa254817e",
            "from": "2019-01-18",
            "to": "2019-01-18"
        },
        {
            "uuid": "f0693b89-2e4e-4aba-abfe-117fa254817e",
            "from": "2019-01-19",
            "to": "2019-01-19"
        }
    ]
}
     ```
 

### Post

This method expect a body with a JSON media-type which should contains 5 keys: `email`, `name`, `lastName`, `date` (beginning) and `dateTo` (end). Is important to know that both `date` and `dateTo` should have the format `YYYY-MM-DD`. If the creation of the resource is succesful you will get an object containing the booking UUID. Dates are inclusive, this means that 2019-01-17 and 2019-02-19 will be taken as a 3-days booking.

#### Examples:

 - **Request:** *http://localhost:8080/booking*.
     ```
    {
        "email": "tomasjuarez@gmail.com",
        "name": "Tomas",
        "lastName": "Juarez",
        "date": "2019-01-17T21:23:41.114",
        "dateTo": "2019-01-19T21:23:41.114"
    }
     ```
 - **Response:**
    ```
    {
        "error": null,
        "payload": {
            "uuid": "6487b38d-f9aa-45d4-9578-5e1589c5f6f9",
            "from": "2019-01-17",
            "to": "2019-01-19"
        }
    }
    ```

### Patch
This method expects a path parameter like `/booking/{id}` which should contain a valid booking UUID returned by `POST`. Also, you need to attach a simple json in the body containing two keys: `date` (beginning) and `dateTo` (end) with the format `YYYY-MM-DD`. The dates are inclusive numbers: this means that 2019-01-17 and 2019-02-19 will be taken as a 3-days booking.

Note that if your booking was created with 3-days length, then if you update the dates, you should send 3 consecutive days. The same if you created a 2-days booking.

#### Examples:
 - **Request:** *http://localhost:8080/booking/6487b38d-f9aa-45d4-9578-5e1589c5f6f9*
     ```
     {
    	"date": "2019-01-14",
    	"dateTo": "2019-01-16"
    }
     ```
 - **Response:** *blank response with 204 (no-content) status.*

### Delete

This method expects a path parameter containing a valid UUID for a previous created booking in the form of `booking/{id}`.

#### Examples:
 - **Request:** *http://localhost:8080/booking/6487b38d-f9aa-45d4-9578-5e1589c5f6f9*
 - **Response:** *blank response with 204 (no-content) status.*