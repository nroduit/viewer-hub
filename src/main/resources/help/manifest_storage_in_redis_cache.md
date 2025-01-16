# Cache redis: stockage des manifestes 

Une fois le manifeste construit, celui-ci sera stocké dans un cache redis durant une période définie (TTL actuellement de 3 minutes).

Ce mécanisme permet d'avoir des instances multiples du Viewer Hub, la récupération du manifeste par Weasis étant asynchrone.

Il est également utile lorsque l'utilisateur redemande la visualisation du même examen durant la période du TTL: le manifeste sera récupéré directement du cache redis.

La clé pour la récupération du manifeste correspond au hash des critères de recherche.