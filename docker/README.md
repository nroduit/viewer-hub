# Imaging Hub

## Overview
A Docker Compose stack for managing dcm4chee PACS, Viewer Hub, and all the related services.

Note: This stack is intended for development and testing purposes only. It allows debugging Viewer Hub and testing the integration with dcm4chee PACS. It is not intended for production use.

This stack includes the following services:
- **dcm4chee**: A PACS server that enables storing and retrieving medical images.
- **viewer-hub**: A web application that manages the resources required by the different versions of Weasis.
- **minio**: An object storage server compatible with Amazon S3 APIs. It is used to store resources required by the different versions of Weasis.
- **redis**: A cache server used to store the manifest of the resources required by the different versions of Weasis.
- **postgres**: A database server used by viewer-hub and dcm4chee.
- **keycloak**: An open-source identity and access management server used to authenticate users.
- **config-server**: A server that provides the configuration for the different services.
- **eureka**: A server that provides service discovery for the different services.


## Prerequisites
- Docker (20.10+)
- Docker Compose CLI

## Configurations
This stack supports multiple configurations:

- **Debug (`local`)**: Includes all the required stack except viewer-hub and uses local volumes.
- **Unsecure (`unsecure`)**: Enables HTTP and uses development-grade settings. dcm4chee and viewer-hub services have no authentication.
- **Secure (`secure`)**: Enables HTTPS and uses production-grade settings. dcm4chee and viewer-hub services have authentication.

### Usage
Run the following commands based on the environment:

- **For debugging viewer-hub**:
  ```bash
  ./scripts/start.sh local
  ```
  And then run viewer-hub from your IDE
 

## Minio

Minio is an open-source object storage server compatible with Amazon S3 APIs. It is used to store resources required by the different versions of Weasis.

Access the Minio console at: http://localhost:9090

Use the following credentials:
- User: `viewer-hub`
- Password: `viewer-hub`

## Eureka

Eureka is a centralized service registry used to manage and discover service instances. 
It allows services to register themselves and to discover other services dynamically, facilitating communication and load balancing among services.

Access the Eureka console at: http://localhost:8761/

## Keycloak

Keycloak is an open-source identity and access management server used to authenticate users.

Access the Keycloak console at: http://localhost:8085

Use the following credentials:
- User: `admin`
- Password: `admin`

## Dcm4chee

Dcm4chee is a PACS server that enables storing and retrieving medical images.

Access the dcm4chee console at: http://localhost:8080/dcm4chee-arc/ui2/en/study/study

For secure mode, access the dcm4chee console at: https://localhost:8443/dcm4chee-arc/ui2/en/study/study
Use the following credentials:
- User: `admin`
- Password: `changeit`

## Nexus

Nexus is used to store weasis bundles in order to import them in the viewer-hub server.

Nexus console is accessible at this address: http://localhost:8086/

## Viewer Hub

Viewer Hub is a web application that manages the resources required by the different versions of Weasis.

Access the Viewer Hub console at: http://localhost:8081

Use the following credentials:
- User: `viewer-hub-user`
- Password: `password`

## Launch Weasis from your own web application

The display service is available at the URL: http://localhost:8081/display/weasis

To launch Weasis from your own web application, you need to build the URL with the following parameters:
- `archive`: The archive name to be used to retrieve the study. The list of archives is defined in the config serve
- `patientID`: The Patient ID of the study to be displayed. Note to handle a universal patientID, add IssuerOfPatientID like in hl7: patientID=1168514^^^issuerValue     
   Ex: http://localhost:8081/display/weasis?patientID=1168514&archive=dcm4chee-local     
-  Ex with multiple patientID: http://localhost:8081/display/weasis?patientID=1168514&patientID=2023231696&archive=dcm4chee-local
-  Ex with URL encoding of separators `^^^` and IssuerOfPatientID value `test`: http://localhost:8081/display/weasis?patientID=1168514%5E%5E%5Etest&archive=dcm4chee-local
- `modalitiesInStudy`: Filter the studies containing the specified modalities.   
   Ex with only `CT` or `XA`: http://localhost:8081/display/weasis?patientID=1168514&modalitiesInStudy=CT,XA&archive=dcm4chee-local
- `containsInDescription`: Filter the studies containing the specified string in the study description. Note that the search is case-insensitive and diacritic-insensitive.  
   Ex with only `coronary` or `thorax`: http://localhost:8081/display/weasis?patientID=1168514&containsInDescription=coronary,thorax&archive=dcm4chee-local
