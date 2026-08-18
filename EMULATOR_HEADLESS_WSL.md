# Emulador Android headless en WSL/AlmaLinux

Este documento describe cómo ejecutar el emulador de Android en modo headless (sin servidor gráfico) dentro de WSL/AlmaLinux, para ejecutar tests instrumentados (`connectedAndroidTest`) sin instalar nada en Windows.

## Requisitos

- WSL2 con una distribución Linux (aquí: AlmaLinux).
- Android SDK instalado en WSL, con:
  - `cmdline-tools` (para `sdkmanager` y `avdmanager` / `android`).
  - `emulator`.
  - `platform-tools` (para `adb`).
- Espacio libre: al menos 10–15 GB.
- RAM disponible: recomendable 8 GB o más para WSL + emulador.

## 1. Instalar el emulador y una imagen de sistema

Ajusta `$ANDROID_HOME` a tu ruta (ej. `/home/desarrollo/Android/Sdk`).

```bash
# Instalar el emulador
"$ANDROID_HOME/cmdline-tools/latest/bin/android" sdk install \
  "system-images;android-30;google_apis_playstore;x86_64"
```

## 2. Crear un AVD manualmente

Las imágenes `google_apis_playstore` no siempre incluyen `devices.xml`, por lo que es más fiable crear el AVD a mano.

```bash
# Crear directorio del AVD
mkdir -p "$HOME/.android/avd/popis_test.avd"

# Crear config.ini
cat > "$HOME/.android/avd/popis_test.avd/config.ini" << 'EOF'
avd.ini.encoding=UTF-8
AvdId=popis_test
PlayStore.enabled=true
abi.type=x86_64
avd.name=popis_test
disk.dataPartition.size=2048M
hw.cpu.arch=x86_64
hw.device.name=pixel_4
image.sysdir.1=system-images/android-30/google_apis_playstore/x86_64
tag.id=google_apis_playstore
tag.display=Google Play
EOF

# Crear el .ini que referencia al AVD
cat > "$HOME/.android/avd/popis_test.ini" << 'EOF'
avd.ini.encoding=UTF-8
path=/home/desarrollo/.android/avd/popis_test.avd
path.rel=avd/popis_test.avd
target=android-30
EOF
```

Ajusta la ruta `path=` a tu usuario si es diferente.

## 3. Arrancar el emulador en modo headless

```bash
"$ANDROID_HOME/emulator/emulator" \
  -avd popis_test \
  -no-window \
  -no-audio \
  -no-boot-anim \
  -gpu swiftshader_indirect \
  -no-snapshot
```

Notas:

- `-no-window` evita abrir interfaz gráfica.
- `-no-audio`, `-no-boot-anim` y `-no-snapshot` aceleran el arranque.
- `-gpu swiftshader_indirect` usa renderizado por software compatible con entornos sin GPU dedicada.

Deja esta terminal con el emulador corriendo.

## 4. Verificar que adb ve el emulador

En otra terminal:

```bash
"$ANDROID_HOME/platform-tools/adb" devices
```

Deberías ver:

```text
List of devices attached
emulator-5554   device
```

Si no aparece, espera unos segundos y vuelve a ejecutar el comando.

## 5. Ejecutar tests instrumentados

Desde la raíz del proyecto:

```bash
./gradlew connectedAndroidTest
```

Esto instalará la app y los tests en el emulador y los ejecutará.

## 6. Detener el emulador

Cuando termines, puedes detener el emulador con:

```bash
"$ANDROID_HOME/platform-tools/adb" emu kill
```

o simplemente cerrar la terminal donde está ejecutándose.

## Notas adicionales

- No es necesario instalar Android Studio ni ningún componente en Windows; todo corre dentro de WSL.
- Si cambias de imagen de sistema (otra versión de Android), actualiza `image.sysdir.1` y `target` en los archivos `.ini` del AVD.
- Para añadir más tests UI, usa `testTag` en los elementos clave y, si hace falta, expón los composables secundarios como `internal` para testearlos directamente.
