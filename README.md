# Popis Alerta

Aplicación Android de código abierto para teleasistencia doméstica no invasiva.

El proyecto explora la reutilización de un teléfono Android dedicado como dispositivo de apoyo para detectar actividad relacionada con el acceso a una habitación crítica —por ejemplo, un baño— mediante los sensores integrados del móvil: luz, movimiento y, de forma experimental, audio. Los eventos se almacenarán localmente y podrán compartirse mediante las opciones nativas de Android.

## Estado

**En desarrollo temprano.**

La versión actual solo contiene la base técnica Android: Kotlin, Jetpack Compose, navegación y pruebas iniciales. La detección mediante sensores, el historial, la exportación y las alertas todavía no están implementados.

## Objetivos

- Monitorización local mediante sensores nativos del dispositivo.
- Registro cronológico local de eventos.
- Interfaz accesible y sencilla.
- Compartir informes mediante el selector nativo de Android.
- Desarrollo abierto bajo licencia GPL-3.0.

## Requisitos de desarrollo

- JDK 21 para ejecutar Gradle.
- Android SDK Platform 36.
- Android SDK Build-Tools 36.x.
- Un dispositivo Android para las pruebas de sensores.

## Compilar

Desde la raíz del repositorio:

```bash
./gradlew assembleDebug
```

El APK de depuración se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Aviso importante

Popis Alerta es un proyecto experimental. No es un producto sanitario, no sustituye la atención humana y no debe utilizarse como único sistema de emergencia. Los sensores de un teléfono pueden fallar o generar falsos positivos y falsos negativos.

## Licencia

Este proyecto se distribuye bajo la licencia [GNU General Public License v3.0](LICENSE).
