# Rapport de recette — SpamOff V1 bêta 1

Date : 3 novembre 2026. Version applicative : `0.1.0-beta1`, code 1. minSdk 29, targetSdk 36.

## Verdict

**GO pour une bêta technique et les essais sur le Samsung A14.**

**NO-GO pour une validation matérielle définitive ou une publication publique** avant la recette sur téléphone réel et les préparatifs de publication décrits ci-dessous. Aucun test sur un téléphone réel n’a été effectué.

## Preuves de livraison

- Code testé : `2f0900ac24f11bb6cb792d67d67acfa31ab9c2c9`.
- [Exécution GitHub Actions](https://github.com/cortexAI-2025/SpamOff/actions/runs/41672959846) : compilation et huit jobs instrumentés réussis.
- [Artefact Android 15](https://github.com/cortexAI-2025/SpamOff/actions/runs/41672959846/artifacts/13249807273) : APK exacts utilisés dans les scénarios, rapports et captures.
- Les deux APK remis proviennent de `verified-apks` dans cet artefact ; ils ne sont pas des recompilations locales ultérieures.

| Fichier | Octets | SHA-256 |
|---|---:|---|
| SpamOff-V1-beta1-universal.apk | 3 035 548 | `78bfbdaa0728a431b1f4ca033456d31f9715d4d104afc63e90d4d3612bdd94fa` |
| SpamOff-V1-beta1-universal-debug.apk | 28 591 387 | `7c960b70cc4054201a11d9ffe68ea29d0bfb2ecc07141e7b9468a2c8a0425c10` |

L’APK optimisé est recommandé pour le Samsung A14 : **3,04 Mo**. Le debug de développement fait **28,59 Mo**. Les deux portent le même identifiant `fr.spamoff` : installer une seule variante à la fois. Signatures de test, pas de clé de publication.

Signature APK v2 vérifiée, APK universels arm64-v8a / armeabi-v7a / x86 / x86_64. Alignements ZIP et ELF 64 bits à 16 Kio contrôlés. Une exécution sur un système configuré avec des pages de 16 Kio reste à effectuer avant publication.

## Résultats automatisés

- 20 tests unitaires : réussite, aucun échec ni test ignoré.
- Android Lint debug : aucun problème signalé dans la vérification locale finale.
- 13 tests instrumentés distincts exécutés sur chacun des huit niveaux API : **104 exécutions réussies**, plus une relance du contrôle d’ouverture/rôle pour chaque niveau.
- Scénarios Telecom Android 15 : **5/5 réussis**.
- APK optimisé Android 15 : **4/4 réussis**, dont téléchargement réel HTTPS depuis l’interface et appels injectés dans Telecom.

| Android | API | Installation, interface, rôle et tests Room |
|---|---:|---|
| 10 | 29 | 13/13 réussis |
| 11 | 30 | 13/13 réussis |
| 12 | 31 | 13/13 réussis |
| 12L | 32 | 13/13 réussis |
| 13 | 33 | 13/13 réussis |
| 14 | 34 | 13/13 réussis |
| 15 | 35 | 13/13 réussis + scénarios d’appels |
| 16 | 36 | 13/13 réussis |

Les listes blanche/noire, la désactivation, la normalisation internationale, le dédoublonnage, le rejet d’un fichier corrompu/incomplet et la conservation de la base sont couverts. Un déclencheur SQLite de test provoque un échec pendant l’insertion : la suppression des anciennes règles est effectivement annulée par rollback. Un test referme et rouvre une base sur disque pour vérifier sa persistance. Le compteur utilise une transaction compatible avec SQLite sur Android 10.

Les scénarios d’appels utilisent l’API officielle et la téléphonie de l’émulateur. Ils contrôlent l’état courant de TelephonyRegistry et le compteur des décisions de rejet ; ils ne se limitent pas à appeler le repository directement. Le rôle est attribué par shell dans cette recette : le dialogue utilisateur Samsung, notamment son refus, reste à tester manuellement.

## Matrice des dix exigences de recette

| Exigence | Résultat et limite |
|---|---|
| Installation Android 10–15 | Réussie sur émulateurs ; Android 16 testé également. Matériel A14 non testé. |
| Attribution du rôle | Qualification et attribution réelles par le système en CI. Sélection/refus via le dialogue Samsung à faire. |
| Numéro autorisé | Appel simulé conservé en sonnerie, aucun rejet compté ; vérifié aussi dans l’APK optimisé. |
| Numéro bloqué | Rejet et incrément du compteur ; vérifié aussi dans l’APK optimisé. |
| Désactivation | Appel simulé autorisé malgré une règle personnelle de blocage. |
| Hors connexion | Blocage vérifié après coupure du Wi-Fi et des données, voix conservée. |
| Mise à jour | Téléchargement HTTPS et installation depuis l’interface de la bêta optimisée réussis. Planification WorkManager quotidienne configurée ; un cycle réel de 24 h n’a pas été observé. |
| Liste corrompue ou incomplète | Rejet par SHA-256/validation et conservation de la base, tests réussis. |
| Redémarrage | Réussi après déverrouillage, restauration des rôles et stabilisation du système ; sans rouvrir SpamOff après le redémarrage ni lui réattribuer le rôle. |
| Erreurs et permissions refusées | Erreurs de données/mise à jour testées ; refus du dialogue Samsung à vérifier sur le téléphone. |

### Limite constatée pendant le démarrage Android

Lors d’essais précédents, un appel injecté immédiatement après le démarrage n’a pas été remis par Android au service de filtrage. Il a donc été autorisé. Ce résultat n’est pas effacé du verdict : **la protection n’est pas garantie pendant le démarrage ni avant déverrouillage**. La recette finale attend le système déverrouillé, le rôle restauré, puis 30 secondes de stabilisation. Elle ne réattribue pas le rôle et n’ouvre pas l’application après le redémarrage. Ce comportement doit être vérifié sur One UI avant toute promesse commerciale.

## Données et confidentialité

Base réellement embarquée : **0 numéro individuel communautaire et 22 préfixes ARCEP**. Ces plages peuvent servir à des appels légitimes : leur blocage est facultatif et initialement désactivé. La liste blanche est prioritaire. Le filtrage manuel et les plages téléchargées restent locaux et fonctionnent sans Internet.

Aucun accès Contacts, Journal d’appels, SMS, Micro ou service d’accessibilité n’est déclaré. Les permissions normales de réseau, réveil et redémarrage proviennent des besoins de téléchargement/WorkManager. Les signalements ne sont pas envoyés : aucun backend communautaire n’est activé.

Le compteur mesure les décisions de rejet transmises à Android, pas une confirmation de l’opérateur. Les appels masqués/non présentés au service, les contacts exemptés par Android et les appels VoIP hors Telecom ne sont pas couverts universellement.

## Étape matérielle obligatoire

Suivre [DEVICE_ACCEPTANCE.md](DEVICE_ACCEPTANCE.md) sur le Samsung Galaxy A14 avec un second téléphone **dont le numéro n’est pas dans les contacts**. Vérifier notamment le rôle et son refus, les appels réels autorisés/bloqués, la désactivation, Wi-Fi/données coupés avec voix active, le redémarrage, One UI et les deux SIM si disponibles. Aucun de ces essais matériels n’est annoncé comme réussi.

Avant Google Play : identité/contact de l’éditeur, politique publique, clé de publication, contrôles matériels/16 Kio, vérification des notices/licences et accord explicite du propriétaire. Aucune publication Google Play n’a été effectuée. Voir [GOOGLE_PLAY.md](GOOGLE_PLAY.md).
