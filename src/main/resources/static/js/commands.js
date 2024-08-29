const linkedin = "https://md.linkedin.com/in/victor-haideu-b2200226b";
const github = "https://github.com/wallhackj";

let whoami = [];
let list = []

async function fetchString(url) {
    try {
        const response = await fetch(url);
        if (response.ok) {
            return await response.text();
        }
    } catch (error) {
        console.error('Error fetching string:', error);
        throw error;
    }
}

async function updateWhoami() {
    try {
        whoami = [
            `${await fetchString('/username')}`,
        ];
    } catch (error) {
        console.error('Error updating whoami:', error);
    }
}

function formatDate(isoString) {
    const date = new Date(isoString);
    return date.toLocaleString();
}

async function listFiles() {
    try {
        const username = await fetchString('/username');
        const response = await fetch(`/listFiles?username=${encodeURIComponent(username)}`);

        if (response.ok) {
            const files = await response.json();

            return files.map(file => ({
                key: file.key,
                lastModified: formatDate(file.lastModified)
            }));
        } else {
            console.error('Failed to fetch file list:', response.statusText);
        }
    } catch (error) {
        console.error('Error fetching files:', error);
    }

    return [];
}

async function updateFiles() {
    try {
        const files = await listFiles();

        list = files.map(file => `${file.lastModified}   ${file.key}`);
    } catch (error) {
        console.error('Error updating files:', error);
    }
}

window.addEventListener("load", updateWhoami);
window.addEventListener("load", updateFiles);

social = [
    "<br>",
    'LinkedIn: <a href="' + linkedin + '" target="_blank">Victor Haideu' + "</a>",
    'GitHub: <a href="' + github + '" target="_blank"> wallhack' + "</a>",
    "<br>"
];

help = [
    '<span class="command">uploadfile</span>     Upload file (work in progress)',
    '<span class="command">find</span>           Want to find something? (work in progress)',
    '<span class="command">uploadfolder</span>   Upload folder (work in progress)',
    '<span class="command">wget</span>           Download what you want.(work in progress)',

    '<span class="command">whoami</span>         Who are you?',
    '<span class="command">ls</span>             List all files in drive.',
    '<span class="command">mv</span>             Move/Rename Files and Directories (mv foto.jpg photo.jpg or mv directory_name/foto.jpg newDirectory/foto.jpg)',
    '<span class="command">rm</span>             Remove directory or file (rm filename.png or rm directoryName)',
    '<span class="command">social</span>         Display social networks',
    '<span class="command">history</span>        View command history',
    '<span class="command">help</span>           You obviously already know what this does',
    '<span class="command">banner</span>         Display the header',
    '<span class="command">clear</span>          Cleaning of terminal',
    '<span class="command">logout</span>         Logout ofc',
];

banner = [
    '<span class="index">Cloud Drive Not A Corporation. All knights reserved.</span>',
    '                                                                                                    ',
    '________/\\\\\\\\\\\\\\\\\\__/\\\\\\\\\\\\________________________________________/\\\\\\_____________/\\\\\\\\\\\\\\\\\\\\\\\\_____________________________________________________        ',
    ' _____/\\\\\\////////__\\////\\\\\\_______________________________________\\/\\\\\\____________\\/\\\\\\////////\\\\\\___________________________________________________       ',
    '  ___/\\\\\\/______________\\/\\\\\\_______________________________________\\/\\\\\\____________\\/\\\\\\______\\//\\\\\\________________/\\\\\\______________________________      ',
    '   __/\\\\\\________________\\/\\\\\\________/\\\\\\\\\\_____/\\\\\\____/\\\\\\________\\/\\\\\\____________\\/\\\\\\_______\\/\\\\\\__/\\\\/\\\\\\\\\\\\\\__\\///___/\\\\\\____/\\\\\\_____/\\\\\\\\\\\\\\\\__     ',
    '    _\\/\\\\\\________________\\/\\\\\\______/\\\\\\///\\\\\\__\\/\\\\\\___\\/\\\\\\___/\\\\\\\\\\\\\\\\\\____________\\/\\\\\\_______\\/\\\\\\_\\/\\\\\\/////\\\\\\__/\\\\\\_\\//\\\\\\__/\\\\\\____/\\\\\\/////\\\\\\_    ',
    '     _\\//\\\\\\_______________\\/\\\\\\_____/\\\\\\__\\//\\\\\\_\\/\\\\\\___\\/\\\\\\__/\\\\\\////\\\\\\____________\\/\\\\\\_______\\/\\\\\\_\\/\\\\\\___\\///__\\/\\\\\\__\\//\\\\\\/\\\\\\____/\\\\\\\\\\\\\\\\\\\\\\__   ',
    '      __\\///\\\\\\_____________\\/\\\\\\____\\//\\\\\\__/\\\\\\__\\/\\\\\\___\\/\\\\\\_\\/\\\\\\__\\/\\\\\\____________\\/\\\\\\_______/\\\\\\__\\/\\\\\\_________\\/\\\\\\___\\//\\\\\\\\\\____\\//\\\\///////___  ',
    '       ____\\////\\\\\\\\\\\\\\\\\\__/\\\\\\\\\\\\\\\\\\__\\///\\\\\\\\\\/___\\//\\\\\\\\\\\\\\\\\\__\\//\\\\\\\\\\\\\\/\\\\___________\\/\\\\\\\\\\\\\\\\\\\\\\\\/___\\/\\\\\\_________\\/\\\\\\____\\//\\\\\\______\\//\\\\\\\\\\\\\\\\\\\\_ ',
    '        _______\\/////////__\\/////////_____\\/////______\\/////////____\\///////\\//____________\\////////////_____\\///__________\\///______\\///________\\//////////__',
    '                                                                                                    ',
    '<span class="color2">Welcome to my interactive web terminal.</span>',
    "<span class=\"color2\">For a list of available commands, type</span> <span class=\"command\">'help'</span><span class=\"color2\">.</span>",
];