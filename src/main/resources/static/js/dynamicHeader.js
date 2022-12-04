jQuery(function () {
    let type = $("section[type=main]").attr("id")
    $(`section nav ul li:has(a[id!=${type}])`).removeClass("active")
    $(`section nav ul li:has(a[id=${type}])`).addClass("active")
});

function addEnv(e) {
    e.preventDefault();
    $("#addEnv").before("<tr><td><label>Название</label>"
        + "<input type=\"text\" name=\"envNames\"></td>"
        + "<td><label>Значение</label>"
        + "<input type=\"password\" name=\"envValues\"></td></tr>"
        + "<td><label>Удалить</label><button type=\"button\" class=\"btn btn-warning\" onclick=\"removeEnv(event)\">Удалить</button></td>\n");
}

function removeEnv(e) {
    e.preventDefault();
    e.target.parentElement.parentElement.remove();
}
