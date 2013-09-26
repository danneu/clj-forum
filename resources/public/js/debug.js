var $button = $("#toggle-request-map"),
    $output = $("#request-map");

function updateVisibility() {
  if (localStorage["show-request-map?"]) {
    $output.css("display", "block");
  } else {
    $output.css("display", "none");
  }
}

$button.click(function() {
  if (localStorage["show-request-map?"]) {
    // Hiding it
    $button.addClass("btn-default");
    $button.removeClass("btn-success");
    delete localStorage["show-request-map?"];
  } else {
    // Displaying it
    localStorage["show-request-map?"] = true
    $button.addClass("btn-success");
    $button.removeClass("btn-default");
  }
  updateVisibility();
});

// Run once on pageload
updateVisibility();
