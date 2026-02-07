# Docker Setup - ERP Lite Backend

Guía completa para ejecutar la aplicación usando Docker Compose.

## 🔧 Stack Tecnológico

- **Spring Boot**: 4.0.2
- **Java**: 21 (Eclipse Temurin)
- **MySQL**: 8.0
- **Maven**: 3.9.9
- **Docker Compose**: 2.0+

---

## 📋 Pre-requisitos

- Docker Desktop 24.0+ o Docker Engine 24.0+
- Docker Compose 2.0+
- 4GB RAM disponible (mínimo)
- Puertos 3306 y 8080 libres

---

## 🚀 Quick Start

### 1. Iniciar todos los servicios

```bash
# Navegar al directorio docker
cd docker

# Construir y levantar todos los contenedores
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f app
```

### 2. Verificar servicios

```bash
# Ver estado de contenedores
docker-compose ps

# Health check de la aplicación
curl http://localhost:8080/actuator/health

# Acceder a Swagger UI
# http://localhost:8080/swagger-ui.html
```

### 3. Detener servicios

```bash
# Detener contenedores (mantiene datos)
docker-compose stop

# Detener y eliminar contenedores
docker-compose down

# Eliminar contenedores y volúmenes (⚠️ BORRA LA BD)
docker-compose down -v
```

---

## 🏗️ Arquitectura Docker

```
┌─────────────────────────────────────────────────┐
│   erplite-backend (Spring Boot 4.0.2)           │
│   - Puerto: 8080                                 │
│   - Java: 21 (Eclipse Temurin JRE Alpine)      │
│   - Health: /actuator/health                    │
│   - Network: erplite-network                    │
└────────────────────┬────────────────────────────┘
                     │
                     │ JDBC: jdbc:mysql://mysql:3306
                     ▼
┌─────────────────────────────────────────────────┐
│   erplite-mysql (MySQL 8.0)                     │
│   - Puerto: 3306                                 │
│   - Database: cs_solutions_erp_lite             │
│   - Volumen: mysql_data (persistente)           │
│   - Network: erplite-network                    │
└─────────────────────────────────────────────────┘
```

---

## 📦 Servicios

### MySQL Database

- **Imagen**: `mysql:8.0`
- **Puerto**: `3306`
- **Database**: `cs_solutions_erp_lite`
- **Credenciales**:
  - Root: `root_password`
  - Usuario: `erplite`
  - Password: `erplite_pass`
- **Volumen persistente**: `mysql_data`

### Spring Boot Application

- **Build**: Multi-stage (Maven 3.9.9 + Eclipse Temurin 21 JRE Alpine)
- **Puerto**: `8080`
- **Java Version**: 21
- **Spring Boot**: 4.0.2
- **JVM Opts**: `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`
- **Usuario**: `spring` (non-root)
- **Health Check**: `/actuator/health` cada 30s (start-period: 60s)
- **Startup**: Espera a MySQL (depends_on con health check)
- **Network**: `erplite-network` (bridge)

---

## 🔧 Comandos Útiles

### Logs

```bash
# Logs de la aplicación
docker-compose logs -f app

# Logs de MySQL
docker-compose logs -f mysql

# Logs de ambos servicios
docker-compose logs -f

# Ver últimas 100 líneas
docker-compose logs --tail=100 app
```

### Base de Datos

```bash
# Conectar a MySQL desde línea de comandos
docker-compose exec mysql mysql -u erplite -perplite_pass cs_solutions_erp_lite

# Ejecutar query SQL
docker-compose exec mysql mysql -u erplite -perplite_pass cs_solutions_erp_lite -e "SELECT * FROM departments LIMIT 5;"

# Backup de base de datos
docker-compose exec mysql mysqldump -u erplite -perplite_pass cs_solutions_erp_lite > backup.sql

# Restaurar backup
docker-compose exec -T mysql mysql -u erplite -perplite_pass cs_solutions_erp_lite < backup.sql
```

### Aplicación

```bash
# Reiniciar solo la aplicación (después de cambios en código)
docker-compose up -d --build app

# Ejecutar Maven dentro del contenedor
docker-compose run --rm app mvn clean test

# Ver variables de entorno
docker-compose exec app env | grep SPRING

# Ver JAR ejecutado
docker-compose exec app ls -la /app/
```

