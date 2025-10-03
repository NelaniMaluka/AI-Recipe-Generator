# 🍲 AI Recipe Generator Backend

This is the backend service for the AI Recipe Generator. It provides APIs for generating, storing, and sharing recipes, powered by AI models from Hugging Face and external image-generation services.

## 🚀 Features

- **AI-powered recipe generation**
  - Uses Hugging Face models for generating recipe text.
  - Integrates with an external API to generate recipe images.
- **Email recipe sharing**
  - Asynchronous recipe emails using Spring’s `@Async` and SMTP integration.
- **Caching & scheduled updates**
  - Recipes are cached with daily refresh for performance.
- **Database integration**
  - Backed by Microsoft SQL Server with JPA/Hibernate.
  - Includes pagination for efficient queries.
- **Testing**
  - Unit tests for services and controllers.
- **Developer experience**
  - Uses Lombok for boilerplate-free entities and DTOs.
  - Dockerized for consistent deployments.
  - Dependabot enabled for automatic dependency updates.

## 🛠️ Tech Stack

- Java 17 / Spring Boot
- MS SQL Server (database)
- Hibernate / JPA (ORM)
- Lombok (annotations)
- JUnit + Mockito (unit testing)
- Docker (containerization)
- SMTP (email service)
- Caffeine Cache (in-memory caching)
- Dependabot (dependency management)

## 📦 Getting Started

### Prerequisites

- Java 17+
- Maven or Gradle
- Docker (for containerized setup)
- SQL Server running locally or in Azure

### Environment Variables

Create an `.env` file or use system environment variables:

```plaintext
# Database
SPRING_DATASOURCE_URL=jdbc:sqlserver://localhost:1433;databaseName=recipe_db
SPRING_DATASOURCE_USERNAME=your_user
SPRING_DATASOURCE_PASSWORD=your_password

# Hugging Face API
HUGGINGFACE_API_KEY=your_hf_api_key

# Image Generation API
IMAGE_API_KEY=your_image_api_key

# SMTP for emails
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_email_password
```

### ▶️ Running the Project

#### Local (dev)
```bash
./mvnw spring-boot:run
```

#### With Docker
Build and run:
```bash
docker build -t ai-recipe-backend .
docker run -p 8080:8080 --env-file .env ai-recipe-backend
```

## 🧪 Testing

Run all unit tests:
```bash
./mvnw test
```

## 🔑 API Endpoints

| Method | Endpoint                              | Description                                      | Parameters                                                                 |
|--------|---------------------------------------|--------------------------------------------------|---------------------------------------------------------------------------|
| GET    | `/api/recipe/meal-types`             | Retrieve all available meal types                | None                                                                     |
| GET    | `/api/recipe/date-filters`           | Retrieve all available date filters              | None                                                                     |
| GET    | `/api/recipe/{publicId}`            | Get a recipe by its public ID                   | `publicId` (required, string)                                             |
| GET    | `/api/recipe`                       | Search recipes by keyword with pagination       | `searchWord` (required, string), `page` (default: 0), `size` (default: 5) |
| GET    | `/api/recipe/all-recipes`           | Get recipes filtered by time, meal type, and date with pagination | `startTime` (default: 0), `endTime` (default: 180), `mealType` (optional, enum), `dateFilter` (default: ALL), `page` (default: 0), `size` (default: 20) |
| POST   | `/api/recipe/email-recipe`          | Email a recipe asynchronously                   | `email` (required, valid email), `publicId` (required, string)            |

## 📌 Notes

- **Asynchronous Tasks**: Recipe generation and email sending run in background threads.
- **Caching**: Recipes are cached with automatic daily refresh.
- **Dependabot**: Keeps dependencies secure and up-to-date.
- **Validation**: Endpoints include input validation (e.g., non-blank IDs, valid email formats).

## 📜 License

This project is for educational and personal use. Update with your chosen license if you make it public.