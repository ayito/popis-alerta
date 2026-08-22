#!/bin/bash
# Apaga el emulador de Android de forma segura

set -e  # Salir si algún comando falla

echo "🔍 Buscando emuladores en ejecución..."

# Obtener lista de emuladores conectados
EMULATORS=$(adb devices | grep 'emulator-' | grep 'device' | awk '{print $1}')

if [ -z "$EMULATORS" ]; then
    echo "⚠️  No hay emuladores en ejecución."
    exit 0
fi

echo "📱 Emuladores encontrados:"
echo "$EMULATORS"
echo ""

for EMULATOR in $EMULATORS; do
    echo "🛑 Apagando $EMULATOR..."
    adb -s "$EMULATOR" emu kill 2>/dev/null || true
done

# Esperar a que se desconecten
echo "⏳ Esperando a que los emuladores se apaguen..."
sleep 2

# Verificar
REMAINING=$(adb devices | grep 'emulator-' | grep 'device' | awk '{print $1}')
if [ -z "$REMAINING" ]; then
    echo "✅ Todos los emuladores apagados correctamente."
else
    echo "⚠️  Algunos emuladores siguen activos: $REMAINING"
    echo "   Intenta ejecutar de nuevo o apágalos manualmente con:"
    echo "   adb -s <device> emu kill"
fi
