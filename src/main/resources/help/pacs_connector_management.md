# Connecteurs 

Afin de pouvoir se connecter aux différents PACS pour la création du manifest, 3 types de connecteurs sont définis dans le config server: DB, dicom, dicom web.

Ces connecteurs sont définis selon ce modèle:

```yaml
# - Connectors
connector:
    # If value is present: use the connectors specified, if not present or wrong connector ids: use all the connectors
    # defined in the config
    default: # connectorId1, connectorId2
  
    # Connectors configuration
    config:
        connecteur-id:
            type: # DB,DICOM,DICOM WEB
            # ------- Search Criteria ----
            search-criteria:
                deactivated: # SOP_INSTANCE_UID, SERIE_INSTANCE_UID, STUDY_INSTANCE_UID, STUDY_ACCESSION_NUMBER, PATIENT_ID
            # ---------- Wado -------------
            # Used to retrieve images from manifest
            wado:
                authentication:
                    force-basic: # Used to force usage of basic authentication parameters to retrieve images (even if request is authenticated) # true/false
                    oauth2: # If request is authenticated: retrieve the token from the authenticated request and inject it in the manifest for Weasis to get the images
                        url: # Url used to retrieve images by adding authenticated request token in manifest
                    basic: # Retrieve the images by using this basic authentication parameters
                        url: # Url used to retrieve images by using basic authentication
                        # Basic credentials
                        login:
                        password:
                    transfer-syntax-uid:
                    compression-rate:
                    requireOnlySOPInstanceUID: # true/false
                    additionnalParameters:
                    overrideDicomTags:
                    httpTags:
                        X-Time:
                        X-Value:
            # -------- For database --------
            db-connector:
                user:
                password:
                uri:
                driver:
                query:
                    select:
                    accession-number-column:
                    patient-id-column:
                    study-instance-uid-column:
                    serie-instance-uid-column:
                    sop-instance-uid-column:
            # -------- For dicom ----------
            dicom-connector:
                calling-aet:
                aet:
                host:
                port:
                tls:
                    mode: # true/false
                    need-client-authentication: # true/false
                key-store:
                    url:
                    type:
                    password:
                    key-store-password:
                truststore:
                    url:
                    type:
                    password:
```

## DB

Ce connecteur est utilisé pour pouvoir se connecter à la base de donnée du pacs afin de retrouver les examens, séries, instances des images pour la construction du manifeste.

```yaml
db-connector:
    user: # database user
    password: # encoded password
    uri: # database uri
    driver: # database driver
    query:
        select: # SQL query to retrieve patientName, patientId, patientBirthDate, patientSex, studyInstanceUid, studyDate, accessionNumber, studyId, referringPhysicianName, studyDescription,
                # seriesInstanceUid, modality, seriesDescription, seriesNumber, sopInstanceUid, instanceNumber
        accession-number-column: # accession number column used in the SQL query above
        patient-id-column: # patient id column used in the SQL query above
        study-instance-uid-column: # study instance uid column used in the SQL query above
        serie-instance-uid-column: # serie instance uid column used in the SQL query above
        sop-instance-uid-column: # sop instance uid column used in the SQL query above
```

## Dicom

Ce connecteur est utilisé pour pouvoir se connecter au pacs en dicom afin de retrouver les examens, séries, instances des images pour la construction du manifeste.

```yaml
dicom-connector:
    calling-aet: # calling aet
    aet: # aet
    host: # host
    port: # port
```