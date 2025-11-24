window.config = {
  routerBasename: null,
  extensions: [],
  modes: [],
  showStudyList: true,
  dataSources: [
    {
      namespace: '@ohif/extension-default.dataSourcesModule.dicomweb',
      sourceName: 'orthanc-local',
      configuration: {
        friendlyName: 'Orthanc',
        acceptHeader: [ 'application/dicom+json'],
        name: 'orthanc-local',
        wadoUriRoot: 'http://localhost:8081/dicomweb/orthanc-local',
        qidoRoot: 'http://localhost:8081/dicomweb/orthanc-local',
        wadoRoot: 'http://localhost:8081/dicomweb/orthanc-local',
        qidoSupportsIncludeField: true,
        supportsReject: true,
        imageRendering: 'wadors',
        thumbnailRendering: 'wadors',
        enableStudyLazyLoad: true,
        supportsFuzzyMatching: true,
        supportsWildcard: true,
        omitQuotationForMultipartRequest: false,
      }
    },
    {
      namespace: '@ohif/extension-default.dataSourcesModule.dicomweb',
      sourceName: 'dcm4chee-local',
      configuration: {
        friendlyName: 'Dcm4chee',
        name: 'dcm4chee-local',
        wadoUriRoot: 'http://localhost:8081/dicomweb/dcm4chee-local',
        qidoRoot: 'http://localhost:8081/dicomweb/dcm4chee-local',
        wadoRoot: 'http://localhost:8081/dicomweb/dcm4chee-local',
        qidoSupportsIncludeField: true,
        supportsReject: true,
        imageRendering: 'wadors',
        thumbnailRendering: 'wadors',
        enableStudyLazyLoad: true,
        supportsFuzzyMatching: true,
        supportsWildcard: true,
        omitQuotationForMultipartRequest: true,
      }
    },
  ],
  defaultDataSourceName: 'orthanc'
};