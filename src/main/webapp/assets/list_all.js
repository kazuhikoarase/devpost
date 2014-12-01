/**
 * devpost - list_all.js
 */

$(function() {
  $('.msg-subject').each(function() {
    $(this).attr('title', $(this).text() );
  });
  $('.rcptTo').click(function(event) {
    location.href = '?rcptTo=' + encodeURIComponent($(this).attr('rcptTo') );
  });
  $('.rcptTo').on('mouseover', function(event) {
    $(this).find('TD').addClass('current-row');
  });
  $('.rcptTo').on('mouseout', function(event) {
    $(this).find('TD').removeClass('current-row');
  });
});
