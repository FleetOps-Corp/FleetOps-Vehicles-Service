# Fase 4 — Validación del Pipeline de CI (GitHub Actions)

> Estado: **Pipeline en verde** ✅ (commit `cce4daa`, workflow `CI - FleetOps Vehicles`)

Este documento explica cómo verificar que el pipeline de Integración Continua sigue funcionando correctamente, cómo leer sus resultados y cómo diagnosticar los errores más comunes. Está pensado para cualquier miembro del equipo, no solo para quien lo configuró.

---

## 1. Qué valida el pipeline actualmente

El workflow `.github/workflows/ci.yml` corre en cada `push`/`pull request` a `main` o `develop`, y ejecuta en orden:

| # | Step | Qué garantiza |
|---|------|----------------|
| 1 | Checkout | El runner tiene una copia exacta del repositorio en ese commit |
| 2 | Validar Maven Wrapper | `mvnw` es ejecutable (`chmod +x`) y responde correctamente |
| 3 | Configurar JDK 21 (Temurin) | La build usa la misma versión de Java que producción |
| 4 | Cache de Maven | Acelera runs sucesivos reutilizando `~/.m2/repository` |
| 5 | Compilar | El código fuente compila sin errores |
| 6 | Ejecutar tests | La suite de tests pasa contra un Postgres real (service container) |
| 7 | Reporte JaCoCo | Se genera cobertura de código (`target/site/jacoco/`) |
| 8 | Checkstyle | Se reportan violaciones de estilo (no bloquea el build todavía) |
| 9 | Empaquetar | Se genera el jar ejecutable final |
| 10 | Subir Artifact | El jar queda disponible para descarga desde la UI de Actions |

---

## 2. Cómo verificar un run exitoso

1. Ve a la pestaña **Actions** del repositorio.
2. Entra al run más reciente del branch/PR correspondiente.
3. Confirma:
   - El job `Build, Test & Quality Checks` tiene ✅ verde en todos los steps.
   - El step **Ejecutar tests** no reporta `Tests run: ... Errors: 0` (si hay errores, el job ya habría fallado, pero igual conviene mirar el resumen).
   - El step **Subir jar como artifact** aparece completado y, al final de la página del run, existe un artifact llamado `fleetops-vehicles-jar` descargable.
4. (Opcional) Descarga el artifact y verifica que el jar arranca localmente:
   ```powershell
   java -jar fleetops-vehicles-*.jar
   ```

---

## 3. Cómo leer los logs de cada step

- Cada step es una sección colapsable; haz clic para expandir su output completo.
- El step **postgres** (dentro de `services`) no aparece como step normal, pero su estado de salud se refleja indirectamente: si el healthcheck (`pg_isready`) nunca pasa, el job entero queda colgado/fallido antes de llegar a "Ejecutar tests".
- El resumen de tests (`Tests run: X, Failures: Y, Errors: Z, Skipped: W`) aparece en el log del step **Ejecutar tests**, igual que en un `mvn test` local.
- El reporte de Checkstyle (`target/checkstyle-result.xml`) no se sube como artifact todavía — solo se ve el conteo de violaciones en el log del step correspondiente.

---

## 4. Errores comunes y cómo diagnosticarlos

| Síntoma | Causa probable | Cómo confirmarlo |
|---|---|---|
| `Permission denied` al ejecutar `./mvnw` | El step "Validar Maven Wrapper" no llegó a ejecutar `chmod +x`, o se reordenaron los steps | Revisar que el `chmod +x mvnw` esté antes de cualquier otro `./mvnw` |
| `FATAL: no existe la base de datos "fleetops_vehicles"` | El *service container* de Postgres no inició o las credenciales no coinciden con `application.properties` | Revisar la sección `services.postgres` en `ci.yml` (`POSTGRES_DB/USER/PASSWORD`) |
| Job completo en rojo sin ningún step marcado | Timeout esperando que el `healthcheck` de Postgres pase | Revisar logs crudos del job (no de un step particular) buscando mensajes de Docker sobre el contenedor `postgres` |
| `Could not resolve dependencies` / timeouts de red | Problema transitorio de Maven Central o del runner de GitHub | Reintentar el run (botón "Re-run jobs") |
| Checkstyle reporta muchas violaciones pero el job sigue verde | Comportamiento esperado — `failOnViolation=false` en `pom.xml` | No es un error; es solo visibilidad de deuda técnica |
| Cache de Maven no se reutiliza (build más lento de lo esperado) | Cambió `pom.xml` (la key de cache incluye `hashFiles('**/pom.xml')`) | Esperado tras cualquier cambio de dependencias/plugins |

---

## 5. Checklist de cierre de la Fase 4

- [x] El pipeline corre automáticamente en push/PR a `main`/`develop`.
- [x] Los 10 steps requeridos están presentes y en el orden correcto.
- [x] El job completo terminó en verde en al menos un run real.
- [x] El artifact del jar se genera y es descargable.
- [ ] (Pendiente, fuera de esta fase) Activar análisis de SonarCloud — punto ya marcado con `TODO-FASE-SONARCLOUD` en `ci.yml`.
- [ ] (Pendiente, fuera de esta fase) Build/push de imagen Docker — punto ya marcado con `TODO-FASE-DOCKER`.
- [ ] (Pendiente, fuera de esta fase) Despliegue a AWS / CD — punto ya marcado con `TODO-FASE-CD-AWS`.

---

## 6. Próximos pasos sugeridos (no implementados aún)

1. Configurar los `Secrets` del repositorio (`SONAR_TOKEN`, `SONAR_HOST_URL`) y activar el step comentado de SonarCloud.
2. Definir un registry de contenedores (GHCR, Docker Hub o ECR) y agregar el step de build/push de la imagen Docker ya validada.
3. Diseñar un workflow separado de Continuous Deployment hacia AWS EC2, reutilizando `docker-compose.yml`.

Ninguno de estos pasos debe iniciarse hasta que el equipo confirme que esta fase de CI es estable y comprendida por todos.