### Desarrollo

```bash
# Reconstruir imagen después de cambios (sin caché)
docker-compose build --no-cache app

# Forzar recreación de contenedores
docker-compose up -d --force-recreate

# Ver uso de recursos en tiempo real
docker stats erplite-backend erplite-mysql

# Inspeccionar imagen construida
docker image inspect erp-lite-backend-app:latest

# Listar imágenes del proyecto
docker images | grep "erp-lite"
```

---

## 🧪 Testing con Docker

### Tests de Integración

```bash
# Ejecutar tests dentro del contenedor
docker-compose run --rm app mvn test

# Ejecutar tests con cobertura Jacoco
docker-compose run --rm app mvn clean verify jacoco:report

# Ver reportes generados
docker-compose run --rm app ls -la target/site/jacoco/

# Copiar reportes al host
docker cp erplite-backend:/app/target/site/jacoco ./jacoco-reports
```

### Tests Locales vs Docker

```bash
# Ejecutar tests locales (requiere MySQL local)
./mvnw test

# Ejecutar tests en Docker (entorno aislado)
docker-compose run --rm app mvn test

# Verificar diferencias en resultados
docker-compose run --rm app mvn test | grep "Tests run"
```

### Verificar Migraciones Flyway

```bash
# Ver estado de migraciones
docker-compose exec mysql mysql -u erplite -perplite_pass cs_solutions_erp_lite -e "SELECT version, description, installed_on, success FROM flyway_schema_history ORDER BY installed_rank;"

# Contar registros de tablas principales
docker-compose exec mysql mysql -u erplite -perplite_pass cs_solutions_erp_lite -e "
  SELECT 
    'departments' as tabla, COUNT(*) as registros FROM departments
  UNION ALL
  SELECT 'municipalities', COUNT(*) FROM municipalities
  UNION ALL  
  SELECT 'document_types', COUNT(*) FROM document_types;
"

# Ver última migración aplicada
docker-compose exec mysql mysql -u erplite -perplite_pass cs_solutions_erp_lite -e "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1;"
```

---

## 🛠️ Troubleshooting

### Problema: Puerto 3306 o 8080 en uso

```bash
# Linux/Mac: Encontrar proceso usando puerto
lsof -i :3306
lsof -i :8080

# Windows: Encontrar proceso usando puerto
netstat -ano | findstr :3306
netstat -ano | findstr :8080

# Detener proceso por PID (Linux/Mac)
kill -9 <PID>

# Alternativa: Cambiar puerto en docker-compose.yml
# mysql:
#   ports:
#     - "3307:3306"  # Usar puerto 3307 en el host
```

### Problema: Contenedor no inicia (health check failed)

```bash
# Ver logs detallados
docker-compose logs mysql
docker-compose logs app

# Verificar health check de MySQL manualmente
docker-compose exec mysql mysqladmin ping -h localhost -u root -proot_password

# Verificar health check de la app
docker-compose exec app wget --spider http://localhost:8080/actuator/health

# Reiniciar con logs visibles (modo interactivo)
docker-compose down
docker-compose up

# Ver estado específico de health checks
docker inspect erplite-backend --format='{{json .State.Health}}' | jq
docker inspect erplite-mysql --format='{{json .State.Health}}' | jq
```

### Problema: Flyway migration failed

```bash
# Ver estado de Flyway
docker-compose logs app | grep Flyway

# Conectar y verificar tabla flyway_schema_history
docker-compose exec mysql mysql -u erplite -perplite_pass cs_solutions_erp_lite \
  -e "SELECT * FROM flyway_schema_history;"

# Reparar estado de Flyway (cuidado en producción)
docker-compose exec mysql mysql -u erplite -perplite_pass cs_solutions_erp_lite \
  -e "DELETE FROM flyway_schema_history WHERE success = 0;"

# Limpiar completamente y reiniciar
docker-compose down -v
docker-compose up -d
```

### Problema: Cambios en código no se reflejan

```bash
# Reconstruir imagen sin caché
docker-compose build --no-cache app
docker-compose up -d app
```

### Problema: Base de datos corrupta

