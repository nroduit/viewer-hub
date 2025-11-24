$('#study').live('pagebeforecreate', function() {
  var b = $('<a>')
      .attr('id', 'viewerhub-button')
      .attr('data-role', 'button')
      .attr('href', '#')
      .attr('data-icon', 'search')
      .attr('data-theme', 'e')
      .text('Open in viewer')
      .button();

  b.insertAfter($('#study-info'));

  b.click(function() {
    if ($.mobile.pageData) {
      $.ajax({
        url: '../studies/' + $.mobile.pageData.uuid,
        dataType: 'json',
        cache: false,
        success: function(study) {
          var studyInstanceUid = study.MainDicomTags.StudyInstanceUID;
          var url = 'http://localhost:8081/display?studyUID=' + studyInstanceUid + '&archive=orthanc-local';
          fetch(url);
        }
      });
    }
  });
});

$('#series').live('pagebeforecreate', function() {
  var b = $('<a>')
      .attr('id', 'viewerhub-button')
      .attr('data-role', 'button')
      .attr('href', '#')
      .attr('data-icon', 'search')
      .attr('data-theme', 'e')
      .text('Open in viewer')
      .button();

  b.insertAfter($('#series-info'));

  b.click(function() {
    if ($.mobile.pageData) {
      $.ajax({
        url: '../series/' + $.mobile.pageData.uuid,
        dataType: 'json',
        cache: false,
        success: function(series) {
          $.ajax({
            url: '../studies/' + series.ParentStudy,
            dataType: 'json',
            cache: false,
            success: function(study) {
              var studyInstanceUid = study.MainDicomTags.StudyInstanceUID;
              var seriesInstanceUid = series.MainDicomTags.SeriesInstanceUID;
              var url = 'http://localhost:8081/display?studyUID=' + studyInstanceUid + '&seriesUID=' + seriesInstanceUid + '&archive=orthanc-local';
              fetch(url);
            }
          });
        }
      });
    }
  });
});