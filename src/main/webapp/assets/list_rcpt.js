/**
 * devpost - list_rcpt.js
 */

$(function() {

  $('.msg-subject').each(function() {
    $(this).attr('title', $(this).text() );
  });

  var createSVGElement = function(tagName) {
    return $(document.createElementNS(
        'http://www.w3.org/2000/svg', tagName) );
  };

  var createSVG = function(w, h) {
    return createSVGElement('svg').attr({
      version: '1.1',
      width: w, height: h,
      viewBox: '0 0 ' + w + ' ' + h
    });
  };

  var createBtn = function() {
    var s = 13;
    var g = 2;
    var checked = false;
    var incomplete = false;
    var $btn = createSVG(s, s).attr('class', 'checkbox');
    $btn.append(createSVGElement('rect').
      attr({x:0,y:0,width:s,height:s}) );
    $btn.append(createSVGElement('path').
      attr('class', 'checkbox-sign').
      attr('d', 'M ' + g + ' ' + g + ' L ' + (s - g) + ' ' + (s - g) +
       ' M ' + (s - g) + ' ' + g + ' L ' + g + ' ' + (s - g) ) );
    $btn.on('mousedown', function(event) {
      checked = !checked;
      incomplete = false;
      updateUI();
    } );
    var updateUI = function() {
      if (checked) {
        $btn.attr('class', 'checkbox-checked');
      } else if (incomplete) {
        $btn.attr('class', 'checkbox-incomplete');
      } else {
        $btn.attr('class', 'checkbox');
      }
    };
    var isChecked = function() {
      return checked;
    };
    var setIncomplete = function(value) {
      incomplete = value;
      updateUI();
    };
    var setChecked = function(value) {
      checked = value;
      updateUI();
    };
    $btn.data('controller', {
      isChecked: isChecked,
      setChecked: setChecked,
      setIncomplete: setIncomplete
    });
    return $btn;
  };

  var $allBtn = createBtn();
  var $buttons = [];
  var allBtn = $allBtn.data('controller');
  $($('.header').children('TH')[0]).
    css('text-align', 'center').append($allBtn);
  $allBtn.on('mousedown', function(event) {
    event.preventDefault();
    event.stopPropagation();
    $.each($buttons, function(i, $btn) {
      var btn = $btn.data('controller');
      btn.setChecked(allBtn.isChecked() );
    });
    updateDeleteButton();
  });

  var getCheckCount = function() {
    var cnt = 0;
    $.each($buttons, function(i, $btn) {
      var btn = $btn.data('controller');
      if (btn.isChecked() ) {
        cnt += 1;
      }
    });
    return cnt;
  };

  $('.msg').on('mouseover', function(event) {
    $(this).find('TD').addClass('current-row');
  }).on('mouseout', function(event) {
    $(this).find('TD').removeClass('current-row');
  }).each(function() {
    var $btn = createBtn();
    var btn = $btn.data('controller');
    btn.msgId = $(this).attr('msgId');
    $($(this).children('TD')[0]).
      css('text-align', 'center').append($btn);
    $buttons.push($btn);
    $btn.on('mousedown', function(event) {
      event.preventDefault();
      event.stopPropagation();
      var cnt = getCheckCount();
      if (cnt == 0) {
        allBtn.setIncomplete(false);
        allBtn.setChecked(false);
      } else if (cnt < $buttons.length) {
        allBtn.setIncomplete(true);
        allBtn.setChecked(false);
      } else {
        allBtn.setIncomplete(false);
        allBtn.setChecked(true);
      }
      updateDeleteButton();
    });
  });

  $('.viewMsg').on('click', function(event) {
      location.href = 'mbox/view?msgId=' +
        encodeURIComponent($(this).closest('TR').attr('msgId') );
  });
  $('.downloadMsg').on('click', function(event) {
      location.href = 'mbox/get?msgId=' +
        encodeURIComponent($(this).closest('TR').attr('msgId') );
  });

  var updateDeleteButton = function() {
    $('#deleteMsg').prop('disabled', getCheckCount() == 0);
  };

  $('#deleteMsg').click(function() {
    var msgIdList = [];
    $.each($buttons, function(i, $btn) {
      var btn = $btn.data('controller');
      if (btn.isChecked() ) {
        msgIdList.push(btn.msgId);
      }
    });
    $.ajax({
      type: 'POST',
      url: 'mbox/delete',
      data: {msgId: msgIdList}
    }).done(function(data) {
      location.reload();
    });
  });

  updateDeleteButton();

});
