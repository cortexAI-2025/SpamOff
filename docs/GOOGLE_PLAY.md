# Préparer une publication Google Play — aucune publication autorisée

État : bêta distribuée directement, aucune soumission effectuée.

Au 3 novembre 2026, les nouvelles applications et mises à jour pour téléphone doivent cibler Android 16 / API 36 ou supérieur. SpamOff cible API 36 et conserve minSdk 29. Revalider cette règle le jour de la soumission :
https://developer.android.com/google/play/requirements/target-sdk

1. Terminer la recette réelle et les tests de la matrice API 29–36 ; vérifier TalkBack, grandes polices, petits écrans, démarrage à froid et délai Telecom.
2. Compléter l’identité de l’éditeur, adresse de contact et politique de confidentialité publique stable. Vérifier le nom, l’icône et les droits de marque.
3. Contrôler le manifeste fusionné : aucun READ_CALL_LOG, READ_SMS, READ_CONTACTS, RECORD_AUDIO ou service d’accessibilité. L’app utilise le rôle de filtrage Android ; ne pas ajouter de permissions sensibles pour contourner ses limitations.
4. Créer et conserver une clé d’upload appartenant au propriétaire hors Git ; configurer la signature release via secrets CI ou Android Studio. Activer Play App Signing selon le choix du propriétaire.
5. Produire un AAB release signé (`./gradlew bundleRelease` après configuration de signature), incrémenter versionCode ; ne jamais soumettre la clé debug.
6. Remplir Sécurité des données après examen exact des dépendances et téléchargements ; déclarer sans publicité, aucun compte, les finalités et le traitement local. Ne pas promettre un blocage universel ou une base inexistante.
7. Préparer fiche française, icône 512×512, captures originales, classification du contenu et test fermé/interne selon les conditions du compte développeur.
8. Vérifier la politique sur les permissions et APIs sensibles : https://support.google.com/googleplay/android-developer/answer/16558241 et https://support.google.com/googleplay/android-developer/answer/10208820.
9. Faire approuver explicitement par le propriétaire toute soumission et publication.

L’API CallScreeningService à partir d’Android 10 permet de filtrer sans READ_CALL_LOG : https://developer.android.com/develop/connectivity/telecom/dialer-app/screen-calls . L’acceptation Google Play ne peut pas être garantie à partir de la seule compilation.

## Pages mémoire de 16 Kio

Des bibliothèques AndroidX incluent du code natif. Avant soumission, vérifier l’alignement ZIP (`zipalign -c -P 16 4`) et les segments ELF 64 bits, puis exécuter une recette sur un appareil ou émulateur configuré en pages de 16 Kio. Les vérifications structurelles ne remplacent pas cette exécution. Référence officielle : https://developer.android.com/guide/practices/page-sizes .
