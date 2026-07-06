# Survisland

Plugin Minecraft Paper pour Survisland, avec intégration Discord embarquée (JDA).

## Stack technique

- **Minecraft / Paper API**: 26.2 (alpha)
- **Java**: 25
- **Build**: Gradle 9.6.1
- **Discord**: JDA 6.5.0 (bot embarqué dans le plugin, pas de service séparé)

## Structure du projet

```
plugin/   → le plugin Minecraft (commandes, listeners, bot Discord, skins, chat spectateur)
```

Il n'y a pas de module ou service Discord séparé : le bot JDA tourne à l'intérieur du plugin
(`plugin/src/main/java/com/lwidev/survisland/discord/EmbeddedDiscordBot.java`) et se connecte
directement à l'API Discord (pas d'API REST intermédiaire).

## Commandes

| Commande | Alias | Description | Permission |
|---|---|---|---|
| `/live <message>` | — | Envoie un message sur Discord | `survisland.live.send` |
| `/setlive [channel_id]` | — | Configure le channel Discord cible | `survisland.live.config` |
| `/confess <message>` | `/conf` | Envoie un message dans son confess Discord lié | `survisland.confess.send` |
| `/link` | — | Génère un code pour lier son compte au confess Discord (`/verify <code>` côté Discord) | `survisland.confess.link` |
| `/camp` | `/campement` | Affiche la direction vers son campement dans l'action bar | `survisland.camp.use` |
| `/pause` | — | Active/désactive la pause du jeu (gel des joueurs) | `survisland.pause.use` |

## Fonctionnalités additionnelles

- **Chat spectateur** : si activé dans `config.yml` (`chatspec.enabled`), les messages des joueurs
  en mode spectateur sont automatiquement reformatés et redirigés vers les spectateurs et les
  joueurs ayant la permission `survisland.chatspec.receive`. Pas de commande de toggle en jeu.
- **Skins forcés** : un skin forcé pour un joueur (stocké dans `skin-data.yml`) est réappliqué
  automatiquement à sa connexion. Il n'y a actuellement pas de commande en jeu pour en assigner un
  nouveau.

## Configuration (`config.yml`)

```yaml
discord:
  token: ""                 # ou variable d'env DISCORD_BOT_TOKEN
  live-channel-id: ""
  message-format: "**{sender} »** {message}"
  confess-format: "{message}"

chatspec:
  enabled: false
  prefix: "[SPEC]"
  color: "GRAY"

skins:
  enabled: false
  cache-duration: 3600       # secondes
```

Le token du bot Discord peut être fourni via `discord.token` dans `config.yml` ou via la variable
d'environnement `DISCORD_BOT_TOKEN` (prioritaire).

## Développement

```bash
# Build complet
./gradlew build

# Lancer un serveur Paper local avec le plugin chargé
./gradlew runServer

# Nettoyer les artefacts de build
./gradlew clean
```

## Workflow de contribution

Ce dépôt suit un modèle strict : **aucun changement n'est mergé sur `main` sans validation explicite
du mainteneur (LwiDev)**.

1. Créer une branche depuis `main` (`feature/xxx`, `fix/xxx`).
2. Faire les changements, vérifier que `./gradlew build` passe.
3. Ouvrir une Pull Request vers `main` avec une description claire de ce qui change et pourquoi.
4. Attendre la relecture et l'approbation explicite avant tout merge — pas de merge automatique,
   pas de push direct sur `main`.
5. Une fois approuvée, la PR est mergée (par le mainteneur ou après son accord explicite).

Aucun outil ne doit pousser sur `main` ou merger une PR sans confirmation préalable de LwiDev.

## Versioning

Le dépôt utilise des tags git `vMAJOR.MINOR.PATCH` (semver). Un tag est créé à chaque merge sur
`main` (patch incrémenté par défaut) ; un bump minor ou major se fait explicitement selon la nature
du changement (nouvelle fonctionnalité rétrocompatible → minor, changement incompatible → major).
