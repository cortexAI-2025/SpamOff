# Architecture V1

`MainActivity` (Compose) → `MainViewModel` → `SpamRepository` → DAO Room / SettingsRepository DataStore.
`SpamScreeningService` utilise le même repository, sans Retrofit dans son chemin de décision.
`ListUpdateWorker` télécharge et valide le flux maintenu dans le dépôt ; Retrofit/OkHttp seulement lors d’une mise à jour.

## Décision d’appel

1. Ne traiter que les appels entrants remis par Telecom.
2. Lire le bouton de protection ; s’il est désactivé, autoriser.
3. Normaliser avec libphonenumber au format E.164, région FR par défaut. Hors France, saisir +indicatif.
4. Numéro absent/invalide : autoriser.
5. Liste blanche personnelle : autoriser, même si un préfixe correspond.
6. Liste noire personnelle : rejeter.
7. Numéro exact de la base distante : rejeter (base V1 : zéro numéro exact).
8. Préfixe : rejeter seulement si l’option ARCEP est activée explicitement.
9. Sinon autoriser.

La recherche personnelle et exacte utilise des clés indexées. Les préfixes sont recherchés par une requête `IN` sur les préfixes possibles du numéro, sans parcours de toute la base. Pas de journal des numéros appelants. Un compteur agrégé est incrémenté après l’envoi réussi de la réponse de blocage.

## Délai Android

Telecom impose une réponse dans 5 secondes. Une tâche IO supervisée calcule la décision ; une coroutine sur le thread principal attend au plus 2,8 secondes, puis envoie une autorisation en cas d’indisponibilité. L’annulation de la requête n’est pas attendue pour envoyer la réponse. Le démarrage du processus, la charge système et le comportement Samsung restent à mesurer sur matériel réel. Aucun appel HTTP dans ce chemin.

## Persistance

Room : `personal` (clé numéro, booléen autorisé), `rules` (clé valeur/type), `metadata`, `stats`, `reports` locaux. La base et DataStore sont en stockage interne ; pas de sauvegarde Android. Pas de migration destructive. La première ouverture installe la petite base embarquée, également accessible sans réseau.

## Mise à jour

Téléchargements Retrofit suspendus en IO, HTTPS seulement et redirections désactivées ; 4 Kio pour le manifeste, 8 Mio pour la liste. Vérifications SHA-256, schéma, version concordante, valeurs et liste non vide. Les doublons sont supprimés. La transaction compare la version courante, supprime et remplace les seules règles téléchargées ; les règles personnelles restent intactes. Tout échec avant commit préserve l’ancienne base.

Le manifeste et les données viennent du même éditeur HTTPS : SHA-256 protège contre corruption/incomplétude, **pas** contre la compromission du dépôt. Une signature indépendante avec clé publique embarquée est une évolution possible. L’éditeur du flux doit être protégé par 2FA et revue des changements.

WorkManager persiste sa planification après redémarrage, sous réserve des restrictions Android et de l’arrêt forcé par l’utilisateur. Le service de filtrage est lancé à la demande par Telecom ; aucun service permanent de l’application.

## API communautaire différée

Aucun endpoint ni envoi de signalement n’est activé en V1. Contrat et garanties attendues : [COMMUNITY_API.md](COMMUNITY_API.md). Les signalements locaux ne deviennent jamais automatiquement des règles partagées.

## Variantes de compilation

`debug` conserve les classes nécessaires aux tests instrumentés. `beta` reprend les optimisations R8 et la réduction des ressources de `release`, avec une signature de test pour être installable. `release` reste non signée. Les tests instrumentés portent sur debug ; un scénario séparé installe beta et pilote son interface et de vrais callbacks Telecom émulés, sans injecter le runtime de test dans l’APK réduit.

Le compteur utilise une insertion `OR IGNORE` puis un incrément dans une transaction, compatibles avec le SQLite d’Android 10. Il ne dépend pas de la syntaxe UPSERT apparue dans SQLite 3.24.
