# Signalements communautaires — contrat facultatif, non activé

La V1 n’effectue aucun envoi. Pour une future version, prévoir :

- Consentement ponctuel avant tout envoi, récapitulatif explicite numéro/catégorie/commentaire, aucune transmission d’historique ou contacts.
- `POST /v1/reports` en HTTPS, numéro E.164, catégorie énumérée, commentaire de 500 caractères maximum, clé d’idempotence aléatoire.
- Validation côté serveur, limites de taille, limite de débit par IP et budget global, preuve anti-abus adaptée et délai entre signalements identiques.
- Enregistrement en attente, modération humaine/automatique contrôlée, aucun ajout direct à une liste publique sur simple déclaration.
- Protection contre faux signalements, seuils corroborés, droit de contestation, exclusion des numéros d’urgence, audit et gestion des suppressions.
- Durée de rétention, minimisation des adresses IP et données, hébergement, finalités et contact de l’éditeur documentés avant activation.
- Publication exclusivement des règles validées dans un flux versionné ; signature cryptographique indépendante à envisager.

Ne pas activer ce service en ajoutant seulement une URL : backend, protections et politique doivent être déployés et testés ensemble.
