# Gestion des versions de Weasis 

## Import manuelle d'une version de Weasis

Une version de Weasis peut être importée manuellement dans Weasis Manager dans l'onglet "Package"

Le fichier a importer doit avoir un nom de ce format "weasis-native xxx.zip".

Ce fichier correspond à une des versions de releases Weasis produites ici: https://github.com/nroduit/Weasis/releases/ 

### Processus d'import

- Récupération du fichier "weasis-native xxx.zip", décompression puis stockage des ressources/bundles de la version dans minio/S3.
-  Compression du dossier "ressources" de la version en fichier zip (nécessaire pour Weasis) et stockage sur S3.
-  Mise à jour sur S3 du fichier de compatibilité des versions de Weasis si la version importée est plus récente.
- Mise à jour du cache concernant le mapping de compatibilité des versions de Weasis
-  Chargement des propriétés de la version en base de donnée.

## Suppression des versions de Weasis

Pour supprimer une version de Weasis, il est nécessaire de sélectionner la version à supprimer, puis effectuer un click droit et de valider la suppression.

La suppression d'une version dont le "launch config" est "default" entrainera la suppression de toutes les versions liées à ce "default".
