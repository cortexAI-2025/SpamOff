# Sources et licences

Vérification documentaire : 3 novembre 2026.

## Sources de données

ARCEP, [La numérotation](https://www.arcep.fr/la-regulation/grands-dossiers-thematiques-transverses/la-numerotation.html) et [décision 2025-2215](https://www.legifrance.gouv.fr/jorf/article_jo/JORFARTI000054122970).
Transcription manuelle de faits réglementaires : 12 préfixes métropolitains et 10 préfixes ultramarins au format international. Aucun fichier propriétaire ou liste de signalements tiers n’a été importé. Ni les numéros individuels couverts par une plage, ni les fraudes supposées ne sont comptés comme une base réelle de numéros signalés.

Le code de pays des territoires ultramarins est appliqué séparément ; ne pas convertir toutes les racines en +33. Ces numéros peuvent être utilisés pour des services légitimes. Le blocage des plages est donc facultatif et initialement désactivé.

## Étude open source préalable

- [SpamBlocker aj3423](https://github.com/aj3423/SpamBlocker), licence MIT vérifiée dans LICENSE ; manifeste et implémentation de CallScreeningService examinés. Utile pour confirmer l’usage du service officiel ; ses nombreuses règles, services SMS et intégrations distantes sont hors du périmètre léger V1. Aucun code copié.
- [Google libphonenumber](https://github.com/google/libphonenumber), Apache-2.0 : réutilisé pour éviter une normalisation téléphonique artisanale.
- AndroidX Room, Compose, WorkManager, DataStore, Lifecycle : composants officiels Android, Apache-2.0.
- Kotlin et coroutines : Apache-2.0.
- Retrofit et OkHttp : Apache-2.0.
- Tests : JUnit 4 (EPL-1.0), AndroidX Test (Apache-2.0), org.json pour tests JVM (licence JSON, dépendance **de test seulement**, pas incluse à l’APK).

Les notices embarquées des dépendances ne sont pas exclues globalement du packaging. Le code original SpamOff est sous licence MIT dans LICENSE. L’icône de bouclier vectorielle est originale ; aucun logo ou visuel de Saracroche n’est repris.

Une copie du texte Apache-2.0 est disponible dans [licenses/Apache-2.0.txt](licenses/Apache-2.0.txt). Les dépendances de test ne font pas partie de l’APK applicatif.
