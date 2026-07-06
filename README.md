# Survisland Monorepo

Un monorepo complet pour le plugin Minecraft Paper Survisland avec intégration Discord.

## Fonctionnalités

### 🎮 Plugin Minecraft

#### Commandes
- **`/camp`**: Ajoute la direction vers le centre du campement dans l'Action bar
- **`/live <message>`**: Envoie un message du serveur vers Discord
- **`/set_live <channel_id>`**: Configure le channel Discord cible

#### Gestion des Skins
- Forcer des skins spécifiques pour les épreuves
- Support des skins personnalisés
- Cache intelligent des données

### 🤖 Bot Discord

- **JDA (Java Discord API)** pour une intégration native
- **API REST** intégrée pour recevoir les messages du plugin
- **Endpoints disponibles**:
  - `GET /health` - Statut du bot
  - `POST /api/send-message` - Envoyer un message

### 📚 Module Shared

- **Models communs**: Action, MessageRequest, DiscordConfig
- **DTOs** pour la communication inter-modules
- **Configuration centralisée**

### 4. Configuration in-game

```bash
# Configurer le channel Discord
/set_live 123456789012345678

# Envoyer un message test
/live Serveur en ligne!

# Ajouter une action
/surv action add "aveuglement" "/effect give @s blindness 10 255,/tell @s Tu es aveuglé!"
```

## Permissions

### Actions
- `survisland.action.menu` - Ouvrir le menu (défaut: true)
- `survisland.action.add` - Ajouter des actions (défaut: op)
- `survisland.action.modify` - Modifier des actions (défaut: op)
- `survisland.action.grant/revoke` - Gérer les permissions (défaut: op)

### Discord
- `survisland.live.send` - Envoyer messages Discord (défaut: op)
- `survisland.live.config` - Configurer Discord (défaut: op)

### Chat Spectateur
- `survisland.chatspec.use` - Utiliser le chat spec (défaut: true)
- `survisland.chatspec.receive` - Recevoir les messages spec (défaut: op)

## Développement

### Ajouter une nouvelle fonctionnalité

1. **Models communs** → `shared/src/main/java/com/lwidev/survisland/shared/`
2. **Plugin Minecraft** → `plugin/src/main/java/com/lwidev/survisland/`
3. **Bot Discord** → `discord/src/main/java/com/lwidev/survisland/discord/`

### Architecture des commandes

Le système utilise **Cloud Command Framework** pour:
- ✅ Autocomplétion avancée
- ✅ Validation automatique des arguments
- ✅ Suggestions contextuelles
- ✅ Gestion des permissions intégrée

### Communication Plugin ↔ Discord

```
Plugin Minecraft  →  HTTP POST  →  Bot Discord  →  Discord API
      ↓                              ↓
   /live cmd        MessageRequest   JDA.sendMessage()
```

## Support et Contribution

- **Issues**: Signalez les bugs dans les issues GitHub
- **Pull Requests**: Contributions bienvenues
- **Documentation**: Mise à jour automatique avec les nouvelles fonctionnalités

## Licence

© 2024 LwiDev - Tous droits réservés