# Guía de Contribución — FleetOps Vehicles

Gracias por contribuir a este microservicio. Este documento define las normas mínimas para que un equipo de ~30 desarrolladores pueda colaborar de forma ordenada y consistente.

---

## Estrategia de ramas (Git Flow simplificado)

Usamos una versión simplificada de Git Flow con las siguientes ramas:

| Rama | Propósito |
|---|---|
| `main` | Código estable, siempre desplegable. Solo recibe merges vía Pull Request desde `develop` o `hotfix/*`. |
| `develop` | Rama de integración. Todas las `feature/*` y `fix/*` se integran aquí primero. |
| `feature/*` | Una rama por funcionalidad nueva, creada desde `develop`. Ejemplo: `feature/reservas-vehiculo`. |
| `fix/*` | Corrección de un bug no urgente, creada desde `develop`. Ejemplo: `fix/validacion-fecha-reserva`. |
| `hotfix/*` | Corrección urgente sobre `main`, para bugs críticos en producción. Se mergea a `main` y también a `develop`. |

### Convención de nombres de rama

```
feature/<descripcion-corta-en-kebab-case>
fix/<descripcion-corta-en-kebab-case>
hotfix/<descripcion-corta-en-kebab-case>
```

Ejemplos:
- `feature/agregar-endpoint-historial`
- `fix/validacion-fecha-reserva`
- `hotfix/fix-null-pointer-reserva`

### Flujo básico

```bash
git checkout develop
git pull origin develop
git checkout -b feature/mi-nueva-funcionalidad

# ... trabajar, commitear ...

git push origin feature/mi-nueva-funcionalidad
# Abrir Pull Request: feature/mi-nueva-funcionalidad -> develop
```

---

## Flujo de Pull Requests

1. Crea tu rama `feature/*` (o `hotfix/*`) desde `develop` (o `main`, para hotfixes).
2. Haz commits pequeños y frecuentes, siguiendo la convención de abajo.
3. Antes de abrir el PR, actualiza tu rama con los últimos cambios de `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout feature/mi-rama
   git merge develop
   ```
4. Abre el Pull Request contra `develop` (o `main` solo para `hotfix/*`).
5. Completa la plantilla de PR (ver [.github/PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md)) y el checklist de esta guía.
6. Espera a que el pipeline de CI pase (build, tests, JaCoCo, Checkstyle).
7. Se requiere al menos **una aprobación** de un revisor (ver [CODEOWNERS](.github/CODEOWNERS)) antes de hacer merge.
8. Usa **squash merge** o **merge commit** según se acuerde en el equipo, evitando rebases forzados sobre ramas compartidas.
9. Elimina la rama `feature/*`/`hotfix/*` después del merge.

---

## Convención de commits (Conventional Commits)

Usamos [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/):

```
<tipo>(<alcance opcional>): <descripción corta en presente>

[cuerpo opcional]

[footer opcional]
```

### Tipos permitidos

| Tipo | Uso |
|---|---|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de un bug |
| `docs` | Cambios solo de documentación |
| `style` | Formato, espacios, punto y coma (sin cambios de lógica) |
| `refactor` | Cambio de código que no agrega funcionalidad ni corrige bugs |
| `test` | Agregar o corregir tests |
| `chore` | Tareas de mantenimiento (dependencias, configuración, CI/CD) |
| `perf` | Cambios que mejoran el rendimiento |

### Ejemplos

```
feat(vehiculos): agregar endpoint de historial de estados
fix(reservas): corregir validacion de fechas superpuestas
docs(readme): agregar seccion de Docker Compose
chore(ci): agregar cache de dependencias Maven
```

---

## Checklist antes de abrir un Pull Request

Antes de abrir tu PR, verifica que:

- [ ] El proyecto compila localmente: `./mvnw clean compile`
- [ ] Todos los tests pasan localmente: `./mvnw test`
- [ ] Checkstyle no reporta violaciones nuevas: `./mvnw checkstyle:check`
- [ ] Probaste el cambio corriendo el proyecto con Docker Compose (si aplica): `docker compose up -d --build`
- [ ] Actualizaste la documentación relevante (README, comentarios, este archivo) si el cambio lo amerita
- [ ] Tu rama está actualizada con `develop` (sin conflictos)
- [ ] Los commits siguen la convención de Conventional Commits
- [ ] No modificaste archivos fuera del alcance de tu tarea (evita cambios accidentales en `pom.xml`, workflows, Dockerfile, etc. si no es el objetivo del PR)
- [ ] Completaste la plantilla de PR (`.github/PULL_REQUEST_TEMPLATE.md`)

---

## Cómo ejecutar el proyecto con Docker

```bash
# 1. Copiar variables de entorno
cp .env.example .env

# 2. Levantar el stack completo (API + PostgreSQL)
docker compose up -d --build

# 3. Verificar que todo esté saludable
docker compose ps
curl http://localhost:8081/actuator/health

# 4. Apagar
docker compose down
```

Más detalle en la sección [Docker Compose](README.md#docker-compose) del README.

---

## Cómo ejecutar el CI localmente

El pipeline de GitHub Actions (`.github/workflows/ci.yml`) se puede reproducir localmente paso a paso:

```bash
./mvnw --version                 # Validar Maven Wrapper
./mvnw -B clean compile           # Compilar
./mvnw -B test                    # Ejecutar tests (requiere PostgreSQL local o via Docker)
./mvnw -B jacoco:report           # Reporte de cobertura
./mvnw checkstyle:check           # Estilo de código
./mvnw -B clean package -DskipTests   # Empaquetar
```

> Si no tienes PostgreSQL instalado localmente, puedes levantarlo con:
> ```bash
> docker compose up -d postgres
> ```

---

## Normas básicas para el equipo

- **No commitear** archivos generados (`target/`), secretos (`.env`), ni logs (`*.log`, `hs_err_pid*`) — ya están cubiertos por `.gitignore`.
- **No modificar** `pom.xml`, workflows de CI/CD, `Dockerfile` o `docker-compose.yml` sin coordinarlo con el equipo, ya que afectan a todos los desarrolladores y el pipeline compartido.
- **Un PR, un propósito**: evita mezclar refactors grandes con features o fixes no relacionados.
- **Revisiones respetuosas y constructivas**: el objetivo del code review es mejorar el código, no criticar a la persona.
- Ante dudas sobre la arquitectura o el alcance de un cambio, consulta con los responsables listados en [CODEOWNERS](.github/CODEOWNERS) antes de implementar.
