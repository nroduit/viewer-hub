window.config = {
  routerBasename: null,
  debug: true,
  extensions: [],
  modes: [],
  showStudyList: true,
  dataSources: [
    {
      namespace: '@ohif/extension-default.dataSourcesModule.dicomweb',
      sourceName: 'dcm4chee-local',
      configuration: {
        friendlyName: 'dcm4chee-local',
        name: 'dcm4chee-local',
        wadoUriRoot: 'http://localhost:8088/DCM4CHEE-LOCAL/wado',
        qidoRoot: 'http://localhost:8088/DCM4CHEE-LOCAL/rs',
        wadoRoot: 'http://localhost:8088/DCM4CHEE-LOCAL/rs',
        stowRoot: 'http://localhost:8088/DCM4CHEE-LOCAL/rs',
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
    {
      namespace: '@ohif/extension-default.dataSourcesModule.dicomweb',
      sourceName: 'orthanc-local',
      configuration: {
        friendlyName: 'orthanc-local',
        name: 'orthanc-local',
        wadoUriRoot: 'http://localhost:8088/ORTHANC-LOCAL/wado',
        qidoRoot: 'http://localhost:8088/ORTHANC-LOCAL/dicomweb',
        wadoRoot: 'http://localhost:8088/ORTHANC-LOCAL/dicomweb',
        stowRoot: 'http://localhost:8088/ORTHANC-LOCAL/dicomweb',
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
  defaultDataSourceName: 'dcm4chee-local'
};