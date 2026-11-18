package ru.slie.luna.plugins.gravity.web;

import org.springframework.stereotype.Component;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.web.WebItem;

import java.util.Map;

@Component
public class GravityAdminScriptFilesWebItem implements WebItem {
    private final I18nResolver i18n;

    public GravityAdminScriptFilesWebItem(I18nResolver i18n) {
        this.i18n = i18n;
    }

    @Override
    public String getId() {
        return "scripts";
    }

    @Override
    public String getLocation() {
        return "admin/plugins/gravity";
    }

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public String getName() {
        return i18n.getText("gravity.web.item.scripts.label");
    }

    @Override
    public String getRouteName() {
        return "gravityScripts";
    }

    @Override
    public Map<String, String> getRouteParams() {
        return Map.of();
    }
}
