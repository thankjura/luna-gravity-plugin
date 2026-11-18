package ru.slie.luna.plugins.gravity.rest.request;

public class ScriptFileRequest {
    private String path;
    private String newPath;
    private String content;
    /** Last modification time seen by the client: saving fails if the file was changed since. */
    private Long modified;

    public String getPath() {
        return path;
    }

    public String getNewPath() {
        return newPath;
    }

    public String getContent() {
        return content;
    }

    public Long getModified() {
        return modified;
    }
}
