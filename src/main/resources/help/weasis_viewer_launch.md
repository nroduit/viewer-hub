# Lancement de la visionneuse

Les différents clients de viewer-hub (actuellement Compacs, Xplore, les pacs dcm4chee) souhaitant lancer la visionneuse Weasis peuvent le faire de plusieurs manières.

Profile IHE IID: https://www.ihe.net/uploadedFiles/Documents/Radiology/IHE_RAD_Suppl_IID.pdf

## Non authentifié
Le lancement non authentifié est actuellement utilisé par Compacs, Xplore de manière transitoire en attendant la migration des différentes applications pour effectuer des appels sécurisés.

### Lancement standard
Url de lancement: {{viewer-hub base url }/display/weasis

### Lancement en utilisant le profil IHE IID
Url de lancement: {{viewer-hub base url }/display/IHEInvokeImageDisplay



## Authentifié
Le lancement de manière authentifié est utilisé actuellement par les pacs dcm4chee, notamment pour transmettre le token oauth2 à Weasis (par l'intermédiaire du manifest) qui lui permettra des récupérer les examens demandés sur les pacs.

### Lancement standard
Url de lancement: {{viewer-hub base url }/display/auth/weasis

### Lancement en utilisant le profil IHE IID
Url de lancement: {{viewer-hub base url }/display/auth/IHEInvokeImageDisplay



## Critères de recherche
### Critères de recherche communs
|Clé|	Description|
|:-----------------------------------------------------------------------------------------------------:|----------------------------------------|
|archive|	Id du connecteur de pacs à utiliser pour effectuer la recherche (liste possible)|


### Critères de recherche standard
|Clé	|Description|
|:-----------------------------------------------------------------------------------------------------:|----------------------------------------|
|patientID|	Identifiant du patient (liste possible)|
|studyUID	|Identifiant unique de l'examen (liste possible)|
|accessionNumber|	Accession number (liste possible)|
|seriesUID|	Identifiant unique de la série (liste possible)|
|objectUID	|Identifiant unique de l'instance de l'image (liste possible)|


### Critères de recherche IHE IID
La recherche par IHE IID est valide:
- si le type de recherche est par patient alors le patient id doit être renseigné
- si le type de recherche est par examen alors le studyUID ou l'accession number doit être renseigné

|Clé|	Description|
|:-----------------------------------------------------------------------------------------------------:|----------------------------------------|
|requestType|	Type de requête IHE IID. Valeur soit "PATIENT", soit "STUDY" |
|patientID|	Identifiant du patient. Obligatoire si requestType "PATIENT" |
|studyUID|	Identifiant unique de l'examen (liste possible). Obligatoire si requestType "STUDY" et accessionNumber non renseigné |
|accessionNumber|	Accession number (liste possible). Obligatoire si requestType "STUDY" et studyUID non renseigné |


## Filtres de recherche sur les patients trouvés


|Clé |	Description |
|:-----------------------------------------------------------------------------------------------------:|----------------------------------------|
|lowerDateTime|	Filtre en ne gardant que les examens des patients dont la date d'examen est supérieure à "lowerDateTime" |
|upperDateTime|	Filtre en ne gardant que les examens des patients dont la date d'examen est inférieure à "upperDateTime" |
|mostRecentResults|	Filtre en ne gardant que les dates d'examens les plus récentes |
|modalitiesInStudy|	Filtre en ne gardant que les examens dont les modalités des séries sont indiquées dans "modalitiesInStudy"|
|containsInDescription|	Filtrage selon la description de l'examen |


## Transmission de query parameters modifiant le lancement de Weasis


|Clé	| Description |
|:-----------------------------------------------------------------------------------------------------:|----------------------------------------|
|pro |	Modification des paramètres de lancement. Voir: https://weasis.org/en/getting-started/weasis-protocol/index.html |
|user |	Utilisateur ayant lancé Weasis. Valeur fournie par le client. Sert à connaître à quel groupe appartient le user afin d'avoir des configurations spécifiques de Weasis. |
|host |	Host ayant lancé Weasis. Valeur fournie par le client. Sert à connaître à quel groupe appartient le host afin d'avoir des configurations spécifiques de Weasis |
|client |	Utilisé uniquement pour le monitoring pour identifier quel client/application à lancé l'affichage de Weasis. Valeur fournie par le client. |
|extCfg | [deprecated]	Prochainement désengagé au profit du query param "config". Même comportement que le query param "config" ci-dessous. |
|config |	Sert à préciser le mode de lancement de Weasis: dicomizer, clipboard_form, etc.. |
|arg |	Modification des paramètres de lancement. Voir: https://weasis.org/en/getting-started/weasis-protocol/index.html |


