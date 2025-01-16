# Cryptographie 

Dans viewer-hub pour lancer la visionneuse Weasis, une url contenant des critères de recherche dans des "query parameters" est utilisée.

Il est possible d'encrypter ces paramètres de recherche lors du lancement de la visionneuse Weasis afin de ne pas transmettre en clair certaines données sensibles (ex: identifiant du patient).

Pour activer cette fonctionnalité, le fichier application-cryptography.yml dans le config-server doit être modifié en mettant le champ "enabled" ci-dessous à true.

La définition d'un password et d'un salt est également nécessaire pour encoder/décoder les paramètres de recherche. 

```yaml
# - Cryptography
cryptography:
  enabled: true
  password: 
  salt: 
```

Le même algorithme de chiffrement doit être utilisé côté client (chiffrage) et côté viewer-hub(déchiffrage).