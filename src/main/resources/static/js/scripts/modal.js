$(document).ready(function() {
    var successMessage = /*[[${successMessage}]]*/ null;
    var errorMessage = /*[[${errorMessage}]]*/ null;

    if (successMessage && successMessage !== '') {
        $("#successMessage").text(successMessage);
        $("#successModal").modal('show');
    } else if (errorMessage && errorMessage !== '') {
        $("#errorMessage").text(errorMessage);
        $("#errorModal").modal('show');
    }
});

var closeModalButton = document.getElementById('closeModalButton');
if (closeModalButton) {
    closeModalButton.addEventListener('click', function() {
        $('#errorModal').modal('hide');
        $('#successModal').modal('hide');
    });
}
