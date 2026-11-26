# SensoriToys – cambios de seguridad y productos

## Arranque

Requisitos: MySQL activo en XAMPP y JDK 25 disponible (un JDK 25 también puede ejecutar los módulos configurados para `release 17`). Mantén cada proceso en una terminal separada:
1. `ms_usuarios`: `.\mvnw.cmd spring-boot:run` (8081).
2. `ms_productos`: `.\mvnw.cmd spring-boot:run` (8082).
3. `ms_pedidos`: `.\mvnw.cmd spring-boot:run` (8083).
4. `ms-authsvc`: `.\mvnw.cmd spring-boot:run` (8090; login H2 de demostración).
5. `gateway`: `.\mvnw.cmd spring-boot:run` (8089), después de los servicios anteriores.

Las bases MySQL del comercio usan `ddl-auto=update`; no es necesario borrarlas. `ms-authsvc` usa su propia H2 en memoria y vuelve a crear sus dos usuarios demo en cada inicio. El gateway publica su login en `POST /api/v1/auth/login`.
Administrador inicial: `admin@sensoritoys.cl` / `Admin2026!`.
Variables de entorno opcionales: `ADMIN_PASSWORD`, `JWT_SECRET` (debe ser el mismo en gateway, ms_usuarios y ms-authsvc).

## Login
`POST /api/v1/usuarios/login` (correo/contraseña del comercio) pasa por el Authenticator y devuelve `{ token, id, nombre, email, rol }`. Para los usuarios demo H2 usa `POST /api/v1/auth/login` con `{ username, password }`. Ambos JWT se envían como `Authorization: Bearer <token>`.

## Permisos aplicados en el gateway
| Acceso | Endpoints |
|---|---|
| Público | `POST usuarios/login`, `POST usuarios/registro`, `GET productos`, `GET productos/{id}`, `GET categorias` |
| Autenticado | `POST usuarios/logout`, `POST pedidos` |
| Propio usuario o ADMIN | `GET/PUT/DELETE usuarios/{id}` |
| Solo ADMIN | `GET usuarios`, `PUT usuarios/{id}/rol/{idRol}`, `GET productos/todos`, `GET productos/dashboard/stock`, `POST/PUT/DELETE productos`, `PUT productos/{id}/stock`, `POST/PUT/DELETE categorias`, `GET/DELETE pedidos` y `GET pedidos/dashboard/ventas` |

Cualquier ruta sin regla exige ADMIN. Sin token: 401. Con token sin permiso: 403.

## Productos
- `DELETE /productos/{id}` desactiva el producto (`activo=false`); no lo borra.
- `PUT /productos/{id}` modifica el producto; con `activo: true` se reactiva.
- `GET /productos` y `GET /productos/{id}` solo muestran productos activos; `GET /productos/todos` muestra todos.

## CORS
Se maneja como indicó el profe: una clase `CorsConfig` en `config` de cada ms_* (usuarios, productos y pedidos). El gateway no configura CORS; solo agrega la cabecera a sus propias respuestas 401/403 para que el navegador pueda leerlas (`sensoritoys.cors.origen-permitido`).
