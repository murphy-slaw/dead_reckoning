package net.funkpla.dead_reckoning;

import com.mojang.blaze3d.platform.InputConstants;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.client.SurveyorClientEvents;
import folk.sisby.surveyor.landmark.Landmark;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DeadReckoning implements ClientModInitializer {
  public static final String MOD_ID = "dead_reckoning";
  public static final String MOD_NAME = "Dead Reckoning";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static final DeadReckoningConfig CONFIG = DeadReckoningConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", MOD_ID, DeadReckoningConfig.class);
  public static final TagKey<Item> COMMON_COMPASSES = TagKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath("c", "compasses"));
  public static final TagKey<Item> COMPASSES = TagKey.create(BuiltInRegistries.ITEM.key(), Identifier.of(DeadReckoning.MOD_ID, "compasses"));
  @Nullable
  @Getter @Setter private static List<Landmark> landmarks;
  @Getter @Setter private static boolean HUD_ENABLED = true;
  private static final KeyMapping TOGGLE_HUD_KEY = new KeyMapping(
    "key.dead_reckoning.toggle",
    InputConstants.Type.KEYSYM,
    GLFW.GLFW_KEY_PERIOD,
    net.minecraft.client.KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("dead_reckoning", "general")));

  public static void invalidateLandmarks() {
    setLandmarks(null);
  }

  public static boolean isCompass(ItemStack stack) {
    var registry = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ITEM);
    return stack.is(COMPASSES) || stack.is(COMMON_COMPASSES) || CONFIG.compassItems.contains(registry.getKey(stack.getItem()).identifier().toString());
  }

  @Override
  public void onInitializeClient() {
    LOGGER.info("Loading {}...", MOD_NAME);

    WorldSummary.enableLandmarks();

    HudRenderCallback.EVENT.register(new CompassOverlay());

    KeyBindingHelper.registerKeyBinding(TOGGLE_HUD_KEY);

    SurveyorClientEvents.Register.landmarksAdded(
      Identifier.of(MOD_ID, "landmarks_added"),
      (worldLandmarks, landmarks) -> invalidateLandmarks());

    SurveyorClientEvents.Register.landmarksRemoved(
      Identifier.of(MOD_ID, "landmarks_removed"),
      (worldLandmarks, landmarks) -> invalidateLandmarks());

    ClientTickEvents.END_CLIENT_TICK.register(
      client -> {
        while (TOGGLE_HUD_KEY.consumeClick()) {
          if (client.player == null) return;
          client.player.displayClientMessage(
            Component.translatable(
              "dead_reckoning.toggled_hud_visibility", isHUD_ENABLED() ? "off" : "on"),
            true);
          setHUD_ENABLED(!isHUD_ENABLED());
        }
      });
  }
}
