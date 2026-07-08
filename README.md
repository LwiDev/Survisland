# Survisland

Plugin Minecraft Paper pour Survisland, avec intégration Discord embarquée (JDA).

## Stack technique

- **Minecraft / Paper API**: 26.2 (alpha)
- **Java**: 25
- **Build**: Gradle 9.6.1
- **Commandes**: Brigadier (API de commandes native de Paper) via une petite CommandAPI maison
- **Discord**: JDA 6.5.0 (bot embarqué dans le plugin, pas de service séparé)

## Structure du projet

```
api/      → briques génériques réutilisables sur lesquelles le plugin s'appuie (aucune logique propre à Survisland)
plugin/   → le plugin Minecraft (commandes, listeners, bot Discord, skins, chat spectateur)
```

Il n'y a pas de module ou service Discord séparé : le bot JDA tourne à l'intérieur du plugin
(`plugin/src/main/java/com/lwidev/survisland/discord/EmbeddedDiscordBot.java`) et se connecte
directement à l'API Discord (pas d'API REST intermédiaire).

Doc technique du module `api` générée automatiquement à chaque push sur `main` :
[lwidev.github.io/Survisland](https://lwidev.github.io/Survisland/).

## Commandes

Les commandes sont enregistrées via Brigadier (pas de bloc `commands:`/`permissions:` dans
`plugin.yml` — tout est généré par le code, voir `com.lwidev.survisland.api.command`).

| Commande | Alias | Arguments | Description | Permission | Défaut |
|---|---|---|---|---|---|
| `/live` | — | `<message>` | Envoie un message sur Discord | `survisland.live` | op |
| `/setlive` | — | `[channel_id]` | Configure le channel Discord cible | `survisland.setlive` | op |
| `/confess` | `/conf` | `<message>` | Envoie un message dans son confess Discord lié | `survisland.confess` | joueur |
| `/link` | — | — | Génère un code pour lier son compte au confess Discord (`/verify <code>` côté Discord) | `survisland.link` | joueur |
| `/camp` | `/campement` | — | Affiche la direction vers son campement dans l'action bar | `survisland.camp` | joueur |
| `/pause` | — | — | Active/désactive la pause du jeu (gel des joueurs) | `survisland.pause` | op |
| `/skin` | — | `force <joueurs> <skin>`<br>`restore <joueurs>`<br>`list` | Force un skin (pseudo ou texture) pour un ou plusieurs joueurs, en ligne ou non (accepte les target-selectors `@p`/`@a`/etc.)<br>Restaure le skin original d'un ou plusieurs joueurs<br>Liste les skins forcés actifs | `survisland.skin.force`<br>`survisland.skin.restore`<br>`survisland.skin.list` | op |
| `/follow` | — | `<joueur>`<br>`stop` | Suit un joueur en mode spectateur (téléportation automatique s'il s'éloigne)<br>Arrête le suivi en cours | `survisland.follow`<br>`survisland.follow.stop` | joueur |
| `/menu` | — | — | Ouvre le centre de contrôle GUI (équipes, joueurs, partie) | `survisland.menu` | op |

## Charte graphique

Les couleurs de la marque sont centralisées dans `BrandUtils` (module `api`) — toujours en hex-exact
(`TextColor`), jamais via les 16 couleurs nommées legacy, pour un rendu identique partout (menus,
messages). À utiliser par rôle, pas au goût :

| Rôle | Couleur | Hex | Usage |
|---|---|---|---|
| `PRIMARY` | Orange Survisland | `#FF8C1A` | Éléments les plus importants d'un écran (titres, action principale) |
| `SECONDARY` | Jaune doré | `#FFC94D` | Navigation/actions courantes |
| `TERTIARY` | Bleu azur | `#1BA8E0` | Actions utilitaires/admin appliquées à une cible (gamemode, téléportation, effets...) |

Les couleurs sémantiques (vert/rouge pour confirmer-annuler, activé-désactivé, succès-erreur) sont
volontairement séparées de cette triade de marque. Pour les messages de commande, passer par
`MessageUtils` (`sendSuccessMessage`, `sendErrorMessage`, ...) plutôt que de coder la couleur à la main
— c'est déjà branché sur la bonne couleur (rouge pour les erreurs, etc.) et garde le préfixe
`[Survisland]` cohérent partout.

## Configuration (`config.yml`)

```yaml
discord:
  token: ""                 # ou variable d'env DISCORD_BOT_TOKEN
  live-channel-id: ""
  message-format: "**{sender} »** {message}"
  confess-format: "{message}"

menu:
  announcement-presets:
    - "Immunité dans 30 minutes"
    - "Immunité dans 10 minutes"
    - "Confort dans 30 minutes"
    - "Confort dans 10 minutes"
  vote-recipients:
    gold: "GoldVision98"
    marcus: "Marcouscous"

chatspec:
  enabled: false
  prefix: "[SPEC]"
  color: "GRAY"

follow:
  max-distance: 15           # blocs
  check-interval-ticks: 10   # 20 ticks = 1 seconde

skins:
  enabled: false
  cache-duration: 3600       # secondes
```

Le token du bot Discord peut être fourni via `discord.token` dans `config.yml` ou via la variable
d'environnement `DISCORD_BOT_TOKEN` (prioritaire).

Chat spectateur : si `chatspec.enabled` est activé, les messages des joueurs en mode spectateur
sont automatiquement reformatés et redirigés vers les spectateurs et les joueurs ayant la
permission `survisland.chatspec.receive`. Pas de commande de toggle en jeu.

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
