let before = document.getElementById("before");
const command = document.getElementById("typer");
const textarea = document.getElementById("texter");
const terminal = document.getElementById("terminal");

let git = 0;
let pw = false;

const commands = [];

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
            commander(command.innerHTML.toLowerCase());
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

function commander(cmd) {
    switch (cmd.toLowerCase().split(' ')[0]) {
        case "mv":
            setTimeout(handlerRenMov(cmd.toLowerCase().split(' ')[1],
                cmd.toLowerCase().split(' ')[2]), 80);
            break;
        case "rm":
            setTimeout(handlerRemoveing(cmd.toLowerCase().split(' ')[1]), 80);
            break;
        case "wget":
            setTimeout(handlerDownload(cmd.toLowerCase().split(' ')[1]), 80);
            break;
        case "uploadfolder":

        case "find":

        case "uploadfile":

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
            setTimeout(function() {
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
            window.location.href='/logout';
            break;
        case "clear":
            setTimeout(function() {
                terminal.innerHTML = '<a id="before"></a>';
                before = document.getElementById("before");
            }, 1);
            break;
        case "banner":
            loopLines(banner, "", 80);
            break;
        // socials
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

async function handlerDownload(name) {
    try {
        let url;
        if (name.includes('.')) {
            // For files
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

            // Create a Blob from the response
            const blob = await response.blob();
            // Create a link element
            const link = document.createElement('a');
            // Set the href to a URL created from the Blob
            link.href = window.URL.createObjectURL(blob);
            link.download = fileName;
            // Append the link to the document
            document.body.appendChild(link);
            // Programmatically click the link to trigger the download
            link.click();
            // Remove the link from the document
            link.remove();

            addLine("Download initiated!", "color2", 0);
        } else {
            addLine("Something went wrong with the download!", "color2", 0);
        }
    } catch (error) {
        addLine("Error handling download.", "color2", 0);
        console.error('Error handling download:', error);
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
        console.error('Error handling rename or move:', error);
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
        console.error('Error handling rename or move:', error);
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
