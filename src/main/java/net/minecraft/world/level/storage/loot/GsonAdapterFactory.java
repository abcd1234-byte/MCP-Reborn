package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Type;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class GsonAdapterFactory {
   public static <E, T extends SerializerType<E>> GsonAdapterFactory.Builder<E, T> builder(Registry<T> pRegistry, String pId, String pName, Function<E, T> pTypeFunction) {
      return new GsonAdapterFactory.Builder<>(pRegistry, pId, pName, pTypeFunction);
   }

   public static class Builder<E, T extends SerializerType<E>> {
      private final Registry<T> registry;
      private final String elementName;
      private final String typeKey;
      private final Function<E, T> typeGetter;
      @Nullable
      private Pair<T, GsonAdapterFactory.InlineSerializer<? extends E>> inlineType;
      @Nullable
      private T defaultType;

      Builder(Registry<T> pRegistry, String pElementName, String pTypeKey, Function<E, T> pTypeGetter) {
         this.registry = pRegistry;
         this.elementName = pElementName;
         this.typeKey = pTypeKey;
         this.typeGetter = pTypeGetter;
      }

      public GsonAdapterFactory.Builder<E, T> withInlineSerializer(T pInlineType, GsonAdapterFactory.InlineSerializer<? extends E> pInlineSerializer) {
         this.inlineType = Pair.of(pInlineType, pInlineSerializer);
         return this;
      }

      public GsonAdapterFactory.Builder<E, T> withDefaultType(T pDefaultType) {
         this.defaultType = pDefaultType;
         return this;
      }

      public Object build() {
         return new GsonAdapterFactory.JsonAdapter<>(this.registry, this.elementName, this.typeKey, this.typeGetter, this.defaultType, this.inlineType);
      }
   }

   public interface InlineSerializer<T> {
      JsonElement serialize(T pValue, JsonSerializationContext pContext);

      T deserialize(JsonElement pJson, JsonDeserializationContext pContext);
   }

   static class JsonAdapter<E, T extends SerializerType<E>> implements JsonDeserializer<E>, JsonSerializer<E> {
      private final Registry<T> registry;
      private final String elementName;
      private final String typeKey;
      private final Function<E, T> typeGetter;
      @Nullable
      private final T defaultType;
      @Nullable
      private final Pair<T, GsonAdapterFactory.InlineSerializer<? extends E>> inlineType;

      JsonAdapter(Registry<T> pRegistry, String pElementName, String pTypeKey, Function<E, T> pTypeGetter, @Nullable T pDefaultType, @Nullable Pair<T, GsonAdapterFactory.InlineSerializer<? extends E>> pInlineType) {
         this.registry = pRegistry;
         this.elementName = pElementName;
         this.typeKey = pTypeKey;
         this.typeGetter = pTypeGetter;
         this.defaultType = pDefaultType;
         this.inlineType = pInlineType;
      }

      public E deserialize(JsonElement p_78848_, Type p_78849_, JsonDeserializationContext p_78850_) throws JsonParseException {
         if (p_78848_.isJsonObject()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(p_78848_, this.elementName);
            String s = GsonHelper.getAsString(jsonobject, this.typeKey, "");
            T t;
            if (s.isEmpty()) {
               t = this.defaultType;
            } else {
               ResourceLocation resourcelocation = new ResourceLocation(s);
               t = this.registry.get(resourcelocation);
            }

            if (t == null) {
               throw new JsonSyntaxException("Unknown type '" + s + "'");
            } else {
               return t.getSerializer().deserialize(jsonobject, p_78850_);
            }
         } else if (this.inlineType == null) {
            throw new UnsupportedOperationException("Object " + p_78848_ + " can't be deserialized");
         } else {
            return this.inlineType.getSecond().deserialize(p_78848_, p_78850_);
         }
      }

      public JsonElement serialize(E p_78852_, Type p_78853_, JsonSerializationContext p_78854_) {
         T t = this.typeGetter.apply(p_78852_);
         if (this.inlineType != null && this.inlineType.getFirst() == t) {
            return ((InlineSerializer<E>)this.inlineType.getSecond()).serialize(p_78852_, p_78854_);
         } else if (t == null) {
            throw new JsonSyntaxException("Unknown type: " + p_78852_);
         } else {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty(this.typeKey, this.registry.getKey(t).toString());
            ((Serializer<E>)t.getSerializer()).serialize(jsonobject, p_78852_, p_78854_);
            return jsonobject;
         }
      }
   }
}
