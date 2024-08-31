const linkedin = "https://md.linkedin.com/in/victor-haideu-b2200226b";
const github = "https://github.com/wallhackj";

let whoami = [];
let list = [];

// Util function to fetch string data from a URL
async function fetchString(url) {
    try {
        const response = await fetch(url);
        if (!response.ok) new Error(`Failed to fetch: ${response.statusText}`);
        return await response.text();
    } catch (error) {
        addLine("Something bad on server side!", "color2", 0);
        throw error;
    }
}

// Fetch and update the 'whoami' information
async function updateWhoami() {
    try {
        whoami = [await fetchString('/username')];
    } catch (error) {
        addLine("Who are you?", "color2", 0);
        whoami = [];
    }
}

// Format ISO date string to a more readable format
const formatDate = isoString => new Date(isoString).toLocaleString();

// Fetch and list all files for the current user
async function listFiles() {
    try {

        const response = await fetch(`/listFiles?username=${encodeURIComponent(whoami)}`);

        if (!response.ok) new Error(`Failed to fetch files: ${response.statusText}`);

        const files = await response.json();
        return files.map(file => ({
            key: file.key,
            lastModified: formatDate(file.lastModified)
        }));
    } catch (error) {
        addLine("Server said something is wrong!", "color2", 0);
    }

    return [];
}

// Update the file list for the UI
async function updateFiles() {
    try {
        // Ensure the username is always updated before fetching files
        await updateWhoami();
        list = (await listFiles()).map(file => `${file.lastModified}   ${file.key}`);
    } catch (error) {
        addLine("Updating error!", "color2", 0);
    }
}

// Initialize the application
window.addEventListener("load", async () => {
    await updateFiles();  // Combine updating username and files into one function
});

// Social links data
const social = [
    "<br>",
    `LinkedIn: <a href="${linkedin}" target="_blank">Victor Haideu</a>`,
    `GitHub: <a href="${github}" target="_blank">wallhack</a>`,
    "<br>"
];

// Help command instructions
const help = [
    '<span class="command">whoami</span>         Who are you?',
    '<span class="command">uploadfile</span>     Upload file.',
    '<span class="command">uploadfolder</span>   Upload folder.',
    '<span class="command">find</span>           Want to find something? (find filename)',
    '<span class="command">ls</span>             List all files in drive. (Reload may help)',
    '<span class="command">wget</span>           Download what you want. (wget filename.png or directory)',
    '<span class="command">mv</span>             Move/Rename Files and Directories. (mv foto.jpg photo.jpg or mv directory_name/foto.jpg newDirectory/foto.jpg)',
    '<span class="command">rm</span>             Remove directory or file. (rm filename.png or rm directoryName)',
    '<span class="command">social</span>         Display social networks.',
    '<span class="command">history</span>        View command history.',
    '<span class="command">help</span>           You obviously already know what this does.',
    '<span class="command">banner</span>         Display the header.',
    '<span class="command">clear</span>          Cleaning of terminal.',
    '<span class="command">logout</span>         Logout ofc.',
];

// Terminal banner display
let banner = [
    '<span class="index">Cloud Drive Not A Corporation. All rights reserved.</span>',
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
    '<span class="color2">For a list of available commands, type</span> <span class="command">\'help\'</span><span class="color2">.</span>',
];
