/**
 * Admin Layout JS 
 */
document.addEventListener("DOMContentLoaded", function () {

    console.log("SpendWise AdminLTE layout loaded.");

});

document.addEventListener("DOMContentLoaded", function () {

    const userDropdown = document.getElementById("userDropdown");

    if (userDropdown) {
        userDropdown.addEventListener("click", function (event) {
            event.preventDefault();

            const dropdown =
                bootstrap.Dropdown.getOrCreateInstance(userDropdown);

            dropdown.toggle();
        });
    }

});
