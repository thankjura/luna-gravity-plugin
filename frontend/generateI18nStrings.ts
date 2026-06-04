import * as fs from "fs";
import * as path from "path";

const SOURCE_DIR = path.join('.', 'src');
const FILE_MASK = new RegExp(".*\\.(vue|ts)");
const REGEX = new RegExp("\\$i18n\\.(?<type>[tp])\\(([\'\"])(?<message>.+?)\\2", 'g');

const TARGET_DIR = path.join('.', 'src', 'locales');

const messages = [];
const messagesMap = {};

const findFiles = (directory: string, callback: (filename: string) => void) => {
    const files = fs.readdirSync(directory);
    for (let i = 0; i < files.length; i++) {
        const filename = path.join(directory, files[i]);
        const stat = fs.lstatSync(filename);
        if (stat.isDirectory()) {
            findFiles(filename, callback);
        } else {
            if (!FILE_MASK.test(filename)) {
                continue;
            }
            callback(filename);
        }
    }
}
//
const parseLine = (filename: string, line: string, lineNum: number) => {
    const matches = line.matchAll(REGEX);
    for (const match of matches) {
        const type = match.groups['type'];
        const msg = match.groups['message'];
        const data = messagesMap[msg];
        if (data) {
            data.lines.push(filename + ":" + lineNum);
            if (type == "p") {
                data.parameterized = true;
            }
        } else {
            messagesMap[msg] = {
                "key": msg,
                "lines": [filename + ":" + lineNum],
                "parameterized": type === "p",
            }
            messages.push(messagesMap[msg]);
        }
    }
}

//
const fn = (filename: string) => {
    const content = fs.readFileSync(filename, 'utf-8');
    let lineNum = 1;
    content.split(/\r?\n/).forEach(line => {
        parseLine(filename, line, lineNum++);
    });
}

findFiles(SOURCE_DIR, fn);

const writeFile = (filePath: string, existsMessages?: {[key: string]: string}) => {
    const writer = fs.createWriteStream(filePath, {
        flags: 'w'
    });
    if (!existsMessages) {
        existsMessages = {};
    }

    writer.write("export const messages = {");

    for (let i = 0;  i < messages.length; i++) {
        const msgObj = messages[i];
        writer.write("\n");
        for (let k = 0;  k < msgObj.lines.length; k++) {
            writer.write("  // " + msgObj.lines[k].replaceAll(path.sep, path.posix.sep) + "\n");
        }
        if (msgObj.parameterized) {
            writer.write("  // parameterized\n");
        }
        let translate = existsMessages[msgObj.key];
        if (translate) {
            writer.write("  \"" + msgObj.key + "\": \"" + translate + "\",\n");
        } else {
            writer.write("  \"" + msgObj.key + "\": \"\",\n");
        }
    }

    writer.write("}\n");
    writer.close();
}

fs.readdirSync(TARGET_DIR).forEach(file => {
    const filePath = path.resolve(path.join(TARGET_DIR, file));

    import(`file://${filePath}`).then(({messages}) => {
        writeFile(filePath, messages);
    }).catch(() => {
        writeFile(filePath, {});
    });
});
