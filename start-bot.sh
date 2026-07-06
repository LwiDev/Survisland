#!/bin/bash

# Script de lancement du bot Discord Survisland
# Usage: ./start-bot.sh [token] [port]

echo "=== Survisland Discord Bot Launcher ==="

# Vérifier la version Java
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
JAVA_MAJOR=$(echo $JAVA_VERSION | cut -d'.' -f1)

if [ "$JAVA_MAJOR" -lt 21 ]; then
    echo "⚠️  Attention: Java $JAVA_VERSION détecté, Java 21+ recommandé"
    echo "   Le bot pourrait ne pas fonctionner correctement"
    echo ""
fi

# Configuration par défaut
DEFAULT_PORT=8080
BOT_TOKEN=${1:-$DISCORD_BOT_TOKEN}
BOT_PORT=${2:-$BOT_PORT}
BOT_PORT=${BOT_PORT:-$DEFAULT_PORT}

# Vérifier si le token est fourni
if [ -z "$BOT_TOKEN" ]; then
    echo "❌ Token Discord manquant!"
    echo ""
    echo "Usage:"
    echo "  ./start-bot.sh YOUR_BOT_TOKEN [port]"
    echo "  Ou définir DISCORD_BOT_TOKEN comme variable d'environnement"
    echo ""
    exit 1
fi

# Build du projet si nécessaire
if [ ! -f "discord/build/libs/SurvislandBot-1.0-SNAPSHOT.jar" ]; then
    echo "🔨 Build du bot Discord..."
    ./gradlew :discord:build
    
    if [ $? -ne 0 ]; then
        echo "❌ Échec du build!"
        exit 1
    fi
fi

echo "🚀 Lancement du bot Discord..."
echo "   Token: ${BOT_TOKEN:0:20}..."
echo "   Port: $BOT_PORT"
echo ""

# Lancer le bot avec les variables d'environnement
export DISCORD_BOT_TOKEN="$BOT_TOKEN"
export BOT_PORT="$BOT_PORT"

java -jar discord/build/libs/SurvislandBot-1.0-SNAPSHOT.jar