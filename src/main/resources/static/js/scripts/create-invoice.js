// Ambil data tax dari backend
var taxes = [];
function fetchTaxes() {
    fetch('/api/taxes')
        .then(response => response.json())
        .then(data => {
            const taxList = document.getElementById('taxList');
            taxList.innerHTML = '';

            data.forEach(tax => {
                taxes.push(tax);

                const div = document.createElement('div');
                div.classList.add('form-check');
                const input = document.createElement('input');
                input.classList.add('form-check-input');
                input.type = 'checkbox';
                input.id = tax.taxId;
                input.name = 'taxOption';
                input.value = tax.taxId;

                input.setAttribute('th:field', '*{listTax}');

                const label = document.createElement('label');
                label.classList.add('form-check-label');
                label.setAttribute('for', tax.taxId);
                label.innerText = tax.taxName;

                div.appendChild(input);
                div.appendChild(label);
                taxList.appendChild(div);
            });
        })
        .catch(error => {
            console.error('Error fetching taxes:', error);
        });
}
fetchTaxes();

function getSelectedTaxPercentage() {
    var array = []
    var checkboxes = document.querySelectorAll('input[name="taxOption"]:checked')
    checkboxes.forEach(checkbox => {
        const taxId = checkbox.value;
        const selectedTax = taxes.find(tax => tax.taxId === parseInt(taxId));
        if (selectedTax) {
            array.push(selectedTax.taxPercentage)
        }
    });

    return array;
}

function updateSubtotal(priceInput, quantityInput, subtotalInput) {
    var price = parseFloat(priceInput.value);
    var quantity = parseInt(quantityInput.value);
    var total = price * quantity;
    subtotalInput.value = total.toFixed(2);
}

document.getElementById('totalDiscount').addEventListener('input', function() {
    countTaxes();
    updateGrandTotalInvoice();
});

async function countTaxes() {
    // Ambil tax yang dipilih
    var selectedTaxPercentage = getSelectedTaxPercentage();

    // Ambil subtotal dan hitung dengan discountnya
    var subtotal = document.getElementById("subtotal").value;
    var discount = parseFloat(document.getElementById('totalDiscount').value || 0);
    var amount = subtotal - ((discount/100.0)*subtotal);

    // Hitung total taxes (Rp)
    var taxTotal = 0;
    selectedTaxPercentage.forEach(tax => {
        taxTotal += (tax*amount/100)
    })
    document.querySelector('input[name="taxTotal"]').value = taxTotal.toFixed(2);
}


function updateGrandTotalInvoice() {
    var subtotalElement = document.querySelector('input[name="subtotal"]').value;
    var subtotal = parseFloat(subtotalElement || 0);

    var discountElement = document.getElementById('totalDiscount').value
    var discount = parseFloat(discountElement || 0);

    var taxTotalElement = document.querySelector('input[name="taxTotal"]').value;
    var taxTotal = parseFloat(taxTotalElement || 0);

    var total = subtotal - ((discount/100.0)*subtotal) + taxTotal;

    document.querySelector('input[name="grandTotal"]').value = total.toFixed(2);
}



document.getElementById("addRowInvoice").addEventListener("click", function() {
    var tableBody = document.getElementById("invoiceTableBody");
    var rowCount = tableBody.rows.length;

    var newRow = tableBody.insertRow(rowCount);

    var cellNo = newRow.insertCell(0);
    var cellDescription = newRow.insertCell(1);
    var cellQuantity = newRow.insertCell(2);
    var cellPrice = newRow.insertCell(3);
    var cellTotalPrice = newRow.insertCell(4);
    var cellAction = newRow.insertCell(5);

    cellNo.innerHTML = rowCount+1;
    cellDescription.innerHTML = '<input class="form-control" type="text" name="productDescription">';
    cellQuantity.innerHTML = '<input class="form-control quantity" type="number" name="productQuantity" value="1">';
    cellPrice.innerHTML = '<input class="form-control price" type="number" name="productPrice">';
    cellTotalPrice.innerHTML = '<input class="form-control subtotal" type="number" name="productSubtotal" readonly>';
    cellAction.innerHTML = '<button class="btn btn-primary delete-icon mb-2" type="button">Delete</button>' +
                            '<button class="btn btn-primary check-icon" type="button">Save</button>';

    var quantityInput = cellQuantity.querySelector('input');
    var priceInput = cellPrice.querySelector('input');
    var subtotalInput = cellTotalPrice.querySelector('input');

    quantityInput.addEventListener('change', function() {
        updateSubtotal(priceInput, this, subtotalInput);
        countTaxes();
        updateGrandTotalInvoice();
    });

    priceInput.addEventListener('change', function() {
        updateSubtotal(this, quantityInput, subtotalInput);
        countTaxes();
        updateGrandTotalInvoice();
    });

    var deleteIcon = cellAction.querySelector('.delete-icon');
    deleteIcon.addEventListener('click', function() {
        var productId = newRow.getAttribute('data-product-id');
        if (productId !== null){
            deleteProduct(productId, newRow);
        }
        else{
            tableBody.removeChild(newRow);
        }
    });

    var checkIcon = cellAction.querySelector('.check-icon');
    checkIcon.addEventListener('click', function() {
        var inputs = newRow.querySelectorAll('input[type="text"], input[type="number"]');
        inputs.forEach(function(input) {
            input.disabled = true;
        });
        handleCheckClick(this);
    });
});

