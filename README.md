# Plataforma de Gestión de Cursos en Línea — EFT CDY2204 (Semana 9)

## Caso resuelto

Plataforma donde los **estudiantes** se inscriben a cursos, acceden al contenido y son
evaluados; y los **instructores** gestionan sus cursos, materiales y calificaciones en
tiempo real.

## Cómo se cumple cada requisito de la pauta

| Requisito                                                         | Cómo se resuelve                                                                                                                                                        |
| ----------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Framework Spring / Spring Boot                                    | Todo el backend (`pom.xml`, `@SpringBootApplication`)                                                                                                                   |
| Endpoints JSON                                                    | Todos los controllers devuelven `ResponseEntity` con JSON                                                                                                               |
| Colas en RabbitMQ, productor y consumidor en Java, cola en Docker | `RabbitMQConfig`, `InscripcionProducer` (productor), `InscripcionColaConsumerService` (consumidor). RabbitMQ corre en contenedor Docker vía `docker-compose.yml`        |
| IdaaS (Azure)                                                     | `SecurityConfig` valida JWT emitido por Azure AD B2C (`issuer-uri` / `jwk-set-uri`), con roles `estudiante` / `instructor` vía claim personalizado `extension_rolCurso` |
| Backend securitizado con Spring Security                          | `SecurityConfig` + OAuth2 Resource Server                                                                                                                               |
| Microservicio BFF que orquesta colas                              | El propio microservicio `cursos`: `InscripcionController` expone `/inscripciones/inscribir` (produce) y `/inscripciones/procesar-cola` (consume)                        |
| Mínimo 2 endpoints que llamen a una cola (1 producir, 1 consumir) | `POST /inscripciones/inscribir` (producir) y `POST /inscripciones/procesar-cola` (consumir)                                                                             |
| Almacenamiento Cloud                                              | AWS S3 vía `S3Repository` / `S3RepositoryImpl`, usado por `CursoController` para materiales de curso (`/cursos/subir`, `/cursos/descargar`)                             |
| API Manager                                                       | Todos los endpoints (`/cursos/**`, `/inscripciones/**`, `/calificaciones/**`) quedan documentados para su registro en el API Manager (ver Word de configuración)        |
| Despliegue con pipeline CI/CD sobre la nube                       | `.github/workflows/main.yml`: build de imagen Docker, push a DockerHub, despliegue por SSH a instancia EC2                                                              |
| Docker para el desarrollo                                         | `Dockerfile` (build multi-etapa) + `docker-compose.yml` (RabbitMQ + app)                                                                                                |

## Endpoints principales

### Cursos y materiales (`/cursos`)

- `POST /cursos/crear` — instructor crea material (EFS)
- `POST /cursos/subir` — instructor sube material a S3
- `GET /cursos/descargar` — estudiante/instructor descarga material
- `PUT /cursos/actualizar` — instructor actualiza material
- `DELETE /cursos/eliminar` — instructor elimina material
- `GET /cursos/consultar` — estudiante/instructor consulta materiales disponibles

### Inscripciones y colas (`/inscripciones`)

- `POST /inscripciones/inscribir` — estudiante se inscribe (**productor** → `cola.inscripciones`)
- `POST /inscripciones/procesar-cola` — instructor procesa la cola (**consumidor** → persiste en Oracle)

### Calificaciones (`/calificaciones`)

- `POST /calificaciones/registrar` — instructor registra nota de un examen
- `GET /calificaciones/consultar` — estudiante/instructor consulta notas

## Variables de entorno necesarias

Ver `docker-compose.yml` / `application.yml`: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`,
`AWS_SESSION_TOKEN`, `S3_BUCKET_NAME`, `AZURE_ISSUER_URI`, `AZURE_JWK_SET_URI`,
`ORACLE_DB_URL`, `ORACLE_DB_USER`, `ORACLE_DB_PASSWORD`, `RABBITMQ_HOST`, `RABBITMQ_PORT`,
`RABBITMQ_USER`, `RABBITMQ_PASSWORD`.

## Frontend de demo

En `frontend/` hay una app estática (HTML/CSS/JS + MSAL Browser) que hace login contra
Azure AD B2C y consume todos los endpoints del backend, pensada para grabar el video de
la Parte II. Antes de usarla, completar `frontend/config.js` con los datos reales del
tenant Azure AD B2C y la URL pública del backend.

**Para dejar todo funcional en EC2 y grabar el video, seguir `RUNBOOK.md`** (incluye
configuración de Oracle, S3, Azure AD B2C, despliegue en EC2 y guion sugerido del video).

## Cómo correr en local

```bash
export AWS_ACCESS_KEY_ID=...
export AWS_SECRET_ACCESS_KEY=...
export AWS_SESSION_TOKEN=...
export S3_BUCKET_NAME=...
export AZURE_ISSUER_URI=...
export AZURE_JWK_SET_URI=...
export ORACLE_DB_URL=...
export ORACLE_DB_USER=...
export ORACLE_DB_PASSWORD=...

docker-compose up --build
```

La API queda disponible en `http://localhost:8080`, y el panel de RabbitMQ en
`http://localhost:15672` (usuario/clave: `guest`/`guest`).

## Pendiente por completar por el equipo

1. Reemplazar `AZURE_ISSUER_URI` / `AZURE_JWK_SET_URI` con los valores reales del tenant
   Azure AD B2C que configuren (ver `RUNBOOK.md`).
2. Completar `frontend/config.js` con el `b2cClientId` del registro SPA, el `apiScope` y
   la IP pública de la EC2 donde corra el backend.
3. Registrar los endpoints en el API Manager que estén usando (Azure API Management / AWS
   API Gateway / Kong, según lo definido en clases) — documentar el paso a paso con capturas.
4. Ajustar el bucket S3 y las credenciales AWS reales al momento de desplegar.
5. Completar en el Word de configuración las capturas de pantalla propias de su ambiente
   (Azure, RabbitMQ, API Manager) — ver plantilla entregada.
6. Seguir `RUNBOOK.md` para dejar todo funcional en EC2 antes de grabar el video de la
   Parte II.
