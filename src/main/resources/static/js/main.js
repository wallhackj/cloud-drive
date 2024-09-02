let before = document.getElementById("before");
const command = document.getElementById("typer");
const textarea = document.getElementById("texter");
const terminal = document.getElementById("terminal");

let git = 0;
let pw = false;

const commands = [];
let searched = [];


setTimeout(function() {
    loopLines(banner, "", 80);
    textarea.focus();
}, 100);


window.addEventListener("keyup", enterKey);

//init
textarea.value = "";
command.innerHTML = textarea.value;

function enterKey(e) {
    if (e.keyCode === 181) {
        document.location.reload();
    }
        if (e.keyCode === 13) {
            commands.push(command.innerHTML);
            git = commands.length;
            addLine("user@terminal.com:~$ " + command.innerHTML, "no-animation", 0);
            commander(command.innerHTML).then(r => {
                console.log(r);
            });
            command.innerHTML = "";
            textarea.value = "";
        }
        if (e.keyCode === 38 && git !== 0) {
            git -= 1;
            textarea.value = commands[git];
            command.innerHTML = textarea.value;
        }
        if (e.keyCode === 40 && git !== commands.length) {
            git += 1;
            if (commands[git] === undefined) {
                textarea.value = "";
            } else {
                textarea.value = commands[git];
            }
            command.innerHTML = textarea.value;
        }
}

function splitArguments(args) {
    // Ensure the input is a string
    if (typeof args !== 'string') {
        throw new TypeError('Expected a string as input');
    }

    const firstPeriodIndex = args.indexOf('.');

    if (firstPeriodIndex === -1) {
        return [args.trim(), ''];
    }
    const nextSpaceIndex = args.indexOf(' ', firstPeriodIndex + 1);
    const firstPart = nextSpaceIndex === -1 ? args.trim() : args.slice(0, nextSpaceIndex).trim();
    const remainingPart = nextSpaceIndex === -1 ? '' : args.slice(nextSpaceIndex + 1).trim();

    return [firstPart, remainingPart];
}


async function commander(cmd) {
    const word = cmd.split(' ');
    const command = word[0];
    const args = word.slice(1).join(' ');
    const mvArgs = splitArguments(args);

    switch (command) {
        case "mv":
            const firstArg = mvArgs[0];
            const secondArg = mvArgs[1];
            setTimeout(() => handlerRenMov(firstArg, secondArg), 80);
            break;
        case "rm":
            setTimeout(() =>handlerRemoveing(args), 80);
            break;
        case "wget":
            setTimeout(() =>handlerDownload(args), 80);
            break;
        case "uploadfolder":
            setTimeout(handlerUploadFolder, 80);
            break;
        case "find":
            await updateSearchFile(args.join(' '));
            loopLines(searched, "color2 margin", 80)
            break;
        case "uploadfile":
            setTimeout(handlerUploadFile, 80);
            break;
        case "ls":
            loopLines(list, "color2 margin", 80)
            break;
        case "help":
            loopLines(help, "color2 margin", 80);
            break;
        case "whoami":
            loopLines(whoami, "color2 margin", 80);
            break;
        case "sudo":
            addLine("Oh no, you're not admin...", "color2", 80);
            setTimeout(function () {
                window.open('https://www.youtube.com/watch?v=dQw4w9WgXcQ');
            }, 1000);
            break;
        case "social":
            loopLines(social, "color2 margin", 80);
            break;
        case "history":
            addLine("<br>", "", 0);
            loopLines(commands, "color2", 80);
            addLine("<br>", "command", 80 * commands.length + 50);
            break;
        case "logout":
            window.location.href = '/logout';
            break;
        case "clear":
            setTimeout(function () {
                terminal.innerHTML = '<a id="before"></a>';
                before = document.getElementById("before");
            }, 1);
            break;
        case "banner":
            loopLines(banner, "", 80);
            break;
        case "linkedin":
            addLine("Opening LinkedIn...", "color2", 0);
            newTab(linkedin);
            break;
        case "github":
            addLine("Opening GitHub...", "color2", 0);
            newTab(github);
            break;
        default:
            addLine("<span class=\"inherit\">Command not found. For a list of commands, type <span class=\"command\">'help'</span>.</span>", "error", 100);
            break;
    }
}

async function updateSearchFile(name) {
    try {
        const file = await searchFile(name);

        if (file) {
            searched = [`${file.lastModified}   ${file.key}`];
        } else {
            searched = ['File not found!'];
        }
    } catch (error) {
        console.error('Error updating files:', error);
    }
}