```bash
# Eliminar volumen y recrear
docker-compose down -v
docker-compose up -d
```

---

## 🔒 Seguridad (Producción)

### Variables de Entorno

Crear archivo `.env` en el directorio `docker/` (NO commitear a Git):

```env
# MySQL Configuration
MYSQL_ROOT_PASSWORD=your_secure_root_password_here
MYSQL_DATABASE=cs_solutions_erp_lite
MYSQL_USER=erplite_prod
MYSQL_PASSWORD=your_secure_app_password_here

# Spring Boot Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/cs_solutions_erp_lite?useSSL=true&requireSSL=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=${MYSQL_USER}
SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD}
SPRING_PROFILES_ACTIVE=prod

# JVM Options
JAVA_OPTS=-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC
```

Actualizar `docker-compose.yml` para producción:

```yaml
services:
  mysql:
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    # NO exponer puerto en producción
    # ports:
    #   - "3306:3306"
  
  app:
    environment:
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL}
      SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
```

### Agregar .env al .gitignore

```bash
# Verificar que .env esté ignorado
grep ".env" .gitignore

# Si no está, agregarlo
echo -e "\n# Docker environment variables\n.env\ndocker/.env" >> .gitignore
```

### Checklist de Seguridad para Producción

- [ ] Cambiar todas las credenciales por defecto
- [ ] Usar variables de entorno o Docker secrets
- [ ] NO exponer puerto MySQL (3306) fuera del contenedor
- [ ] Habilitar SSL/TLS para MySQL
- [ ] Configurar firewall para limitar acceso al puerto 8080
- [ ] Usar usuario non-root (ya configurado: `spring`)
- [ ] Actualizar imágenes base regularmente
- [ ] Implementar rate limiting en la API
- [ ] Configurar HTTPS con reverse proxy (nginx/traefik)
- [ ] Habilitar Spring Security en producción

---

## 🎯 Workflow de Desarrollo

### Ciclo de Desarrollo Típico

```bash
# 1. Iniciar servicios
cd docker
docker-compose up -d

# 2. Ver logs durante desarrollo
docker-compose logs -f app

# 3. Hacer cambios en el código (src/main/java)
# ... editar código ...

# 4. Reconstruir y reiniciar app
docker-compose up -d --build app

# 5. Verificar cambios
curl http://localhost:8080/actuator/health

# 6. Ejecutar tests
docker-compose run --rm app mvn test

# 7. Detener cuando termines
docker-compose stop
```

### Desarrollo Híbrido (IDE + Docker MySQL)

Si prefieres ejecutar Spring Boot desde tu IDE pero usar MySQL en Docker:

```bash
# 1. Iniciar solo MySQL
docker-compose up -d mysql

# 2. Ejecutar app desde IDE o terminal
cd ..
./mvnw spring-boot:run

# La app se conectará a MySQL en localhost:3306
# Configurado en src/main/resources/application.properties
```

### Hot Reload (Spring Boot DevTools)

Para habilitar hot reload (requiere `spring-boot-devtools` en pom.xml):

```powershell
# En docker-compose.yml, agregar volumen para sincronizar código
# app:
#   volumes:
#     - ../src:/app/src
#     - ../target:/app/target

# Reconstruir con DevTools activo
docker-compose up -d --build app
```

### Debugging Remoto

Para depurar la aplicación en el contenedor desde tu IDE:

```yaml
# Agregar en docker-compose.yml -> app -> environment:
JAVA_TOOL_OPTIONS: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# Y exponer puerto de debug:
ports:
  - "8080:8080"
  - "5005:5005"  # Debug port
```

Luego configurar Remote Debug en tu IDE apuntando a `localhost:5005`.

---

## 📦 Comandos de Mantenimiento

### Limpieza de Docker

```bash
# Ver espacio usado por Docker
docker system df

# Limpiar contenedores detenidos
docker container prune -f

# Limpiar imágenes sin usar
docker image prune -a -f

# Limpiar volúmenes sin usar (⚠️ CUIDADO)
docker volume prune -f

# Limpieza completa del sistema (⚠️ ESTO BORRA TODO)
docker system prune -a --volumes -f

# Limpiar solo imágenes de este proyecto
docker images | grep "erp-lite" | awk '{print $3}' | xargs docker rmi -f
```

