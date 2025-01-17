# Compatibilité de versions Weasis 

## Fichier de compatibilité

Un fichier de compatibilité nommé "version-compatibility.json" est présent dans chaque release de Weasis (dans le zip weasis-native.zip)

Ce fichier contient la correspondance entre la version de la release ("release-version") et la version minimale de Weasis installé sur le poste client ("minimal-version").

Ce fichier indique également quelle version de traduction doit être utilisé ("i18n-version").

## Cache

Lors de l'upload d'une nouvelle version dans Viewer Hub ou au démarrage de l'application (dans le cas où le fichier de compatibilité est déjà présent sur le S3), Viewer Hub construira à partir du fichier de compatibilité les différentes combinaisons possibles entre les versions installées dans Viewer Hub et les releases possibles de Weasis.

Ces combinaisons seront stockées dans un cache redis. Ce cache est actuellement rafraichi toutes les 24h.

Ainsi lors du lancement de Weasis par un client, Viewer Hub connaitra directement quelle version, i18n et quelles ressources utiliser pour lancer Weasis sur le poste uilisateur.

## Minio/S3

En important une nouvelle version de Weasis dans le manager, si le fichier de compatibilité est plus récent, Viewer Hub remplacera le fichier de compatibilité présent sur le S3. Ce fichier de compatibilité sera renommé sur S3 "mapping-minimal-version.json"

Pour comparer si ce fichier de mapping est plus récent, Viewer Hub comparera le numéro de version de la dernière release.

