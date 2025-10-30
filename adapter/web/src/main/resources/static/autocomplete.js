$(function () {
    $("#query").autocomplete({
        source: "/autocomplete",
        minLength: 3
    }).autocomplete("instance")._renderMenu = function (ul, items) {
        var that = this, currentCategory = "";

        $.each(items, function (index, item) {
            var li;

            if (item.type !== currentCategory) {
                let category = item.type === 'contributor'
                    ? contributor
                    : title;

                ul.append("<li class='ui-autocomplete-category'>" + category + "</li>");
                currentCategory = item.type;
            }

            li = that._renderItemData(ul, $.extend({}, item, {label: item.value}));

            if (item.type) {
                li.attr("aria-label", currentCategory + " : " + item.value);
            }
        });
    };
});
