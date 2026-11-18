package ru.slie.luna.plugins.gravity.script.file;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** File or directory of the scripts tree. {@code path} is relative to the scripts root ("" for the root). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScriptFileNode(String name, String path, boolean directory, long size, long modified, List<ScriptFileNode> children) {}
