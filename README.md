# SpamOff — bêta Android native

Application gratuite et sans publicité de filtrage local des appels. Aucun compte.
Kotlin, Compose Material 3, MVVM, Room, DataStore, WorkManager, Retrofit.
Android 10 minimum (API 29), cible Android 16 (API 36), Samsung A14 compatible en principe : la recette matérielle reste obligatoire.

## Démarrer sur Samsung A14

1. Installer l’APK debug fourni depuis une source autorisée. Autoriser temporairement cette source dans Android si nécessaire.
2. Ouvrir SpamOff et toucher **Autoriser dans Android** ; choisir SpamOff comme application de filtrage.
3. Activer le filtrage. Ajouter un numéro de test dans **Listes > Bloquer**.
4. Facultatif : activer **Bloquer les plages d’appels automatisés**. Cette option peut bloquer des appels légitimes ; la liste blanche est prioritaire.
5. Appeler depuis un second téléphone dont le numéro n’est pas dans les contacts. Le désactiver puis rappeler pour comparer.
6. Répéter hors connexion Internet, en conservant le réseau mobile voix actif. Le mode avion coupe généralement aussi la voix : il ne convient pas à cette recette.

Aucune permission Contacts, SMS, Micro ou Journal d’appels n’est demandée. Les appels masqués ou non remis par Android ne peuvent pas être filtrés. Les contacts sont normalement exemptés par Android. Les appels VoIP/WhatsApp ne relèvent pas de ce service.

## Fonctionnalités

- Filtrage CallScreeningService sans requête réseau lors d’un appel ; délai interne de 2,8 s avec autorisation de l’appel en cas d’erreur.
- Liste blanche prioritaire, blocage personnel, 22 préfixes ARCEP embarqués (aucun numéro individuel communautaire embarqué).
- Signalement **local uniquement**, catégorie et commentaire ; aucun partage automatique.
- Téléchargement HTTPS, taille limitée, SHA-256, version et schéma vérifiés, dédoublonnage et transaction Room.
- Mise à jour manuelle et quotidienne facultative via WorkManager ; dernière base valide conservée en cas d’échec.
- Thèmes clair, sombre, système. Compteur des décisions de blocage transmises à Android (pas de confirmation opérateur).

## Compiler

Installer Android Studio, JDK 17, SDK 36 et Build Tools 35.0.0. Configurer `ANDROID_HOME` ou `local.properties` (non versionné).

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
./gradlew connectedDebugAndroidTest
```

APK : `app/build/outputs/apk/debug/app-debug.apk` (signé avec la clé debug locale, réduit par R8).
La variante release n’est pas signée : ne pas la distribuer avant la préparation de la clé du propriétaire.
Pour mettre à jour un APK déjà installé, il faut conserver la même clé de signature. La clé debug temporaire de cette compilation n’est pas une clé de publication et n’est pas incluse dans Git.

## Tests et limites

Voir [rapport de tests](docs/TEST_REPORT.md), [recette Samsung](docs/DEVICE_ACCEPTANCE.md) et [architecture](docs/ARCHITECTURE.md).
Le workflow teste et compile puis prévoit des tests instrumentés pour les API 29–36. La présence du workflow ne prouve pas son exécution : consulter les résultats réels.

## Données et confidentialité

Voir [sources](docs/SOURCES_AND_LICENSES.md), [confidentialité](docs/PRIVACY.md), [mise à jour des listes](data/README.md) et [publication Play](docs/GOOGLE_PLAY.md).
Pas de serveur obligatoire. Les préfixes ARCEP indiquent une catégorie technique d’appels automatisés ; ils ne constituent pas une liste de fraudeurs.
