# Flux de règles maintenu par SpamOff

`rules.json` : schéma 1, version entière croissante, source, tableaux `exact` E.164 et `prefixes` internationaux. `manifest.json` : schéma 1, même version, SHA-256 des **octets exacts** de rules.json.

```sh
python3 scripts/update_manifest.py
```

Après modification, ce script recalcule le manifeste et synchronise les données embarquées. Incrémenter la version de rules.json avant publication. Vérifier la source réglementaire, tester et faire relire tout nouveau préfixe. Ne jamais ajouter un numéro particulier sur la seule base d’un signalement non modéré.

Le client télécharge les fichiers depuis `https://raw.githubusercontent.com/cortexAI-2025/SpamOff/main/data/`. Les deux fichiers doivent être publiés dans le même commit. Une incohérence temporaire de cache provoque un refus et conserve la base précédente. Le même schéma permet ultérieurement des listes exactes sous licence compatible, sans prétendre qu’elles existent déjà.
