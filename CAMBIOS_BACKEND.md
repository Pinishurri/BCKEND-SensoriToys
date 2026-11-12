# SensoriToys – cambios de seguridad y productos

## Arranque
1. MySQL activo. Reiniciar la base `usuarios_db` (los usuarios antiguos tienen la contraseña sin hash).
2. ms_usuarios (8081): crea los roles ADMIN y CLIENTE y el usuario administrador.
3. ms_productos (8082): agrega columnas `edad`, `imagen_url`, `activo`; si no hay datos, carga las categorías y los 4 productos iniciales.
4. ms_pedidos (8083) y gateway (8089).

Administrador inicial: `admin@sensoritoys.cl` / `Admin2026!`.
Variables de entorno opcionales: `ADMIN_PASSWORD`, `JWT_SECRET` (debe ser el mismo en gateway y ms_usuarios).

## Login
`POST /api/v1/usuarios/login` → `{ token, id, nombre, email, rol }`. El token se envía como `Authorization: Bearer <token>`.

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