### Backup y Restore

```bash
# Backup de base de datos
docker-compose exec mysql mysqldump -u erplite -perplite_pass cs_solutions_erp_lite > backup-$(date +%Y%m%d-%H%M%S).sql

# Backup del volumen completo
docker run --rm -v erp-lite-backend_mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-data-backup.tar.gz -C /data .

# Restore desde SQL dump
cat backup-20260207-150000.sql | docker-compose exec -T mysql mysql -u erplite -perplite_pass cs_solutions_erp_lite

# Restore volumen completo
docker run --rm -v erp-lite-backend_mysql_data:/data -v $(pwd):/backup alpine tar xzf /backup/mysql-data-backup.tar.gz -C /data
```

---

## 🔍 Diagnóstico y Monitoreo

### Inspeccionar Contenedores

```bash
# Información completa del contenedor
docker inspect erplite-backend | jq

# Ver variables de entorno
docker inspect erplite-backend --format='{{range .Config.Env}}{{println .}}{{end}}'

# Ver puertos mapeados
docker inspect erplite-backend --format='{{range $p, $conf := .NetworkSettings.Ports}}{{$p}} -> {{(index $conf 0).HostPort}}{{println}}{{end}}'

# Ver uso de recursos en tiempo real
docker stats --no-stream erplite-backend erplite-mysql

# Ver procesos dentro del contenedor
docker top erplite-backend
docker top erplite-mysql
```

### Logs Avanzados

```bash
# Logs desde timestamp específico
docker-compose logs --since 2026-02-07T10:00:00 app

# Logs hasta timestamp
docker-compose logs --until 2026-02-07T12:00:00 app

# Buscar errores en logs
docker-compose logs app | grep -E "ERROR|Exception|Failed"

# Exportar logs a archivo
docker-compose logs app > app-logs-$(date +%Y%m%d-%H%M%S).log
```

---

## 📊 Monitoreo

### Prometheus + Grafana (Opcional)

```bash
# Agregar a docker-compose.yml
# prometheus:
#   image: prom/prometheus
#   ...
# grafana:
#   image: grafana/grafana
#   ...
```

### Ver métricas actuales

```bash
# Actuator metrics (JSON)
curl http://localhost:8080/actuator/metrics | jq

# JVM memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used | jq

# HTTP requests count
curl http://localhost:8080/actuator/metrics/http.server.requests | jq

# Health check detallado
curl http://localhost:8080/actuator/health | jq

# Info de la aplicación
curl http://localhost:8080/actuator/info | jq
```

---

## 🚀 Performance Tuning

### JVM Tuning

Configuración actual en `Dockerfile`:

```dockerfile
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

Opciones adicionales recomendadas para producción:

```dockerfile
# Para mejor GC performance
"-XX:+UseG1GC", \
"-XX:InitialRAMPercentage=50.0", \
"-XX:+UseStringDeduplication", \

# Para debugging
"-XX:+HeapDumpOnOutOfMemoryError", \
"-XX:HeapDumpPath=/app/logs/heap-dump.hprof"
```

### MySQL Tuning

Agregar archivo `my.cnf`:

```ini
[mysqld]
max_connections = 200
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
```

Montar en docker-compose:

```yaml
mysql:
  volumes:
    - ./my.cnf:/etc/mysql/conf.d/my.cnf
