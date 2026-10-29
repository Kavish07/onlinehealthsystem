# Keep JSP, no REST API layer

Status: proposed

A Spring Boot app invites either exposing a JSON API (for a future SPA) or swapping JSP for a templating engine like Thymeleaf, and a future reader might assume one of those is coming. We're deliberately keeping JSP as the only view technology and not introducing an API layer: there is no API consumer today, `docs/PRD-spring-boot-migration.md` already treats UI/view-tech changes as a non-goal, and this review is scoped to backend code-health, not frontend modernization.

## Considered options
- **REST API + SPA frontend**: rejected — no consumer exists; purely speculative scope for a portfolio project.
- **Thymeleaf swap**: rejected — no behavioral or maintenance benefit over JSP once the controller layer is already migrated, since JSP rendering already works fine with Spring MVC.
