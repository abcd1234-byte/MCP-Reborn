package net.minecraft.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class DimensionSpecialEffects {
   private static final Object2ObjectMap<ResourceLocation, DimensionSpecialEffects> EFFECTS = Util.make(new Object2ObjectArrayMap<>(), (p_108881_) -> {
      DimensionSpecialEffects.OverworldEffects dimensionspecialeffects$overworldeffects = new DimensionSpecialEffects.OverworldEffects();
      p_108881_.defaultReturnValue(dimensionspecialeffects$overworldeffects);
      p_108881_.put(DimensionType.OVERWORLD_EFFECTS, dimensionspecialeffects$overworldeffects);
      p_108881_.put(DimensionType.NETHER_EFFECTS, new DimensionSpecialEffects.NetherEffects());
      p_108881_.put(DimensionType.END_EFFECTS, new DimensionSpecialEffects.EndEffects());
   });
   private final float[] sunriseCol = new float[4];
   private final float cloudLevel;
   private final boolean hasGround;
   private final DimensionSpecialEffects.SkyType skyType;
   private final boolean forceBrightLightmap;
   private final boolean constantAmbientLight;

   public DimensionSpecialEffects(float pCloudLevel, boolean pHasGround, DimensionSpecialEffects.SkyType pSkyType, boolean pForceBrightLightmap, boolean pConstantAmbientLight) {
      this.cloudLevel = pCloudLevel;
      this.hasGround = pHasGround;
      this.skyType = pSkyType;
      this.forceBrightLightmap = pForceBrightLightmap;
      this.constantAmbientLight = pConstantAmbientLight;
   }

   public static DimensionSpecialEffects forType(DimensionType pDimensionType) {
      return EFFECTS.get(pDimensionType.effectsLocation());
   }

   @Nullable
   public float[] getSunriseColor(float pTimeOfDay, float pPartialTicks) {
      float f = 0.4F;
      float f1 = Mth.cos(pTimeOfDay * ((float)Math.PI * 2F)) - 0.0F;
      float f2 = -0.0F;
      if (f1 >= -0.4F && f1 <= 0.4F) {
         float f3 = (f1 - -0.0F) / 0.4F * 0.5F + 0.5F;
         float f4 = 1.0F - (1.0F - Mth.sin(f3 * (float)Math.PI)) * 0.99F;
         f4 = f4 * f4;
         this.sunriseCol[0] = f3 * 0.3F + 0.7F;
         this.sunriseCol[1] = f3 * f3 * 0.7F + 0.2F;
         this.sunriseCol[2] = f3 * f3 * 0.0F + 0.2F;
         this.sunriseCol[3] = f4;
         return this.sunriseCol;
      } else {
         return null;
      }
   }

   public float getCloudHeight() {
      return this.cloudLevel;
   }

   public boolean hasGround() {
      return this.hasGround;
   }

   public abstract Vec3 getBrightnessDependentFogColor(Vec3 pFogColor, float pBrightness);

   public abstract boolean isFoggyAt(int pX, int pY);

   public DimensionSpecialEffects.SkyType skyType() {
      return this.skyType;
   }

   public boolean forceBrightLightmap() {
      return this.forceBrightLightmap;
   }

   public boolean constantAmbientLight() {
      return this.constantAmbientLight;
   }

   @OnlyIn(Dist.CLIENT)
   public static class EndEffects extends DimensionSpecialEffects {
      public EndEffects() {
         super(Float.NaN, false, DimensionSpecialEffects.SkyType.END, true, false);
      }

      public Vec3 getBrightnessDependentFogColor(Vec3 p_108894_, float p_108895_) {
         return p_108894_.scale((double)0.15F);
      }

      public boolean isFoggyAt(int p_108891_, int p_108892_) {
         return false;
      }

      @Nullable
      public float[] getSunriseColor(float p_108888_, float p_108889_) {
         return null;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class NetherEffects extends DimensionSpecialEffects {
      public NetherEffects() {
         super(Float.NaN, true, DimensionSpecialEffects.SkyType.NONE, false, true);
      }

      public Vec3 getBrightnessDependentFogColor(Vec3 p_108901_, float p_108902_) {
         return p_108901_;
      }

      public boolean isFoggyAt(int p_108898_, int p_108899_) {
         return true;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class OverworldEffects extends DimensionSpecialEffects {
      public static final int CLOUD_LEVEL = 128;

      public OverworldEffects() {
         super(128.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
      }

      public Vec3 getBrightnessDependentFogColor(Vec3 p_108908_, float p_108909_) {
         return p_108908_.multiply((double)(p_108909_ * 0.94F + 0.06F), (double)(p_108909_ * 0.94F + 0.06F), (double)(p_108909_ * 0.91F + 0.09F));
      }

      public boolean isFoggyAt(int p_108905_, int p_108906_) {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum SkyType {
      NONE,
      NORMAL,
      END;
   }
}