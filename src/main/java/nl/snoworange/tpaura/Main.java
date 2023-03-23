package nl.snoworange.tpaura;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import nl.snoworange.tpaura.hud.HudTPAura;
import nl.snoworange.tpaura.modules.TPAura;
import org.slf4j.Logger;

public class Main extends MeteorAddon {

    public static final Logger LOG = LogUtils.getLogger();
    public static final HudGroup HUD_GROUP = new HudGroup("Misc");

    @Override
    public void onInitialize() {
        LOG.info("Initializing TPAura addon v0.0.1 by Snoworange and HGKidudeski");

        // Module
        Modules.get().add(new TPAura());

        // HUD
        Hud.get().register(HudTPAura.INFO);
    }

    @Override
    public String getPackage() {
        return "nl.snoworange.tpaura";
    }
}
