# Servicio de autenticacion JWT

Servicio Spring Boot que autentica dos usuarios almacenados en una base H2 en memoria y entrega un JWT con el rol del usuario.

## Arquitectura

El codigo sigue el patron Controller-Service-Repository (CSR) y separa los objetos auxiliares por responsabilidad:

```text
cl.alejandrossm.authsvc
|-- controller/   Entrada HTTP y respuesta del endpoint
|-- service/      Autenticacion, reglas de negocio y generacion del JWT
|-- repository/   Acceso a los usuarios almacenados en H2
|-- model/        Entidad JPA y roles
|-- dto/          Contratos de entrada y salida de la API
|-- exception/    Excepciones y respuestas de error centralizadas
`-- config/       Seguridad, JWT y carga inicial de datos
```

El flujo del login es `AuthController -> AuthService -> AuthenticationManager -> DatabaseUserDetailsService -> AppUserRepository`.

## Ejecucion

Requisito: Java 25.

```powershell
.\mvnw.cmd spring-boot:run
```

El servicio queda disponible en `http://localhost:8090`.

## Endpoint de login

`POST /auth/login`

Administrador:

```json
{"username": "admin", "password": "admin"}
```

Cliente:

```json
{"username": "cliente", "password": "1234"}
```

Ejemplo de respuesta:

```json
{
  "token": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "username": "admin",
  "role": "ADMIN"
}
```

El JWT incluye el usuario en `sub`, el rol en el claim `role`, el emisor y las fechas de emision y expiracion. Una autenticacion incorrecta responde `401 Unauthorized`; una solicitud incompleta o mal formada responde `400 Bad Request`.

## Pasos realizados

1. Se agregaron Spring Security, Spring Data JPA, H2 y el soporte JOSE/JWT de Spring Security.
2. Se configuro el servidor para utilizar el puerto `8090`.
3. Se configuro una base H2 en memoria, recreada en cada inicio del servicio.
4. Se creo la entidad de usuario, su repositorio JPA y los roles `ADMIN` y `CLIENTE`.
5. Se agrego una carga inicial que guarda `admin/admin` con rol `ADMIN` y `cliente/1234` con rol `CLIENTE`. Las contrasenas se almacenan cifradas con BCrypt.
6. Se configuro Spring Security como servicio sin estado, sin formulario, sesion ni autenticacion HTTP Basic. Solo se permite publicamente `/auth/login` y cualquier otra ruta queda denegada.
7. Se implemento la validacion de credenciales contra H2 y la generacion de JWT firmado con HMAC-SHA256.
8. Se incorporo el rol tanto en el JWT como en la respuesta del login para diferenciar al administrador del cliente usando el mismo endpoint.
9. Se agrego manejo uniforme de errores y pruebas automatizadas para ambos roles y para credenciales incorrectas.
10. Se reorganizo el codigo con el patron CSR, dejando DTO, modelos, configuracion y excepciones en paquetes dedicados.
11. Se agrego validacion declarativa de `username` y `password`, y la carga inicial se hizo idempotente.

## Configuracion JWT

Por defecto los tokens duran una hora. En un ambiente real se deben definir estas variables:

```powershell
$env:JWT_SECRET="una-clave-segura-de-al-menos-32-caracteres"
$env:JWT_EXPIRATION_SECONDS="3600"
.\mvnw.cmd spring-boot:run
```

No se debe publicar ni guardar en Git el secreto usado en produccion.

## Pruebas

```powershell
.\mvnw.cmd test
```

## Integracion con SensoriToys

El gateway expone el login demo en `POST http://localhost:8089/api/v1/auth/login` y lo redirige a este servicio en el puerto 8090. Además, redirige el login de la tienda `POST http://localhost:8089/api/v1/usuarios/login` al mismo Authenticator. El gateway acepta los claims `role` y `uid` emitidos por este servicio. Configura el mismo `JWT_SECRET` en el gateway, `ms_usuarios` y `ms-authsvc`; por defecto los tres usan la misma clave local de desarrollo.

El login de la tienda acepta el formato existente `{ email, contrasenia }`; el Authenticator delega la comprobación a `ms_usuarios`/MySQL y emite el JWT central. Para demostrar el servicio H2, `POST /api/v1/auth/login` acepta `{ username, password }` con `admin/admin` o `cliente/1234`. Esos dos usuarios demo se recrean al reiniciar.