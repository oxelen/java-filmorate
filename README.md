![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.0-brightgreen)
![REST API](https://img.shields.io/badge/REST%20API-available-orange)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue)
![Maven](https://img.shields.io/badge/Maven-3.8.6-red)
# Filmorate

REST API for a movie recommendation and social platform where users can rate films, like them, and connect with friends.

The application allows users to discover popular movies based on ratings and social interactions.

## Features

### Films
- Create a film
- Update film information
- Get film by ID
- Like / remove like from a film
- Get most popular films

### Users
- Create and update users
- Get user by ID
- Add and remove friends
- View user friends
- Get common friends between users

### Additional Data
- Film genres
- Age ratings (MPA)

## Tech Stack

- Java
- Spring Boot
- Spring MVC
- JDBC / Spring Data
- PostgreSQL
- Maven
- Lombok
- JUnit

## API Example

### Create User

POST /users

```json
{
  "email": "user@example.com",
  "login": "userlogin",
  "name": "John Doe",
  "birthday": "1990-01-01"
}
```

### Add Like to Film

PUT /films/{filmId}/like/{userId}

### Get Popular Films

GET /films/popular?count=10

## Project Structure

```
controller
service
repository
model
dto
exception
validation
```

## Database

The project uses a relational database to store:

- users
- films
- likes
- friendships
- genres
- ratings

Example query for most popular film:

```sql
SELECT film_id
FROM film_likes
GROUP BY film_id
ORDER BY COUNT(user_id) DESC
LIMIT 1;
```

## Testing

Unit tests are implemented using:

- JUnit
- MockMvc

## How to Run

1. Clone repository

```
git clone https://github.com/oxelen/java-filmorate.git
```

2. Run application

```
./mvnw spring-boot:run
```

3. API will start on

```
http://localhost:8080
```
