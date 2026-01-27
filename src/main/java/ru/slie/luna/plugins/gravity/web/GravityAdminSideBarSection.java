package ru.slie.luna.plugins.gravity.web;

import org.springframework.stereotype.Component;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.web.WebSection;

@Component
public class GravityAdminSideBarSection implements WebSection {
    private final I18nResolver i18n;

    public GravityAdminSideBarSection(I18nResolver i18n) {
        this.i18n = i18n;
    }

    @Override
    public String getId() {
        return "gravity";
    }

    @Override
    public String getLocation() {
        return "admin/plugins";
    }

    @Override
    public int getOrder() {
        return 500;
    }

    @Override
    public String getName() {
        return i18n.getText("gravity.web.section.label");
    }
}
