jQuery(function () {
    let type = $("section[type=main]").attr("id")
    $(`section nav ul li:has(a[id!=${type}])`).removeClass("active")
    $(`section nav ul li:has(a[id=${type}])`).addClass("active")
});