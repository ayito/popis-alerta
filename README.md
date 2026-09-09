# Popis Alerta

Aplicación Android de código abierto para teleasistencia doméstica no invasiva.

Popis Alerta explora la reutilización de un teléfono Android dedicado como dispositivo de apoyo para detectar actividad relacionada con el acceso a una habitación crítica —por ejemplo, un baño— usando sensores integrados del móvil. El objetivo es registrar posibles visitas de forma local y avisar mediante notificaciones, como complemento a la supervisión humana.

> **Aviso importante:** Popis Alerta es un proyecto experimental. No es un producto sanitario, no sustituye la atención humana ni debe utilizarse como único sistema de emergencia. Los sensores de un teléfono pueden fallar y producir falsos positivos o falsos negativos.

## Estado del proyecto

**En desarrollo activo.**

La aplicación ya permite detectar señales de luz y movimiento, correlacionarlas para registrar posibles visitas, guardar el historial localmente y enviar notificaciones al dispositivo.

Las llamadas telefónicas automáticas todavía no están implementadas. La configuración del teléfono de avisos existe como preparación para esa función futura; por ahora, guardar un número no realiza ninguna llamada.

## Funcionalidad implementada

- Detección de variaciones de luz mediante el sensor de luz del dispositivo, cuando está disponible.
- Detección de movimiento mediante el acelerómetro.
- Métrica de movimiento basada en aceleración neta:

  \[
  \left|\sqrt{x^2 + y^2 + z^2} - 9,81\right|
  \]

- Umbrales de luz y movimiento configurables desde la aplicación.
- Valores iniciales de configuración:
  - Luz: 30 lux.
  - Movimiento: 1,5 m/s².
- Calibración en tiempo real: muestra las lecturas de luz y movimiento y su relación con el umbral guardado.
- Registro local de picos de luz y movimiento mediante Room.
- Detección y registro de posibles visitas, con control de enfriamiento para evitar avisos repetidos.
- Historial local de visitas detectadas.
- Notificaciones locales cuando se confirma una posible visita.
- Pantalla de alertas para pausar y reactivar los avisos/detección.
- El botón de pausa evita procesar y registrar nuevas señales mientras los avisos estén pausados.
- Configuración persistente mediante `SharedPreferences`.
- Campo de teléfono para avisos por llamada:
  - Puede dejarse vacío.
  - Acepta un teléfono español de nueve dígitos, que se guarda normalizado como `+34...`.
  - Acepta números internacionales en formato con prefijo `+`.
  - Todavía no inicia llamadas ni solicita permisos de telefonía.
- Interfaz disponible en español e inglés.
- Formato decimal adaptado al idioma activo de la aplicación; en español se muestra con coma.

## Flujo de detección

1. La aplicación registra lecturas de luz y movimiento mientras la detección está activa.
2. Un valor que supera el umbral correspondiente se registra como pico local.
3. `RoomVisitDetector` correlaciona los picos recientes según la ventana temporal configurada.
4. Si se confirma una posible visita y no se aplica el periodo de enfriamiento, se guarda en la base de datos local.
5. Se publica una notificación local de alerta.
6. Si la detección está pausada, no se procesan ni guardan nuevas señales.

## Ajustes

La pantalla **Configuración** permite:

- Consultar la lectura actual de luz en lux.
- Consultar la aceleración neta actual en m/s².
- Consultar si cada lectura está por encima o por debajo de su umbral guardado.
- Modificar y guardar los umbrales.
- Introducir un teléfono para futuras llamadas de aviso.
- Introducir decimales con coma (`1,5`), punto (`1.5`) o sin separador (`2`).
- Ver los valores normalizados con el formato regional activo.

Los valores modificados se aplican únicamente al pulsar **Guardar cambios**.

## Datos locales

La aplicación utiliza almacenamiento local:

- `SharedPreferences` para umbrales, estado de detección y teléfono de avisos.
- Room para picos de sensores, visitas y el historial visible.

Al borrar el historial desde la aplicación se eliminan las visitas registradas. La limpieza de logs de diagnóstico y las llamadas automáticas son trabajo pendiente.

## Requisitos de desarrollo

- JDK 17.
- Android SDK Platform 36.
- Android SDK Build-Tools 36.x.
- Un dispositivo Android físico para probar sensores de luz y movimiento.
- ADB para instalación y diagnóstico opcionales.

## Compilar e instalar

Desde la raíz del repositorio:

```bash
./gradlew :app:assembleDebug
```

El APK de depuración se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Para instalarlo en un dispositivo conectado:

```bash
./gradlew :app:installDebug
```

Para comprobar los dispositivos ADB disponibles:

```bash
adb devices
```

## Pruebas manuales básicas

1. Instala la aplicación en un teléfono con acelerómetro; el sensor de luz es opcional.
2. Abre **Configuración** y verifica que aparecen lecturas en tiempo real.
3. Configura los umbrales deseados, por ejemplo 30 lux y 1,5 m/s².
4. Prueba los decimales con coma, punto y un valor entero.
5. Guarda un teléfono válido, por ejemplo `600 123 456`, y confirma que se normaliza a `+34600123456`.
6. Prueba un valor no válido, por ejemplo `123`, y verifica que aparece el mensaje de validación.
7. Provoca una condición de luz o movimiento que supere los umbrales.
8. Comprueba que se registra una visita y aparece una notificación.
9. Pausa la detección y confirma que no se registran nuevas visitas; reactívala para reanudar el comportamiento.

Para una captura de diagnóstico limitada al proceso actual:

```bash
PID=$(adb shell pidof org.popisalerta.app | tr -d '\r')
adb logcat --pid="$PID" -v threadtime
```

El PID puede cambiar si Android cierra y reinicia la aplicación; en ese caso, vuelve a ejecutar el primer comando.

## Trabajo pendiente

- Implementar llamadas telefónicas al número configurado.
- Solicitar y gestionar permisos de telefonía cuando esa función exista.
- Definir y probar la política de reintentos de llamada:
  - Colgar tras cinco tonos sin respuesta.
  - Reintentar a los cinco segundos si no hay respuesta.
  - Reintentar al minuto si se rechaza la llamada.
  - Detener los reintentos al pausar la detección.
- Revisar y reducir los logs de diagnóstico antes de producción.
- Asociar la limpieza de logs propios con el borrado de historial, si se implementa almacenamiento local de logs.
- Añadir pruebas automatizadas para normalización de teléfonos, detector de visitas y persistencia de configuración.
- Implementar exportación o compartición de historial, si se mantiene como objetivo del proyecto.

## Licencia

Este proyecto se distribuye bajo la licencia [GNU General Public License v3.0](LICENSE).
