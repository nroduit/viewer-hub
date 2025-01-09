# Création du manifeste

Le manifeste contient la liste des examens, séries, instances à récupérer lors du chargement des images par Weasis.

La création du manifeste s'effectue lorsqu'un client fera appel à Weasis Manager pour lancer Weasis par l'intermédiaire de l'url de lancement (utilisant le protocole Weasis: weasis://).

## Manifeste
Le manifeste est un fichier xml contenant les images à télécharger.

Il est représenté sous ce format: https://weasis.org/en/basics/customize/integration/index.html#build-an-xml-manifest


## Construction

Selon les critères de recherche de la requête, Weasis Manager construit le manifeste par l'intermédiaire de connecteurs.

Ces connecteurs sont de type DB ou Dicom (un connecteur de type dicom web sera plus tard implémenté).

Ainsi des requêtes DB ou des appels dicom sont effectués pour récupérer les informations nécessaires pour remplir le manifeste selon les critères de recherche.

Les connecteurs sont définis suivant un modèle dans le config serveur.
