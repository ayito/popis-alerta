#!/bin/bash
# Inicia el emulador 'popis_test' en segundo plano y espera a que esté listo para tests

set -e  # Salir si algún comando falla

AVD_NAME="popis_test"

echo "🔍 Verificando que el AVD '$AVD_NAME' existe..."
if ! emulator -list-avds | grep -q "^${AVD_NAME}$"; then
    echo "AVDs disponibles:"
    emulator -list-avds
    echo ""
    echo "❌ Error: El AVD '$AVD_NAME' no existe."
    echo "   Para crearlo:"
    echo "   avdmanager create avd -n $AVD_NAME -k 'system-images;android-34;google_apis;x86_64'"
    exit 1
fi

echo "🚀 Iniciando emulador '$AVD_NAME'..."

# Iniciar emulador en segundo plano
"$ANDROID_HOME/emulator/emulator" \
  -avd "$AVD_NAME" \
  -no-window \
  -no-audio \
  -no-boot-anim \
  -gpu swiftshader_indirect \
  -no-snapshot &

EMULATOR_PID=$!

echo "⏳ Esperando a que el emulador esté disponible en ADB..."
adb wait-for-device

# Esperar adicional a que el sistema esté completamente bootado
echo "⏳ Esperando a que el sistema esté completamente iniciado..."
adb wait-for-device shell getprop sys.boot_completed

echo ""
echo "✅ ¡Emulador '$AVD_NAME' iniciado con éxito!"
echo "   PID: $EMULATOR_PID"
echo "   Device: $(adb devices | grep -v 'List' | grep 'device' | head -1 | awk '{print $1}')"
echo ""
echo "💡 Para ejecutar tests:"
echo "   ./gradlew connectedAndroidTest"
echo ""
echo "💡 Para apagar el emulador:"
echo "   ./apagar_emulador.sh"
