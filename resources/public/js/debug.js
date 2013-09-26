var $button = $("#toggle-request-map"),
    $output = $("#request-map");

function updateVisibility() {
  if (localStorage["show-request-map?"]) {
    $output.css("display", "block");
    $button.addClass("btn-success");
    $button.removeClass("btn-default");
  } else {
    $output.css("display", "none");
    $button.addClass("btn-default");
    $button.removeClass("btn-success");
  }
}

$button.click(function() {
  if (localStorage["show-request-map?"]) {
    delete localStorage["show-request-map?"];
  } else {
    localStorage["show-request-map?"] = true
  }
  updateVisibility();
});

// Run once on pageload
updateVisibility();
