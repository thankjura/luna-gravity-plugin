package ru.slie.luna.plugins.gravity.web;

import org.springframework.stereotype.Component;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.web.WebItem;

import java.util.Map;

@Component
public class GravityAdminListenersWebItem implements WebItem {
    private final I18nResolver i18n;

    public GravityAdminListenersWebItem(I18nResolver i18n) {
        this.i18n = i18n;
    }

    @Override
    public String getId() {
        return "listeners";
    }

    @Override
    public String getLocation() {
        return "admin/plugins/gravity";
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String getName() {
        return i18n.getText("gravity.web.item.listeners.label");
    }

    @Override
    public String getRouteName() {
        return "gravityListeners";
    }

    @Override
    public Map<String, String> getRouteParams() {
        return Map.of();
    }
}
