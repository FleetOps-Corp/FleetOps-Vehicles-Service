# Changelog

Todos los cambios notables de este proyecto se documentarán en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto se adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### Added
- Contenerización con Docker (`Dockerfile` multi-stage, usuario no-root, healthcheck).
- Orquestación local con Docker Compose (`docker-compose.yml`) para API + PostgreSQL.
- Pipeline de Integración Continua con GitHub Actions (`.github/workflows/ci.yml`): build, tests contra PostgreSQL real, cobertura con JaCoCo, verificación de estilo con Checkstyle, empaquetado y publicación de artifact.
- Workflow de Continuous Deployment a AWS EC2 preparado (`.github/workflows/deploy.yml`), deshabilitado hasta que exista infraestructura real.
- Preparación de SonarCloud (`sonar-project.properties`, `sonar-maven-plugin`), con análisis vía "Automatic Analysis" a nivel de organización (steps de CI-based analysis comentados intencionalmente).
- `.gitignore` y `.dockerignore` para excluir artefactos de build, entornos y logs.
- `CODEOWNERS` para revisión obligatoria de Pull Requests.
- `CONTRIBUTING.md` con estrategia de ramas (Git Flow simplificado), convención de Conventional Commits, flujo de Pull Requests y checklist previo a PR.
- `LICENSE` (MIT), adecuada para el estado actual del proyecto (académico, no distribuido comercialmente).
- Plantilla de Pull Request (`.github/PULL_REQUEST_TEMPLATE.md`).
- Plantillas de Issues en formato YAML moderno (`.github/ISSUE_TEMPLATE/bug_report.yml` y `feature_request.yml`).
- Configuración de Dependabot (`.github/dependabot.yml`) para actualizaciones semanales de Maven y GitHub Actions.
- Documentación ampliada en el README: Docker, Docker Compose, GitHub Actions (CI), Deploy a AWS EC2, SonarCloud, Calidad del proyecto, Versionamiento (SemVer) y Seguridad del repositorio.

### Changed
- `monitoring/prometheus.yml`: se reemplazó una IP pública real hardcodeada por un placeholder (`<EC2_HOST>`), para evitar exponer información de infraestructura en el repositorio.

### Fixed
- Se dejó de versionar `hs_err_pid28052.log` (crash dump de la JVM) mediante `git rm --cached`, conservando el archivo en disco.
