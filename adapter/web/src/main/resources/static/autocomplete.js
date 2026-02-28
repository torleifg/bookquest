document.addEventListener("DOMContentLoaded", function () {
    const input = document.getElementById("query");
    if (!input) return;

    let list = null;
    let activeIndex = -1;
    let items = [];
    let debounceTimer = null;

    function createList() {
        list = document.createElement("ul");
        list.className = "autocomplete-list";
        list.setAttribute("role", "listbox");

        input.parentNode.appendChild(list);
    }

    function removeList() {
        if (list) {
            list.remove();
            list = null;
        }
        activeIndex = -1;
        items = [];
    }

    function setActive(index) {
        const entries = list.querySelectorAll(".autocomplete-item");
        entries.forEach(function (el) { el.classList.remove("active"); });
        activeIndex = index;
        if (index >= 0 && index < entries.length) {
            entries[index].classList.add("active");
            entries[index].scrollIntoView({ block: "nearest" });
        }
    }

    function selectItem(value) {
        input.value = value;
        removeList();
    }

    function render(data) {
        if (!list) {
            createList();
        } else {
            list.innerHTML = "";
        }
        activeIndex = -1;
        items = [];
        let currentCategory = "";
        let itemIndex = 0;

        data.forEach(function (entry) {
            if (entry.type !== currentCategory) {
                const categoryLabel = entry.type === "contributor"
                    ? contributorLabel
                    : titleLabel;

                const categoryLi = document.createElement("li");
                categoryLi.className = "autocomplete-category";
                categoryLi.setAttribute("role", "presentation");
                categoryLi.textContent = categoryLabel;
                list.appendChild(categoryLi);
                currentCategory = entry.type;
            }

            const li = document.createElement("li");
            li.className = "autocomplete-item";
            li.setAttribute("role", "option");
            li.setAttribute("aria-label", currentCategory + " : " + entry.value);
            li.textContent = entry.value;

            const idx = itemIndex;
            li.addEventListener("mousedown", function (e) {
                e.preventDefault();
                selectItem(entry.value);
            });
            li.addEventListener("mouseenter", function () {
                setActive(idx);
            });

            list.appendChild(li);
            items.push(entry);
            itemIndex++;
        });
    }

    function fetchSuggestions(term) {
        fetch("/autocomplete?term=" + encodeURIComponent(term))
            .then(function (response) { return response.json(); })
            .then(function (data) {
                if (data.length > 0 && input === document.activeElement) {
                    render(data);
                } else {
                    removeList();
                }
            })
            .catch(function () {
                removeList();
            });
    }

    input.addEventListener("input", function () {
        const term = input.value.trim();
        clearTimeout(debounceTimer);

        if (term.length < 3) {
            removeList();
            return;
        }

        debounceTimer = setTimeout(function () {
            fetchSuggestions(term);
        }, 200);
    });

    input.addEventListener("keydown", function (e) {
        if (!list) return;

        const entries = list.querySelectorAll(".autocomplete-item");

        if (e.key === "ArrowDown") {
            e.preventDefault();
            setActive(activeIndex < entries.length - 1 ? activeIndex + 1 : 0);
        } else if (e.key === "ArrowUp") {
            e.preventDefault();
            setActive(activeIndex > 0 ? activeIndex - 1 : entries.length - 1);
        } else if (e.key === "Enter" && activeIndex >= 0) {
            e.preventDefault();
            selectItem(items[activeIndex].value);
        } else if (e.key === "Escape") {
            removeList();
        }
    });

    input.addEventListener("blur", function () {
        setTimeout(removeList, 150);
    });
});
