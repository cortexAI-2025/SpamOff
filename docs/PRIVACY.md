# Confidentialité — SpamOff 0.1.0

Version du 3 novembre 2026, destinée à la bêta distribuée directement.

- Pas de compte, publicité, SDK analytique ou traceur.
- Filtrage local : les numéros appelants servent à la décision puis ne sont pas conservés dans un journal SpamOff.
- Les numéros saisis, autorisations, blocages, catégories et commentaires sont stockés dans la base privée de l’application.
- Aucun accès aux contacts, micro, SMS ou journal d’appels. Aucun enregistrement de conversation.
- Le compteur local mémorise un nombre de décisions de blocage, sans numéro ni date d’appel.
- Aucun signalement n’est envoyé à un serveur. La V1 n’a pas d’API communautaire active.
- Les mises à jour sont des téléchargements HTTPS publics depuis GitHub. GitHub reçoit notamment l’adresse IP et les métadonnées réseau habituelles ; aucun numéro ni historique n’est envoyé. L’actualisation automatique peut être désactivée ; une actualisation manuelle implique le même téléchargement.
- Supprimer un numéro de la liste personnelle supprime aussi son signalement local associé. Les signalements sans blocage peuvent être supprimés en ajoutant puis supprimant le numéro, ou en effaçant toutes les données de l’application.
- Désinstaller ou effacer le stockage de l’application supprime les données locales. Les sauvegardes Android de l’application sont désactivées.

Avant publication publique, le propriétaire doit compléter l’identité et le contact de l’éditeur, héberger cette politique sur une URL publique et valider les déclarations de confidentialité requises. Cette documentation n’équivaut pas à une certification RGPD ou à une approbation Google Play.
