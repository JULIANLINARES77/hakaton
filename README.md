# CampusReporta

Sistema web de reportes de daños en campus UNIMINUTO. Incluye una interfaz web,
una API REST en capas con Spring Boot, PostgreSQL y el asistente Laura, que consulta
los datos de CampusReporta usando el modelo local **Ollama `llama3.2:3b`**.

> Este repositorio no tiene módulos de productos, ventas, clientes o facturas.
> El asistente responde sobre reportes, ubicaciones, estados e indicadores de daños.

## Tecnologías y versiones

| Componente | Versión/configuración |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.1.1 |
| Maven | Wrapper del proyecto, Maven 3.9.x |
| PostgreSQL | 16 en Docker Compose; por defecto escucha en `localhost:5433` desde el host |
| Ollama | Servicio HTTP local en `http://localhost:11434` |
| Modelo de IA | `llama3.2:3b` |
| Identificador de modelo reportado en la máquina de desarrollo | `a80c4f17acd5` (ID local; usar el tag `llama3.2:3b` en comandos/API) |
| API Ollama | `POST /api/chat`, respuesta no streaming |
| MapStruct | 1.6.3 |
| Springdoc OpenAPI | 3.0.1 |
| iText | 7.2.5 |
| Interfaz | HTML, CSS y JavaScript; Chart.js, Font Awesome y Google Fonts se cargan desde CDN |

No se necesita clave Gemini, cuenta de proveedor ni variable `GEMINI_API_KEY`.
Ollama se ejecuta localmente y el navegador nunca recibe secretos del modelo.

## Requisitos previos

- Java 21.
- Docker Desktop con Docker Compose, **o** PostgreSQL instalado localmente.
- Ollama instalado en el equipo donde se ejecuta Spring Boot, o Docker Compose para
  ejecutar Ollama junto con la aplicación.
- Espacio libre para el modelo `llama3.2:3b` (aproximadamente 2 GB, puede variar).
- Una contraseña local para PostgreSQL, suministrada mediante variables de entorno.

## Ejecución local en Windows

### 1. Descargar y comprobar el modelo Ollama

Abre PowerShell:

```powershell
ollama --version
ollama pull llama3.2:3b
ollama list
```

El modelo debe aparecer en `ollama list` con el nombre `llama3.2:3b`. El identificador
`a80c4f17acd5` corresponde al ID local mostrado en la máquina; no lo sustituyas por
ese ID en `OLLAMA_MODEL`, porque la API usa el tag `llama3.2:3b`.

Inicia Ollama si no se encuentra ejecutándose. Su API local debe responder en
`http://localhost:11434`.

### 2. Iniciar PostgreSQL

Si no tienes una instancia local compatible, desde la carpeta del proyecto:

```powershell
Copy-Item .env.example .env
# Edita .env y reemplaza el valor de ejemplo de POSTGRES_PASSWORD.
docker compose up -d postgres
```

El puerto publicado es `5433` en Windows, dirigido al puerto `5432` del contenedor.
La base configurada es `hakaton`. El usuario y contraseña se pueden sobrescribir
con `POSTGRES_USER` y `POSTGRES_PASSWORD` del archivo local `.env`. Ese archivo está
ignorado por Git y no debe subirse. Para una base PostgreSQL instalada fuera de Docker,
define `SPRING_DATASOURCE_PASSWORD` con la contraseña de esa instancia antes de iniciar
Spring Boot.

### 3. Iniciar CampusReporta

Desde la carpeta que contiene `mvnw.cmd`:

```powershell
$env:SPRING_DATASOURCE_PASSWORD = "la-misma-clave-local-de-postgres"
.\mvnw.cmd spring-boot:run
```

Por defecto, Spring usa:

- PostgreSQL: `jdbc:postgresql://localhost:5433/hakaton`
- Ollama: `http://localhost:11434`
- Modelo: `llama3.2:3b`
- Web: `http://localhost:8080`

