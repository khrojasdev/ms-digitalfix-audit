# ms-digitalfix-audit

**Proyecto:** DigitalFix - Gestion de ordenes de trabajo para una red de 20 empresas de mantenimiento electrico  
**Componente:** Microservicio  
**Asignatura:** DSY1107 Desarrollo Cloud Native I - Duoc UC  
**Primera entrega (EP1):** 14 de septiembre de 2026

## Descripcion

Auditoria: bitacora inmutable de eventos de negocio para trazabilidad y cumplimiento.

## Tecnologias

Spring Boot 3, RabbitMQ (Spring AMQP), Spring Data JPA, Oracle, Flyway

## Mensajeria (RabbitMQ)

Dos rutas, cada una con su cola y su DLQ. Los nombres viven en `application.yml`
(`amqp.*`) y los beans en `config/RabbitMQConfig`.

| Ruta | Exchange (topic) | Routing key | Cola | DLQ | Consumidor |
|---|---|---|---|---|---|
| Ordenes | `ordenes.negocio.exchange` | `ordenes.eventos.*` | `ordenes.auditoria.queue` | `ordenes.auditoria.dlq` | `mensajeria.ordenes.AuditEventoConsumer` |
| Usuarios | `usuarios.negocio.exchange` | `usuarios.eventos.#` | `usuarios.auditoria.queue` | `usuarios.auditoria.dlq` | `mensajeria.usuarios.UsuarioEventoConsumer` |

### Confirmacion y errores (`mensajeria.ProcesadorDeMensajes`)

| Situacion | Decision |
|---|---|
| Se guardo el evento | `ACK` |
| Mensaje invalido (JSON ilegible o le falta orden, autor o empresa) | `NACK` sin reencolar: a la DLQ de inmediato |
| Falla transitoria (base de datos o red caida un momento) | Reintento hasta `amqp.reintentos.maximo` veces (1 s, 2 s, 4 s...); si persiste, `NACK` a la DLQ |
| Cualquier otro error | `NACK` sin reencolar: a la DLQ |

Nunca se reencola sin control, y todo lo que va a la DLQ queda en el log con la
cola y el motivo. Un evento repetido (misma `eventKey`) se ignora: la bitacora
es idempotente.

## API

`GET /api/audit/events` (solo ADMIN y AUDITOR, siempre de la empresa de quien consulta)

| Parametro | Uso |
|---|---|
| `ordenId` | Timeline de una orden |
| `autorId` | Eventos de una persona (oid) |
| `tipoEvento` | `CREADA`, `ASIGNADA`, ..., `USUARIO_CREADO`, `USUARIO_ROL_CAMBIADO`... |
| `desde`, `hasta` | Rango de fechas (ISO) |
| `page`, `size`, `sort` | Paginacion, p.ej. `sort=fecha,desc` |

Cada evento trae `detalle` con el texto legible: tecnico asignado, comentario del
cambio de estado o correo y rol del usuario.

`GET /api/audit/lag` (solo ADMIN): mensajes pendientes en la cola de auditoria.

## Pruebas

`./mvnw test` (H2 en memoria, sin Oracle ni RabbitMQ): politica de ACK/reintento/DLQ,
consumidores de ordenes y usuarios, timeline por orden, idempotencia y acceso por rol.

## Integrantes

| Integrante | GitHub |
|---|---|
| Kevin Rojas | @khrojasdev |
| Christopher Perez | @ChrisPerezV |
| Diego Lopez | @DiegoLopez-f |

## Repositorios del proyecto

- [`digitalfix-frontend`](https://github.com/khrojasdev/digitalfix-frontend) - Frontend
- [`ms-digitalfix-bff`](https://github.com/khrojasdev/ms-digitalfix-bff) - BFF / API interna
- [`ms-digitalfix-usuarios`](https://github.com/khrojasdev/ms-digitalfix-usuarios) - Microservicio
- [`ms-digitalfix-catalog`](https://github.com/khrojasdev/ms-digitalfix-catalog) - Microservicio
- [`ms-digitalfix-workorders`](https://github.com/khrojasdev/ms-digitalfix-workorders) - Microservicio
- [`ms-digitalfix-notify`](https://github.com/khrojasdev/ms-digitalfix-notify) - Microservicio
- [`ms-digitalfix-report`](https://github.com/khrojasdev/ms-digitalfix-report) - Microservicio
- [`ms-digitalfix-audit`](https://github.com/khrojasdev/ms-digitalfix-audit) - Microservicio **(este)**
- [`digitalfix-infra`](https://github.com/khrojasdev/digitalfix-infra) - Infraestructura

## Tablero

El backlog completo vive en un unico GitHub Project que enlaza los ocho repositorios.
Columnas: Backlog, To Do, In Progress, In Review, Done. Limite de trabajo en curso: 2 tarjetas por persona.

## Como se trabaja aqui

1. Cada tarea del tablero tiene su propia rama, indicada en el cuerpo del issue.
2. `git switch <rama>` - nunca se trabaja directamente sobre `main`.
3. Commits con Conventional Commits: `feat(catalogo): agrega endpoint de servicios`.
4. Pull request hacia `main` con `Closes #<numero del issue>` en la descripcion.
5. Revisa un companero distinto del autor. Recien ahi se hace merge.

Referencias entre repositorios: `khrojasdev/otro-repo#12`.

## Configuracion

Ninguna credencial vive en este repositorio. Todo llega por variables de entorno;
revisa `.env.example` para saber cuales.
