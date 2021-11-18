package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.gui.font.glyphs.MissingGlyph;
import net.minecraft.client.gui.font.glyphs.WhiteGlyph;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FontSet implements AutoCloseable {
   private static final EmptyGlyph SPACE_GLYPH = new EmptyGlyph();
   private static final GlyphInfo SPACE_INFO = () -> {
      return 4.0F;
   };
   private static final Random RANDOM = new Random();
   private final TextureManager textureManager;
   private final ResourceLocation name;
   private BakedGlyph missingGlyph;
   private BakedGlyph whiteGlyph;
   private final List<GlyphProvider> providers = Lists.newArrayList();
   private final Int2ObjectMap<BakedGlyph> glyphs = new Int2ObjectOpenHashMap<>();
   private final Int2ObjectMap<GlyphInfo> glyphInfos = new Int2ObjectOpenHashMap<>();
   private final Int2ObjectMap<IntList> glyphsByWidth = new Int2ObjectOpenHashMap<>();
   private final List<FontTexture> textures = Lists.newArrayList();

   public FontSet(TextureManager p_95062_, ResourceLocation p_95063_) {
      this.textureManager = p_95062_;
      this.name = p_95063_;
   }

   public void reload(List<GlyphProvider> pGlyphProviders) {
      this.closeProviders();
      this.closeTextures();
      this.glyphs.clear();
      this.glyphInfos.clear();
      this.glyphsByWidth.clear();
      this.missingGlyph = this.stitch(MissingGlyph.INSTANCE);
      this.whiteGlyph = this.stitch(WhiteGlyph.INSTANCE);
      IntSet intset = new IntOpenHashSet();

      for(GlyphProvider glyphprovider : pGlyphProviders) {
         intset.addAll(glyphprovider.getSupportedGlyphs());
      }

      Set<GlyphProvider> set = Sets.newHashSet();
      intset.forEach((int p_95076_) -> {
         for(GlyphProvider glyphprovider1 : pGlyphProviders) {
            GlyphInfo glyphinfo = (GlyphInfo)(p_95076_ == 32 ? SPACE_INFO : glyphprovider1.getGlyph(p_95076_));
            if (glyphinfo != null) {
               set.add(glyphprovider1);
               if (glyphinfo != MissingGlyph.INSTANCE) {
                  this.glyphsByWidth.computeIfAbsent(Mth.ceil(glyphinfo.getAdvance(false)), (p_169091_) -> {
                     return new IntArrayList();
                  }).add(p_95076_);
               }
               break;
            }
         }

      });
      pGlyphProviders.stream().filter(set::contains).forEach(this.providers::add);
   }

   public void close() {
      this.closeProviders();
      this.closeTextures();
   }

   private void closeProviders() {
      for(GlyphProvider glyphprovider : this.providers) {
         glyphprovider.close();
      }

      this.providers.clear();
   }

   private void closeTextures() {
      for(FontTexture fonttexture : this.textures) {
         fonttexture.close();
      }

      this.textures.clear();
   }

   public GlyphInfo getGlyphInfo(int p_95066_) {
      return this.glyphInfos.computeIfAbsent(p_95066_, (p_95088_) -> {
         return (GlyphInfo)(p_95088_ == 32 ? SPACE_INFO : this.getRaw(p_95088_));
      });
   }

   private RawGlyph getRaw(int p_95082_) {
      for(GlyphProvider glyphprovider : this.providers) {
         RawGlyph rawglyph = glyphprovider.getGlyph(p_95082_);
         if (rawglyph != null) {
            return rawglyph;
         }
      }

      return MissingGlyph.INSTANCE;
   }

   public BakedGlyph getGlyph(int p_95079_) {
      return this.glyphs.computeIfAbsent(p_95079_, (p_95086_) -> {
         return (BakedGlyph)(p_95086_ == 32 ? SPACE_GLYPH : this.stitch(this.getRaw(p_95086_)));
      });
   }

   private BakedGlyph stitch(RawGlyph pGlyphInfo) {
      for(FontTexture fonttexture : this.textures) {
         BakedGlyph bakedglyph = fonttexture.add(pGlyphInfo);
         if (bakedglyph != null) {
            return bakedglyph;
         }
      }

      FontTexture fonttexture1 = new FontTexture(new ResourceLocation(this.name.getNamespace(), this.name.getPath() + "/" + this.textures.size()), pGlyphInfo.isColored());
      this.textures.add(fonttexture1);
      this.textureManager.register(fonttexture1.getName(), fonttexture1);
      BakedGlyph bakedglyph1 = fonttexture1.add(pGlyphInfo);
      return bakedglyph1 == null ? this.missingGlyph : bakedglyph1;
   }

   public BakedGlyph getRandomGlyph(GlyphInfo pGlyph) {
      IntList intlist = this.glyphsByWidth.get(Mth.ceil(pGlyph.getAdvance(false)));
      return intlist != null && !intlist.isEmpty() ? this.getGlyph(intlist.getInt(RANDOM.nextInt(intlist.size()))) : this.missingGlyph;
   }

   public BakedGlyph whiteGlyph() {
      return this.whiteGlyph;
   }
}
