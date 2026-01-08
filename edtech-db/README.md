# EdTech DB Module

This module contains all database-related components:

## Package Structure

```
com.task.edtech.db
├── entity/          # JPA entities (Provider, Course, enums)
├── repository/      # Spring Data JPA repositories
└── service/         # Business logic services
```

## Entities

- `Provider` - Course provider entity
- `Course` - Course entity
- `CourseMode` - Enum (ONLINE, IN_PERSON)
- `CourseCategory` - Enum (YOGA, CODING, MUSIC, etc.)

## Next Steps

1. Create repositories in `repository` package
2. Create services in `service` package
3. All entities are already created in `entity` package