async function searchFile(file) {
    try {
        const response = await fetch(`/searchFile?username=${encodeURIComponent(whoami)}&searchedFile=${encodeURIComponent(file)}`);

        if (response.ok) {
            const file = await response.json();

            return file ? {
                key: file.key,
                lastModified: formatDate(file.lastModified)
            } : null;
        } else {
            console.error('Failed to fetch file:', response.statusText);
        }
    } catch (error) {
        console.error('Error fetching file:', error);
    }

    return null;
}


function handlerUploadFolder() {
    const popupWidth = 600;
    const popupHeight = 400;
    const left = (window.innerWidth / 2) - (popupWidth / 2);
    const top = (window.innerHeight / 2) - (popupHeight / 2);

    // Open a new window with specified size and position
    const popup = window.open('', '', `width=${popupWidth},height=${popupHeight},top=${top},left=${left}`);

    // HTML content for the popup
    const popupContent = `
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta http-equiv="X-UA-Compatible" content="IE=edge">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Directory Upload</title>
            <style>
                body {
                    font-family: 'Courier New', Courier, monospace;
                    color: #519975;
                    background-color: #211D1B;
                    display: flex;
                    flex-direction: column;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    margin: 0;
                    text-align: center;
                }

                form {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                }

                input[type="file"] {
                    margin-bottom: 20px;
                }

                button {
                    padding: 10px;
                    background-color: #73ABAD;
                    border: none;
                    color: #211D1B;
                    border-radius: 3px;
                    font-size: 1em;
                    cursor: pointer;
                }

                button:hover {
                    background-color: #B89076;
                    color: #211D1B;
                }
            </style>
        </head>
        <body>
            <h2>Select Directory to Upload</h2>
            <form id="uploadForm">
                <input type="file" id="fileInput" name="directory" webkitdirectory multiple>
                <button type="button" onclick="uploadDirectory()">Upload Directory</button>
            </form>

            <script>
                function uploadDirectory() {
                    const input = document.getElementById('fileInput');
                    const files = input.files;

                    if (files.length === 0) {
                        alert('No files selected.');
                        return;
                    }

                    const formData = new FormData();
                    formData.append('username', '${whoami}'); 

                    for (const file of files) {
                        formData.append('directory', file);
                    }

                    fetch('/uploadDirectory', {
                        method: 'POST',
                        body: formData
                    })
                    .then(response => response.json())
                    .then(result => {
                        if (result) {
                            alert('Directory uploaded successfully.');
                        } else {
                            alert('Directory upload failed, maybe rename or check size.');
                        }
                        window.close();  
                    })
                    .catch(error => {
                        console.error('Error uploading directory:', error);
                        alert('Failed to upload directory.');
                    });
                }
            </script>
        </body>
        </html>
    `;

    // Write the content to the popup window
    popup.document.open();
    popup.document.write(popupContent);
    popup.document.close();
}



function handlerUploadFile() {
    const popupWidth = 600;
    const popupHeight = 400;
    const left = (window.innerWidth / 2) - (popupWidth / 2);
    const top = (window.innerHeight / 2) - (popupHeight / 2);

    // Open a new window with specified size and position
    const popup = window.open('', '', `width=${popupWidth},height=${popupHeight},top=${top},left=${left}`);

    // HTML content for the popup
    const popupContent = `
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta http-equiv="X-UA-Compatible" content="IE=edge">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>File Upload</title>
            <style>
                body {
                    font-family: 'Courier New', Courier, monospace;
                    color: #519975;
                    background-color: #211D1B;
                    display: flex;
                    flex-direction: column;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    margin: 0;
                    text-align: center;
                }

                form {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                }

                input[type="file"] {
                    margin-bottom: 20px;
                }

                button {
                    padding: 10px;
                    background-color: #73ABAD;
                    border: none;
                    color: #211D1B;
                    border-radius: 3px;
                    font-size: 1em;
                    cursor: pointer;
                }

                button:hover {
                    background-color: #B89076;
                    color: #211D1B;
                }
            </style>
        </head>
        <body>
            <h2>Select Files to Upload</h2>
            <form id="uploadForm">
                <input type="file" id="fileInput" name="file" multiple>
                <button type="button" onclick="uploadFiles()">Upload</button>
            </form>

            <script>
                function uploadFiles() {
                    const input = document.getElementById('fileInput');
                    const files = input.files;

                    if (files.length === 0) {
                        alert('No files selected.');
                        return;
                    }

                    const formData = new FormData();
                    formData.append('username', '${whoami}'); 

                    for (const file of files) {
                        formData.append('file', file);
                    }

                    fetch('/uploadFile', {
                        method: 'POST',
                        body: formData
                    })
                    .then(response => response.text())
                    .then(result => {
                        alert(result);
                        window.close();  
                    })
                    .catch(error => {
                        console.error('Error uploading files:', error);
                        alert('File to upload failed, maybe rename or check size.');
                    });
                }
            </script>
        </body>
        </html>
    `;

    // Write the content to the popup window
    popup.document.open();
    popup.document.write(popupContent);
    popup.document.close();
}

