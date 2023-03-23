package nl.snoworange.tpaura.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import nl.snoworange.tpaura.Main;
import nl.snoworange.tpaura.util.RotationUtils;

import java.util.Random;

public class TPAura extends Module {

    public TPAura() {
        super(Categories.Movement,
            "TP Aura",
            "teleport spams around your enemies"
        );
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Random random = new Random();
    private int tickTimer = 0;
    public static PlayerEntity currentTarget;

    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("In what range you will target the enemies.")
        .defaultValue(4.25)
        .min(1)
        .sliderMax(7)
        .build()
    );

    public final Setting<Integer> attackTicksDelay = sgGeneral.add(new IntSetting.Builder()
        .name("attack-ticks-delay")
        .description("The delay in ticks to wait to attack enemies.")
        .defaultValue(2)
        .min(0)
        .sliderMax(20)
        .build()
    );

    public final Setting<Integer> randomRange = sgGeneral.add(new IntSetting.Builder()
        .name("random-range")
        .description("In what range you will teleport around enemies")
        .defaultValue(3)
        .min(1)
        .sliderMax(5)
        .build()
    );

    private final Setting<Boolean> ignoreWallTp = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-wall-tp")
        .description("Ignores teleport packets that is inside a block.")
        .defaultValue(true)
        .build()
    );

    private final Setting<CriticalMode> criticalMode = sgGeneral.add(new EnumSetting.Builder<CriticalMode>()
        .name("critical-mode")
        .description("In what method the client should crit enemies.")
        .defaultValue(CriticalMode.PACKET)
        .build()
    );

    @Override
    public void onActivate() {
        tickTimer = 0;
    }

    @Override
    public void onDeactivate() {

    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (this.isActive()) {

            tickTimer++;

            if (mc.player == null
                || mc.world == null
                || mc.interactionManager == null
                || tickTimer < attackTicksDelay.get()) return;

            for (PlayerEntity player : mc.world.getPlayers()) {

                if (player == mc.player) continue;

                if (player.getHealth() < 0 || player.isDead()) continue;

                currentTarget = player;

                if (player.isAttackable() && mc.player.distanceTo(player) <= targetRange.get()) {

                    int randomX = random.nextInt(randomRange.get()) * 2 - 2;
                    int randomZ = random.nextInt(randomRange.get()) * 2 - 2;

                    boolean mayStart = false;

                    if (ignoreWallTp.get()) {

                        Block targetBlock = mc.world.getBlockState(new BlockPos(new Vec3d(player.getX() + randomX, player.getY(), player.getZ() + randomZ))).getBlock();

                        if (targetBlock instanceof AirBlock
                        || targetBlock == Blocks.WATER
                        || targetBlock == Blocks.LAVA) {
                            mayStart = true;
                        }
                    } else {
                        mayStart = true;
                    }

                    if (!mayStart) return;

                    mc.player.setPosition(player.getX() + randomX,
                        player.getY(), player.getZ() + randomZ);

                    RotationUtils.Rotation rotations = RotationUtils
                        .getNeededRotations(player.getBoundingBox().getCenter());

                    mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.LookAndOnGround(rotations.getYaw(),
                            rotations.getPitch(), mc.player.isOnGround()));

                    doCritical();

                    mc.interactionManager.attackEntity(mc.player, player);
                    mc.player.swingHand(Hand.MAIN_HAND);

                    tickTimer = 0;
                }
            }
        }
    }

    public void doCritical() {

        if (mc.player == null) return;

        if (!mc.player.isOnGround())
            return;

        if (mc.player.isTouchingWater() || mc.player.isInLava())
            return;

        switch (criticalMode.get()) {

            case PACKET:
                doPacketJump();
                break;

            case MINIJUMP:
                doMiniJump();
                break;

            case FULL:
                doFullJump();
                break;

            case OFF:
                break;
        }
    }

    public void doPacketJump() {

        if (mc.player == null) return;

        double posX = mc.player.getX();
        double posY = mc.player.getY();
        double posZ = mc.player.getZ();

        sendPos(posX, posY + 0.0625D, posZ, true);
        sendPos(posX, posY, posZ, false);
        sendPos(posX, posY + 1.1E-5D, posZ, false);
        sendPos(posX, posY, posZ, false);
    }

    public void sendPos(double x, double y, double z, boolean onGround) {

        if (mc.player == null) return;

        mc.player.networkHandler.sendPacket(
            new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround));
    }

    public void doMiniJump() {

        if (mc.player == null) return;

        mc.player.addVelocity(0, 0.1, 0);
        mc.player.fallDistance = 0.1F;
        mc.player.setOnGround(false);
    }

    public void doFullJump() {

        if (mc.player == null) return;

        mc.player.jump();
    }

    public enum CriticalMode {
        PACKET,
        MINIJUMP,
        FULL,
        OFF
    }
}
