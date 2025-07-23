# SnackTrack Team-Einladungs-Website

Diese einfache Website dient als Callback-Ziel für Team-Einladungen in der SnackTrack-App.

## Überblick

Wenn ein Benutzer zu einem Team eingeladen wird, erhält er eine E-Mail mit einem Link. Dieser Link führt zu dieser Website, wo der Benutzer die Einladung annehmen kann. Nach der Annahme wird der Benutzer aufgefordert, die SnackTrack-App zu öffnen.

## Funktionsweise

1. Der Einladungslink enthält die Parameter `userId`, `secret`, `teamId` und `membershipId`
2. Sobald der Link angeklickt wird, verarbeitet Appwrite die Einladung im Hintergrund
3. Die Website zeigt an, dass die Einladung erfolgreich angenommen wurde
4. Der Benutzer kann über den "App öffnen"-Button zur App zurückkehren

## Deployment

Die Website sollte auf einem Server unter der Domain bereitgestellt werden, die in der Appwrite-Konfiguration als erlaubter Host eingerichtet ist (`parse.nordburglarp.de`).

```bash
# Beispiel für ein einfaches Deployment mit einem lokalen Webserver
cd team-invite-website
python -m http.server 8080
```

## Integration mit der App

In der App muss die URL im TeamRepository auf diese Website verweisen:

```kotlin
// In TeamRepository.kt
teams.createMembership(
    teamId = teamId,
    email = email,
    roles = appwriteRoles,
    url = "http://parse.nordburglarp.de/team-invite-website"
)
```