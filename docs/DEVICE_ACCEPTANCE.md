# Recette matérielle obligatoire — Samsung Galaxy A14

Statut initial : **non exécutée**. À remplir uniquement après observations réelles.

Noter modèle exact, version Android/One UI, opérateur, double SIM, version APK et SHA-256. Le numéro du second téléphone ne doit pas être dans les contacts ; ne pas utiliser un numéro d’urgence ni un service surtaxé.

| Cas | Procédure | Résultat attendu | Résultat réel |
|---|---|---|---|
| Installation Android 10 à 15 | APK sur appareil ou émulateur API 29–35 | Installation et ouverture | À exécuter / voir CI |
| Attribution du rôle | Accueil > Autoriser dans Android | SpamOff sélectionnable et statut actualisé | Non exécuté matériel |
| Rôle refusé | Annuler le dialogue | Pas de crash, statut inactif | Non exécuté matériel |
| Appel autorisé | Ajouter le second téléphone en liste blanche | Sonnerie normale | Non exécuté matériel |
| Appel bloqué | Remplacer par liste noire et rappeler | Rejet par Android, compteur +1 si réponse transmise | Non exécuté matériel |
| Désactivation | Désactiver puis rappeler le numéro bloqué | Sonnerie normale | Non exécuté matériel |
| Hors connexion | Couper Wi-Fi et données, conserver la voix et rappeler | Même décision locale | Non exécuté matériel |
| Base ARCEP | Option activée et test contrôlé d’un numéro de la plage | Blocage des appels remis par Android | Non exécuté matériel |
| Mise à jour | Actualiser avec Internet | Date actualisée, données personnelles conservées | Non exécuté matériel |
| Fichier corrompu/incomplet | Tests automatisés dédiés | Refus, dernière base conservée | Voir rapport |
| Redémarrage | Redémarrer, déverrouiller puis rappeler | Rôle et règles conservés | Non exécuté matériel |
| Application arrêtée par Android | Retirer des récents puis rappeler | Service sollicité à la demande | Non exécuté matériel |
| Mise à jour désactivée | Désactiver et relancer | Planification annulée | Non exécuté matériel |
| Appels masqués/contacts | Appels de contrôle | Comportement Android documenté, aucune promesse de filtrage | Non exécuté matériel |
| Double SIM si applicable | Répéter pour chaque SIM | Décisions cohérentes | Non exécuté matériel |

Les appels GSM simulés sur émulateur ne remplacent pas le test du modem, de l’opérateur et de One UI. Un arrêt forcé explicite peut empêcher les tâches Android jusqu’à réouverture ; ne pas annoncer une protection garantie dans cet état.
