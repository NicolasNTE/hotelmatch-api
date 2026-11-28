# language: es
Característica: Autenticación y control de acceso (EP01)

  Escenario: US01-E1 Registro con un correo no registrado
    Cuando me registro con un correo nuevo
    Entonces la respuesta es 201
    Y la contraseña queda guardada cifrada

  Escenario: US01-E2 Registro con un correo ya registrado
    Dado que ya existe una cuenta
    Cuando me registro otra vez con el mismo correo
    Entonces la respuesta es 409

  Escenario: US02-E1 Inicio de sesión con credenciales correctas
    Dado que ya existe una cuenta
    Cuando inicio sesión con la contraseña correcta
    Entonces la respuesta es 200
    Y recibo un token con el rol "GUEST"

  Escenario: US02-E2 Inicio de sesión con credenciales incorrectas
    Dado que ya existe una cuenta
    Cuando inicio sesión con una contraseña incorrecta
    Entonces la respuesta es 401
    Y no recibo ningún token

  Escenario: US03-E1 El administrador accede al panel
    Dado que soy un administrador con mi propio hotel
    Cuando consulto el reporte del hotel
    Entonces la respuesta es 200

  Escenario: US03-E2 El huésped no accede al panel
    Dado que soy un huésped
    Cuando consulto el reporte del hotel
    Entonces la respuesta es 403