La aplicación requiere que PostgreSQL esté disponible al iniciar. `ddl-auto=update`
crea/actualiza las tablas, pero no inserta datos de ejemplo. Para cargar la semilla,
ejecuta [database/campusreporta.sql](database/campusreporta.sql) en la base `hakaton`.

### 4. Abrir y probar

- Interfaz: <http://localhost:8080/>
- Widget flotante: botón **Habla con Laura** con la cara del robot.
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Esquema OpenAPI: <http://localhost:8080/api-docs>
- API de chat: `POST http://localhost:8080/api/ia/chat`

Ejemplo PowerShell:

```powershell
$body = @{ pregunta = "¿Cuáles son los reportes pendientes y en qué ubicaciones están?" } | ConvertTo-Json
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/ia/chat" `
  -ContentType "application/json" `
  -Body $body
```

La respuesta incluye `respuesta`, `timestamp` y `documentosUsados`. Si Ollama no está
iniciado, la API devuelve un error `503` con instrucciones para revisar el servicio.
Si el modelo no está instalado, la respuesta indica `ollama pull llama3.2:3b`.
Si Ollama informa `unable to allocate CPU buffer`, el modelo está descargado pero el
equipo no tiene memoria disponible suficiente para cargarlo en ese momento; cierra
aplicaciones que consuman RAM y vuelve a enviar la consulta. Verifica el uso actual
con `ollama ps`.

## Ejecución con Docker Compose

La configuración incluye contenedores de PostgreSQL, Ollama y CampusReporta:

```powershell
docker compose up -d postgres ollama
docker compose exec ollama ollama pull llama3.2:3b
docker compose up -d --build app
```

Después, abre <http://localhost:8080/>. Dentro de la red Compose, Spring accede a
Ollama mediante `http://ollama:11434` y a PostgreSQL mediante
`jdbc:postgresql://postgres:5432/hakaton`. Los pesos del modelo y los datos de la BD
persisten en los volúmenes `campusreporta-ollama-data` y
`campusreporta-postgres-data`.

Para consultar los logs:

```powershell
docker compose logs -f app ollama postgres
```

Para detener los servicios sin borrar los volúmenes:

```powershell
docker compose down
```

## Configuración

La configuración central está en
[application.properties](src/main/resources/application.properties):

```properties
spring.application.name=campusreporta
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5433/hakaton}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
server.port=8080
app.ai.ollama.base-url=${OLLAMA_BASE_URL:http://localhost:11434}
app.ai.ollama.model=${OLLAMA_MODEL:llama3.2:3b}
```

Variables opcionales:

| Variable | Predeterminado | Uso |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/hakaton` | URL de PostgreSQL desde el proceso Spring |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Usuario DB |
| `SPRING_DATASOURCE_PASSWORD` | Sin valor; obligatoria | Contraseña PostgreSQL para ejecución local |
| `POSTGRES_PASSWORD` | Sin valor; obligatoria | Contraseña PostgreSQL para Docker Compose, definida en `.env` |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | URL del servidor Ollama |
| `OLLAMA_MODEL` | `llama3.2:3b` | Tag del modelo Ollama instalado |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:8080,http://127.0.0.1:8080` | Orígenes permitidos para `/api/**` |

En Windows, puedes sobrescribir Ollama solo para la sesión actual de PowerShell:

```powershell
$env:OLLAMA_BASE_URL = "http://localhost:11434"
$env:OLLAMA_MODEL = "llama3.2:3b"
$env:SPRING_DATASOURCE_PASSWORD = "la-misma-clave-local-de-postgres"
.\mvnw.cmd spring-boot:run
```

No guardes contraseñas reales en `application.properties`, `docker-compose.yml` ni
en Git. `.env.example` es solo una plantilla: configura una clave local en `.env`,
archivo que Git ignora.

## Flujo del asistente Laura

