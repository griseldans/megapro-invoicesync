document.getElementById("printButton").addEventListener("click", function(event) {    
    event.preventDefault();
    generatePdf();
});

function generatePdf(){
    var inv = document.getElementById("invoiceId").value;

    fetch('/api/v1/invoice/'+inv+'/download',{
        method: 'GET'
    })
    .then(response => {
        if (!response.ok){
            throw new Error('Failed to generate PDF');
        }
        return response.blob();
    })
    .then(blob => {
        var url = window.URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = url;
        a.download = 'invoice.pdf';
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
    })
    .catch(error => {
        console.error('Failed to generate PDF:', error);
    });
}