async function handlerDownload(name) {
    try {
        let url;
        if (name.includes('.')) {
            url = `/downloadFile?username=${encodeURIComponent(whoami)}&fileName=${encodeURIComponent(name)}`;
        } else {
            // For folders
            url = `/downloadFolder?username=${encodeURIComponent(whoami)}&folderName=${encodeURIComponent(name)}`;
        }

        const response = await fetch(url, {
            method: 'GET'
        });

        if (response.ok) {
            const contentDisposition = response.headers.get('Content-Disposition');
            const fileName = contentDisposition
                ? contentDisposition.split('filename=')[1].replace(/"/g, '')
                : 'download';

            const blob = await response.blob();
            const link = document.createElement('a');
            // Set the href to a URL created from the Blob
            link.href = window.URL.createObjectURL(blob);
            link.download = fileName;
            // Append the link to the document
            document.body.appendChild(link);
            link.click();
            link.remove();

            addLine("Download initiated!", "color2", 0);
        } else {
            addLine("Something went wrong with the download!", "color2", 0);
        }
    } catch (error) {
        addLine("Error handling download.", "color2", 0);
    }
}


async function handlerRemoveing(name) {
    try {
        if (name.includes('.')) {
            const response = await fetch(`/deleteFile?username=${encodeURIComponent(whoami)}&fileName=${encodeURIComponent(name)}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                addLine("Probably deleted. Reloading was our last-ditch effort to resurrect it. Spoiler: It didn’t work, but we tried!", "color2", 0);
            } else {
                addLine("Something went wrong!", "color2", 0);
            }
        } else {
            const response = await fetch(`/deleteFolder?username=${encodeURIComponent(whoami)}&folderName=${encodeURIComponent(name)}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                addLine("Probably deleted. Reloading was our last-ditch effort to resurrect it. Spoiler: It didn’t work, but we tried!", "color2", 0);
            } else {
                addLine("Something went wrong!", "color2", 0);
            }
        }
    } catch (error) {
        addLine("Error handling deleting.", "color2", 0);
    }
}

async function handlerRenMov(from, to) {
    try {
        if (from.includes('.')) {
            const response = await fetch(`/renameFile?username=${encodeURIComponent(whoami)}&fileName=${encodeURIComponent(from)}&newFileName=${encodeURIComponent(to)}`, {
                method: 'PUT'
            });

            if (response.ok) {
                addLine("Ok? Maybe reload page.", "color2", 0);
            } else {
                addLine("Something went wrong!", "color2", 0);
            }
        } else {
            const response = await fetch(`/renameFolder?username=${encodeURIComponent(whoami)}&folderName=${encodeURIComponent(from)}&newFolderName=${encodeURIComponent(to)}`, {
                method: 'PUT'
            });

            if (response.ok) {
                addLine("Ok? Maybe reload page.", "color2", 0);
            } else {
                addLine("Something went wrong!", "color2", 0);
            }
        }
    } catch (error) {
        addLine("Error handling rename or move.", "color2", 0);
    }
}

function newTab(link) {
    setTimeout(function() {
        window.open(link, "_blank");
    }, 500);
}

function addLine(text, style, time) {
    let t = "";
    for (let i = 0; i < text.length; i++) {
        if (text.charAt(i) === " " && text.charAt(i + 1) === " ") {
            t += "&nbsp;&nbsp;";
            i++;
        } else {
            t += text.charAt(i);
        }
    }
    setTimeout(function() {
        const next = document.createElement("p");
        next.innerHTML = t;
        next.className = style;

        before.parentNode.insertBefore(next, before);

        window.scrollTo(0, document.body.offsetHeight);
    }, time);
}

function loopLines(name, style, time) {
    name.forEach(function(item, index) {
        addLine(item, style, index * time);
    });
}