1. El navegador envía una pregunta a `POST /api/ia/chat`.
2. `DataIngestionService` consulta PostgreSQL: conteos por estado y hasta 100 reportes
   recientes; prioriza hasta seis reportes cuyas descripciones, estado o ubicación
   coinciden con términos de la pregunta.
3. `PromptBuilder` construye el mensaje con ese contexto y `SystemPrompt` limita al
   asistente a información del campus, evitando inventar datos.
4. `OllamaRestClient` llama `POST /api/chat` con `stream: false` y el modelo
   `llama3.2:3b`.
5. El widget muestra la respuesta, conserva hasta 40 mensajes en `localStorage` y
   puede dictar/reproducir voz con capacidades del navegador.

El micrófono **no** se escucha continuamente en el servidor. El botón de micrófono
activa el reconocimiento de voz del navegador al solicitarlo el usuario; para usarlo,
el navegador debe soportar Web Speech API y conceder permiso. La lectura usa síntesis
de voz del navegador. La frase “Laura” es un saludo/uso conversacional, no una palabra
de activación permanente.

## API disponible

Base de la API de dominio: `/api/v1/campus`.

| Método | Ruta | Propósito |
| --- | --- | --- |
| `POST` | `/api/ia/chat` | Preguntar al asistente local Ollama |
| `POST` | `/api/v1/campus/reportes` | Crear un reporte |
| `GET` | `/api/v1/campus/reportes` | Listar/filtrar reportes con paginación |
| `GET` | `/api/v1/campus/reportes/{id}` | Consultar un reporte |
| `GET` | `/api/v1/campus/reportes/{id}/historial` | Consultar cambios de estado |
| `PUT` | `/api/v1/campus/reportes/{id}/estado` | Cambiar estado |
| `GET` | `/api/v1/campus/indicadores` | Conteos y reportes recientes |
| `GET` | `/api/v1/campus/reportes/pdf` | Descargar PDF filtrado |
| `GET`, `POST` | `/api/v1/campus/usuarios` | Consultar/registrar usuarios |
| `GET`, `POST` | `/api/v1/campus/ubicaciones` | Consultar/registrar ubicaciones |

## Arquitectura y carpetas

Raíz Java: `src/main/java/uniminuto/campusreporta/`.

```text
config/       configuración web, OpenAPI y cliente Ollama
controller/   API REST de CampusReporta y chat
dto/          contratos de request/response
entity/       entidades y estados JPA
exception/    errores de negocio y respuestas JSON
mapper/       mappers MapStruct
prompt/       instrucciones del sistema y construcción del prompt
repository/   acceso Spring Data JPA
service/      casos de uso, consultas/contexto, chat, PDF y Ollama
```

La cadena de chat reutiliza `ChatService`, `DataIngestionService`, `PromptBuilder`,
`SystemPrompt`, `OllamaClient` y `OllamaRestClient`. No requiere dependencia Java
específica de Ollama: consume la API HTTP nativa usando `RestClient` de Spring.

## Pruebas

Ejecuta la suite completa, incluidas las pruebas HTTP simuladas del cliente Ollama:

```powershell
.\mvnw.cmd clean test
```

Las pruebas del servicio Ollama no llaman a la red ni necesitan el modelo descargado.
Las pruebas de integración JPA usan PostgreSQL configurado por el proyecto.

## Límites actuales

- No se configura PGVector ni una base vectorial; se priorizan reportes recientes con
  búsqueda textual para proporcionar contexto.
- No hay streaming WebSocket para chat ni alertas programadas.
- Los datos de la pantalla principal continúan siendo demostrativos en `app.js`; el
  backend y Laura consultan PostgreSQL.
- El selector de rol del frontend es una interfaz de demostración, no autenticación.
- El sistema no autentica usuarios. No uses el endpoint para exponerlo públicamente
  hasta añadir autenticación y autorización reales.
