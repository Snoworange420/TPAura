package nl.snoworange.tpaura.hud;

import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import nl.snoworange.tpaura.Main;
import nl.snoworange.tpaura.modules.TPAura;

public class HudTPAura extends HudElement {

    public static final HudElementInfo<HudTPAura> INFO = new HudElementInfo<>(Main.HUD_GROUP,
        "TPAura",
        "Displays current TPAura targets.",
        HudTPAura::new
    );

    public HudTPAura() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(renderer.textWidth("TPAura", true), renderer.textHeight(true));

        renderer.text("TPAura target: " + (TPAura.currentTarget != null ? TPAura.currentTarget.getEntityName() : "none"),
            x,
            y,
            Color.WHITE,
            true
        );
    }
}
