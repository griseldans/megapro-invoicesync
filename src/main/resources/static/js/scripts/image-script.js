const dropArea = document.querySelector(".drag-area"),
dragText = dropArea.querySelector("header"),
buttonArea = dropArea.querySelector("button"),
input = dropArea.querySelector("input");
let file;

buttonArea.onclick = () =>{
    input.onclick();
}

input.addEventListener("change", function(){
    file = this.files[0];
    dropArea.classList.add("active");
    showFile();
});

dropArea.addEventListener("dragover", (event)=>{
    event.preventDefault();
    dropArea.classList.add("active");
    dragText.textContent = "Release to Upload File";
});

dropArea.addEventListener("dragleave", ()=>{
    dropArea.classList.remove("active");
    dragText.textContent = "Drag & Drop to Upload File";
});

dropArea.addEventListener("drop", (event)=>{
    event.preventDefault();
    file = event.dataTransfer.files[0];
    showFile();
});

function showFile(){
    let fileType = file.type;
    let validExtensions = ["image/jpeg", "image/jpg", "image/png"];
    if (validExtensions.includes(fileType)){
        let fileReader = new FileReader();
        fileReader.onload = ()=>{
            let fileUrl = fileReader.result;
            let imgTag = `<img src="${fileUrl}" alt="image">`;
            dropArea.innerHTML = imgTag;
            document.querySelector('input[name="image"]').value = fileUrl;
        }
        fileReader.readAsDataURL(file);
    }else{
        alert("This is not an image file!");
        dropArea.classList.remove("active");
        dragText.textContent = "Drag & Drop to Upload File"
    }
}