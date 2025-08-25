**Introducción**
Este proyecto nació con la intención de demostrar como se vería un ataque de ingenería social en terminos educativos, en el cual una empresa suplanta a otra y manda correos a personas específicas y hacerles
creer que ganarían dinero si utilizan la aplicación adjuntada en el correo, una persona de escasos recursos o financieramente quebrada sin conococimientos de seguridad
digital, podría caer.
El propósito de esta auditoría es explicar el funcionamiento del keylogger integrado en la aplicación Android "CalcLab", su estructura de código, y la comunicación entre sus componentes.

Este keylogger tiene dos mecanismos principales para capturar datos ingresados por el usuario:
1. Acceso directo al hardware del teclado físico mediante /dev/input/event2 en dispositivos Android rooteados.
2. Uso de un Servicio de Accesibilidad para capturar texto digitado en cualquier aplicación sin requerir permisos root.
**LIMITANTE:** SOLO funciona de Android 12 para abajo, ya que desde Android 13, no se permite dar permisos especiales a APKs descargadas de fuentes desconocidas

**Cronología del funcionamiento y comunicación entre archivos**
A continuación, se describe paso a paso cómo los archivos interactúan entre sí para lograr la captura y el envío de las pulsaciones del teclado.

**1. Compilación de la librería nativa (keylogger.c)**
**Archivo involucrado:** CMakeLists.txt
Este archivo define la configuración para compilar el código en C (keylogger.c) en una biblioteca nativa .so. La compilación genera libkeylogger.so, que se integrará en la 
aplicación Android.
Kotlin cargará esta librería en tiempo de ejecución con:
System.loadLibrary("keylogger")
Resultado: Se obtiene un módulo nativo en C capaz de interactuar directamente con el hardware del dispositivo.

**2. Captura de pulsaciones del teclado mediante código nativo (keylogger.c)**
**Archivo involucrado:** keylogger.c
Funcionamiento:
1. Se abre el dispositivo de entrada /dev/input/event2.
2. Se crea un hilo (pthread_t) para leer eventos del teclado en segundo plano.
3. Cada vez que una tecla es presionada, se ejecuta:
Java_com_agenciacristal_calculadora_Keylogger_sendKeyDataToServer(env, globalObj, event.code);
Este método envía la pulsación de tecla al código Kotlin.
4. Finalmente, la pulsación es enviada al servidor Flask para su almacenamiento.
Resultado: El keylogger captura directamente las teclas presionadas en dispositivos con acceso root.

**3. Comunicación entre C y Kotlin mediante JNI (Keylogger.kt)**
**Archivo involucrado:** Keylogger.kt
Funcionamiento: Se define la función nativa en Kotlin:
external fun startLogging()
Se implementa sendKeyDataToServer(keyCode: Int), que:
1. Convierte el keyCode en JSON.
2. Lo envía a un servidor Flask mediante una solicitud HTTP POST.
Resultado: Las pulsaciones del teclado detectadas en C se comunican a Kotlin, donde luego se envían al servidor.

**4. Captura de texto usando un Servicio de Accesibilidad (KeyLoggerAccessibilityService.kt)**
**Archivo involucrado:** KeyLoggerAccessibilityService.kt
Funcionamiento: Se registra como un Servicio de Accesibilidad en Android. Captura eventos de tipo TYPE_VIEW_TEXT_CHANGED en cualquier app. Cuando detecta un cambio en el texto ingresado,
lo envía al servidor Flask.
Resultado: Este método permite registrar texto sin necesidad de acceso root, lo que lo hace más efectivo en la mayoría de los dispositivos.

**5. Activación del keylogger desde la aplicación (MainActivity.kt)**
**Archivo involucrado:** MainActivity.kt
Funcionamiento: Se verifica si el Servicio de Accesibilidad está activado. Si no, solicita activación al usuario. Se ejecuta Keylogger.startLogging() para iniciar la captura de teclas.
Resultado: El keylogger se activa al abrir la aplicación, sin que el usuario sospeche de su funcionamiento.

**6. Almacenamiento de datos en el servidor Flask**
**Destino:** https://flask-server-xysa.onrender.com/log
Tanto el keylogger en C como el servicio de accesibilidad envían datos a este servidor. El servidor almacena las pulsaciones para su posterior análisis.
Resultado: Se centraliza la recolección de datos de usuarios en un servidor externo.

**Conclusión de la auditoría**
- El keylogger tiene dos métodos de captura de datos: uno basado en acceso root (/dev/input/event2) y otro basado en servicios de accesibilidad.
- Se integra de manera silenciosa en una aplicación legítima, lo que lo hace altamente evasivo.
- Los datos recolectados son enviados a un servidor Flask externo sin que el usuario lo note.
- El uso del servicio de accesibilidad permite funcionar sin necesidad de permisos root, ampliando su efectividad.