- `lowerDateTime`: Filter the studies which are older than the specified date.     
   Ex CT older than 01.01.2010 12:00:00: http://localhost:8081/display/weasis?patientID=1168514&modalitiesInStudy=CT&lowerDateTime=2010-01-01T12:00:00Z&archive=dcm4chee-local
- `upperDateTime`: Filter the studies which are more recent than the specified date.     
   Ex CT more recent than 01.01.2010 12:00:00: http://localhost:8081/display/weasis?patientID=1168514&modalitiesInStudy=CT&upperDateTime=2010-01-01T12:00:00Z&archive=dcm4chee-local
- `studyUID`: The Study Instance UID of the study to be displayed.     
   Ex: http://localhost:8081/display/weasis?studyUID=1.3.12.2.1107.5.1.4.54023.30000004093013443132800000021&archive=dcm4chee-local
- `accessionNumber`: The Accession Number of the study to be displayed.      
   Ex: http://localhost:8081/display/weasis?accessionNumber=2066852&archive=dcm4chee-local
- `seriesUID`: The Series Instance UID of the series to be displayed.     
   Ex: http://localhost:8081/display/weasis?seriesUID=1.3.12.2.1107.5.1.4.54023.30000004093016410718700008612&archive=dcm4chee-local     
-  Ex with multiple studies and series: http://localhost:8081/display/weasis?studyUID=1.3.6.1.4.1.5962.1.2.2.20031208063649.855&studyUID=1.3.6.1.4.1.5962.1.2.2.20031208063649.857&seriesUID=1.2.840.113704.1.111.4924.1273631010.17&archive=dcm4chee-local
- `objectUID`: The SOP Instance UID of the object to be displayed.     
  Ex: http://localhost:8081/display/weasis?objectUID=1.3.12.2.1107.5.1.4.54023.30000004093016410718700010432&archive=dcm4chee-local

Note: it is possible to limit the type of UIDs (patientID, studyUID, accessionNumber, seriesUID, objectUID) that can be called from services. 
See "request.ids" in this configuration file which enables to set which ID is allowed, by default all are allowed.

## Launch Weasis with IHE IID profile

The Invoke Image Display Profile allows the user of an Image Display Invoker, typically a non-image-aware system like an EHR, PHR or RIS, to request the display of studies for a patient, and have the display performed by an image-aware system like an Image Display (PACS).

archive parameter ?

To launch Weasis with IHE IID profile, you need to build the URL with the following parameters:
- `requestType`: The type of the request (PATIENT, STUDY)
- `patientID`: The Patient ID of the study to be displayed. Note to handle a universal patientID, add IssuerOfPatientID like in hl7: patientID=1168514^^^issuerValue
- `mostRecentResults`: The number of the most recent studies to be displayed.      
-  Ex: http://localhost:8081/display/IHEInvokeImageDisplay?requestType=PATIENT&patientID=11685148&mostRecentResults=2
- `modalitiesInStudy`: Filter the studies containing the specified modalities.     
-  Ex studies containing `CT` or `XA`: http://localhost:8081/display/IHEInvokeImageDisplay?requestType=PATIENT&patientID=11685148&modalitiesInStudy=CT,XA
- `containsInDescription`: Filter the studies containing the specified string in the study description. Note that the search is case-insensitive and diacritic-insensitive.     
-  Ex studies containing `coronary` or `thorax`: http://localhost:8081/display/IHEInvokeImageDisplay?requestType=PATIENT&patientID=11685148&containsInDescription=coronary,thorax
- `lowerDateTime`: Filter the studies which are older than the specified date.     
-  Ex studies older than 01.01.2010 12:00:00: http://localhost:8081/display/IHEInvokeImageDisplay?requestType=PATIENT&patientID=11685148&lowerDateTime=2010-01-01T12:00:00
- `upperDateTime`: Filter the studies which are more recent than the specified date.     
-  Ex studies more recent than 01.01.2010 12:00:00: http://localhost:8081/display/IHEInvokeImageDisplay?requestType=PATIENT&patientID=11685148&lowerDateTime=2010-01-01T12:00:00
- `studyUID`: The Study Instance UID of the study to be displayed.
-  Ex: http://localhost:8081/display/IHEInvokeImageDisplay?requestType=STUDY&studyUID=1.3.12.2.1107.5.1.4.54023.30000004093013443132800000021
- `accessionNumber`: The Accession Number of the study to be displayed.     
-  Ex: http://localhost:8081/display/IHEInvokeImageDisplay?requestType=STUDY&accessionNumber=2066852

