package net.funkpla.dead_reckoning;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.reloader.MarkerTextures;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class AtlasMarker {
  private final Landmark landmark;
  @Getter private final double distance;
  @Getter private final double pitch;
  @Getter private final double yaw;
  @Getter private final Integer color;

  public AtlasMarker(Player player, Landmark landmark) {
    this.landmark = landmark;
    BlockPos pos = landmark.get(LandmarkComponentTypes.POS);
    assert (pos != null);
    double dx = pos.getX() - player.getX();
    double dy = pos.getY() - player.getEyeHeight();
    double dz = pos.getZ() - player.getZ();
    distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
    pitch = -Mth.atan2(dy, distance) * Mth.RAD_TO_DEG;
    yaw = Mth.wrapDegrees(-(Mth.atan2(dx, dz) * Mth.RAD_TO_DEG + (double) player.getYRot()));
    this.color =
        landmark.getOrDefault(LandmarkComponentTypes.COLOR, DyeColor.WHITE.getFireworkColor());
  }

  public Texture getTexture() {

    if (FabricLoader.getInstance().isModLoaded(AntiqueAtlas.ID)) {
      MarkerTextures textures = MarkerTextures.getInstance();
      MarkerTexture markerTexture = textures.get(textures.minimumId(landmark.id()));
      if (markerTexture != null)
        return new Texture(
            markerTexture.id(),
            markerTexture.accentId(),
            markerTexture.textureWidth(),
            markerTexture.textureHeight(),
            null);
    }

    if (landmark.contains(LandmarkComponentTypes.STACK)) {
      var stack = landmark.get(LandmarkComponentTypes.STACK);
      if (stack != null && !stack.isEmpty()) {
        return new Texture(null, null, 16, 16, stack);
      }
    }

    return new Texture(null, null, 16, 16, null);
  }

  public boolean hasAccent() {
    return color != null && getTexture().accentId() != null;
  }

  float calcMarkerScale() {
    float chunkDistance = (float) ((getDistance() / 64f) + 1f);
    return 1f / chunkDistance;
  }

  float calcScale() {
    return Float.min(
        DeadReckoning.CONFIG.markers.scale,
        Float.max(calcMarkerScale(), DeadReckoning.CONFIG.markers.minScale));
  }

  public record Texture(
      Identifier id, Identifier accentId, int width, int height, ItemStack stack) {}
}
