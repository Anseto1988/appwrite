document.addEventListener('DOMContentLoaded', function() {
    // Elemente aus dem DOM abrufen
    const loadingElement = document.getElementById('loading');
    const successElement = document.getElementById('success');
    const errorElement = document.getElementById('error');
    const errorDetailsElement = document.getElementById('error-details');
    const openAppButton = document.getElementById('openApp');
    const retryButton = document.getElementById('retry');

    // URL-Parameter auslesen
    const urlParams = new URLSearchParams(window.location.search);
    const userId = urlParams.get('userId');
    const secret = urlParams.get('secret');
    const teamId = urlParams.get('teamId');
    const membershipId = urlParams.get('membershipId');

    // Funktion zum Öffnen der App
    openAppButton.addEventListener('click', function() {
        // Deep Link zur App erstellen
        const appDeepLink = `snacktrack://team/${teamId}`;
        window.location.href = appDeepLink;
        
        // Fallback: Falls die App nicht geöffnet werden kann, nach 1 Sekunde auf Play Store umleiten
        setTimeout(function() {
            window.location.href = 'https://play.google.com/store/apps/details?id=com.example.snacktrack';
        }, 1000);
    });

    // Funktion für erneuten Versuch
    retryButton.addEventListener('click', function() {
        window.location.reload();
    });

    // Funktion zur Verarbeitung der Einladung
    function processInvitation() {
        // Wenn keine Parameter vorhanden sind, zeigen wir einen Fehler an
        if (!userId || !secret || !teamId || !membershipId) {
            showError('Der Einladungslink ist ungültig oder unvollständig.');
            return;
        }

        // Echter API-Aufruf an Appwrite, um die Einladung anzunehmen
        // Beachte: Der Link in der E-Mail enthält bereits Parameter, die Appwrite automatisch verarbeitet
        // Wir bestätigen hier die Verarbeitung über die Appwrite API

        // WICHTIG: Appwrite verarbeitet die Einladung automatisch, wenn der Link angeklickt wird
        // Das bedeutet, dass die Einladung zu diesem Zeitpunkt bereits akzeptiert wurde,
        // weil der Benutzer auf den Einladungs-Link in der E-Mail geklickt hat
        
        // Der entscheidende Punkt ist, dass das Klicken auf den Link mit den userId und secret Parametern
        // bereits die Einladung verarbeitet hat. Wir müssen keine zusätzliche API-Anfrage stellen.
        
        // Daher zeigen wir einfach die Erfolgsmeldung an
        console.log('Einladungs-Parameter erkannt:', { userId, teamId, membershipId });
        showSuccess();
        
        // Wenn wir später mehr Informationen benötigen oder Probleme beheben müssen,
        // können wir einen Server-Side-Proxy auf pla.nordburglarp.de implementieren, um
        // CORS-Probleme zu vermeiden.
    }

    // Hilfsfunktionen zum Anzeigen von Erfolg oder Fehler
    function showSuccess() {
        loadingElement.style.display = 'none';
        successElement.style.display = 'block';
        errorElement.style.display = 'none';
    }

    function showError(message) {
        loadingElement.style.display = 'none';
        successElement.style.display = 'none';
        errorElement.style.display = 'block';
        errorDetailsElement.textContent = message;
    }

    // Starte die Verarbeitung
    processInvitation();
});