document.getElementById("productDocument").addEventListener("change", function() {
    var fileInput = document.querySelector('input[name="productDocument"]');
    var formData = new FormData();
    formData.append('productDocument', fileInput.files[0]);

    fetch('/api/v1/create-product-document', {
        method: 'POST',
        body: formData
    })
    .then(function(response) {
        if (!response.ok) {
            throw new Error('Error creating product document: ' + response.status);
        }
        return response.json();
    })
    .then(function(data) {
        updateProductList(data);
        getAllProduct();
    })
    .catch(function(error) {
        var modal = document.getElementById("errorReadProductList");
        modal.classList.add('show');
        modal.setAttribute('aria-hidden', 'false');
        modal.style.display = 'block';
        console.error(error);
    });
});

function updateProductList(listProduct){
    var tableBody = document.getElementById("invoiceTableBody");
    var rowCount = tableBody.rows.length;

    listProduct.forEach(function(product, index) {
        var newRow = tableBody.insertRow();
        newRow.setAttribute('data-product-id', product.productId);

        var count = newRow.insertCell(0);
        count.textContent = rowCount + index + 1;

        var description = newRow.insertCell(1);
        description.textContent = product.description;

        var quantity = newRow.insertCell(2);
        quantity.textContent = product.quantity;

        var price = newRow.insertCell(3);
        price.textContent = product.price;

        var totalPrice = newRow.insertCell(4);
        totalPrice.textContent = product.totalPrice;

        var action = newRow.insertCell(5);
        action.innerHTML = '<button class="btn btn-primary delete-icon mb-2" type="button">Delete</button>'

        var deleteIcon = action.querySelector('.delete-icon');
        deleteIcon.addEventListener('click', function() {
            var productId = newRow.getAttribute('data-product-id');
            if (productId !== null){
                deleteProduct(productId, newRow);
            }
            else{
                tableBody.removeChild(newRow);
            }
        });
    });
}

document.getElementById("taxList").addEventListener("click", function(){
    countTaxes();
    updateGrandTotalInvoice();
});

document.getElementById("invoiceTableBody").addEventListener("change", function(event) {
    var target = event.target;
    if (target.matches('input[name="productQuantity"]') || target.matches('input[name="productPrice"]')) {
        var row = target.closest('tr');
        var quantityInput = row.querySelector('input[name="productQuantity"]');
        var priceInput = row.querySelector('input[name="productPrice"]');
        var subtotalInput = row.querySelector('input[name="productSubtotal"]');
        updateSubtotal(priceInput, quantityInput, subtotalInput);
        countTaxes();
        updateGrandTotalInvoice();
    }
});

function deleteProduct(productId, tableRow) {
    fetch('/api/v1/product/' + productId + '/delete', {
        method: 'POST'
    })
    .then(function(response) {
        if (!response.ok) {
            throw new Error('Error deleting product:', response.status);
        }
        console.log('Product deleted successfully');
        tableRow.parentNode.removeChild(tableRow);
        // updateRowNumbers(tableBody);
        getAllProduct();
    })
    .catch(function(error) {
        console.error(error);
    });
}

function handleCheckClick(checkIcon) {
    var cellAction = checkIcon.parentElement;
    var tableRow = cellAction.parentElement;
    var cellDescription = tableRow.cells[1].querySelector('input[name="productDescription"]');
    var cellQuantity = tableRow.cells[2].querySelector('input[name="productQuantity"]');
    var cellPrice = tableRow.cells[3].querySelector('input[name="productPrice"]');
    var cellTotalPrice = tableRow.cells[4].querySelector('input[name="productSubtotal"]');
    
    var productData = {
        description: cellDescription.value,
        quantity: cellQuantity.value,
        price: cellPrice.value,
        totalPrice: cellTotalPrice.value
    };

    fetch('/api/v1/create-product', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(productData)
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Failed to create product');
        }
    })
    .then(data => {
        checkIcon.style.display = 'none';
        console.log(data.productId);
        tableRow.setAttribute('data-product-id', data.productId);
        getAllProduct();
    })
    .catch(error => {
        console.error('Error creating product:', error);
    });
}


function updateCustomerContact() {
    var selectedCustomerName = document.getElementById("customerName").value;
    var selectedOption = document.querySelector('option[value="' + selectedCustomerName + '"]');
    if (selectedOption) {
        var selectedCustomerContact = selectedOption.getAttribute('data-contact');
        var selectedCustomerAddress = selectedOption.getAttribute('data-address');
        document.getElementById("customerContact").value = selectedCustomerContact;
        document.getElementById("customerAddress").value = selectedCustomerAddress;
    } else {
        document.getElementById("customerContact").value = "";
        document.getElementById("customerAddress").value = "";
    }
}

var closeModalButton = document.getElementById('closeModalButton');
closeModalButton.addEventListener('click', function() {
    $('#errorModal').modal('hide');
    $('#successModal').modal('hide');
});

function getAllProduct() {
    var invoiceId = document.getElementById("invoiceDummyId").innerText;
    console.log("invoice id: " + invoiceId);
    
    fetch('/api/v1/invoice/product/' + invoiceId, {
        method: 'GET'
    })
    .then(function(response) {
        if (!response.ok) {
            throw new Error('Error fetching products: ' + response.status);
        }
        return response.json();
    })
    .then(function(products) {
        var subtotal = 0;
        products.forEach(function(product) {
            subtotal += parseFloat(product.totalPrice);
        });
        document.querySelector('input[name="subtotal"]').value = subtotal.toFixed(2);
        countTaxes();
        updateGrandTotalInvoice();
    })
    .catch(function(error) {
        console.error('Error:', error);
    });
}


function showGuidelinesModal() {
    var modal = document.getElementById("guidelinesModal");
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
    modal.style.display = 'block';
}

function hideGuidelinesModal() {
    var modal = document.getElementById("guidelinesModal");
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
    modal.style.display = 'none';
}

function hideErrorReadProductList() {
    var modal = document.getElementById("errorReadProductList");
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
    modal.style.display = 'none';
}