```

---

## 📝 Notas Importantes

- **Spring Boot 4.0.2**: Requiere Java 21 mínimo
- **Flyway**: Migraciones se ejecutan automáticamente al iniciar (baseline-on-migrate)
- **JPA**: Modo `validate` (no modifica esquema, requiere Flyway)
- **Persistencia**: MySQL data en volumen `mysql_data` (sobrevive a `docker-compose down`)
- **Health Checks**: App (30s) y MySQL (10s), con tiempos de inicio (60s y 30s)
- **Network**: Red privada `erplite-network` (bridge) para comunicación interna
- **Seguridad**: Usuario non-root `spring` en el contenedor de la app
- **Primera ejecución**: Puede tardar 3-5 minutos (descarga imágenes + build + migraciones)
- **Contenedor app**: Se reconstruye solo si hay cambios en `pom.xml` o `src/`

### ⚠️ Advertencias

- `docker-compose down -v` elimina TODOS los datos de la base de datos
- Las credenciales actuales son para desarrollo, NO usar en producción
- Flyway en modo `baseline-on-migrate=true` puede causar conflictos si se ejecuta sobre BD existente
- El health check de la app espera 60s antes del primer check (start_period)

---

## 🔗 URLs Importantes

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **API Base** | http://localhost:8080/api/v1 | Endpoints REST |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentación interactiva |
| **Health Check** | http://localhost:8080/actuator/health | Estado del servicio |
| **Metrics** | http://localhost:8080/actuator/metrics | Métricas de Spring Boot |
| **Info** | http://localhost:8080/actuator/info | Información de la aplicación |
| **MySQL** | localhost:3306 | Acceso directo a MySQL |

### Ejemplos de Endpoints

```bash
# Listar departamentos
curl http://localhost:8080/api/v1/departments | jq

# Crear documento tipo
curl -X POST http://localhost:8080/api/v1/document-types \
  -H "Content-Type: application/json" \
  -d '{"code":"CC","name":"Cédula de Ciudadanía"}' | jq

# Health check con formato
curl http://localhost:8080/actuator/health | jq
```

---

## 📚 Referencia y Recursos

### Documentación Oficial

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot 4.0.x Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [MySQL 8.0 Docker Hub](https://hub.docker.com/_/mysql)
- [Eclipse Temurin Docker Images](https://hub.docker.com/_/eclipse-temurin)
- [Flyway Migration Guide](https://flywaydb.org/documentation/)

### Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Spring Boot | 4.0.2 | Framework de aplicación |
| Java | 21 | Lenguaje de programación |
| MySQL | 8.0 | Base de datos |
| Maven | 3.9.9 | Build tool |
| Flyway | Incluido | Database migration |
| Docker | 24.0+ | Containerización |
| Docker Compose | 2.0+ | Orquestación |

### Configuración del Proyecto

```
docker/
├── docker-compose.yml    # Orquestación de servicios
├── Dockerfile           # Build de la aplicación Spring Boot
└── DOCKER.md           # Esta documentación

src/main/resources/
├── application.properties    # Configuración local
└── db/migration/            # Scripts Flyway SQL

target/
└── erp-lite-backend-*.jar   # JAR generado
```

### Problema: Permisos en volúmenes

```bash
# Si hay errores de permisos con volúmenes
# Limpiar volúmenes con permisos corruptos
docker-compose down -v
docker volume rm erp-lite-backend_mysql_data
docker-compose up -d

# Verificar permisos dentro del contenedor
docker-compose exec app ls -la /app
docker-compose exec mysql ls -la /var/lib/mysql
```

### Problema: Performance/Recursos

```bash
# Verificar recursos del sistema
free -h
df -h

# Ver consumo de Docker
docker stats --no-stream

# Limitar recursos de contenedores (docker-compose.yml)
# services:
#   app:
#     deploy:
#       resources:
#         limits:
#           cpus: '2'
#           memory: 2G
```

---

## 🆘 Soporte y Contribución

### Reportar Problemas

Si encuentras algún problema:

1. Verificar logs: `docker-compose logs -f`
2. Revisar health checks: `docker-compose ps`
3. Consultar esta documentación
4. Revisar issues en el repositorio del proyecto

### Comandos de Ayuda Rápida

```bash
# Ver versiones instaladas
docker --version
docker-compose --version

# Verificar instalación de Docker
docker run hello-world

# Ver ayuda de docker-compose
docker-compose --help
docker-compose up --help
```

---

## 📝 Changelog de Documentación

### 2026-02-07
- ✅ Actualizada a Spring Boot 4.0.2 y Java 21
- ✅ Comandos adaptados para PowerShell/Windows
- ✅ Agregadas secciones de workflow de desarrollo
- ✅ Incluido troubleshooting específico de Windows
- ✅ Expandida sección de seguridad
- ✅ Agregados comandos de backup/restore
- ✅ Mejorada documentación de health checks
- ✅ Agregada tabla de tecnologías y versiones

---

**🎉 Documentación actualizada - Febrero 2026**
