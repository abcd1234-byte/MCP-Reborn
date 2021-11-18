package net.minecraft.world.level;

import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PathNavigationRegion implements BlockGetter, CollisionGetter {
   protected final int centerX;
   protected final int centerZ;
   protected final ChunkAccess[][] chunks;
   protected boolean allEmpty;
   protected final Level level;

   public PathNavigationRegion(Level pLevel, BlockPos pCenterPos, BlockPos pOffsetPos) {
      this.level = pLevel;
      this.centerX = SectionPos.blockToSectionCoord(pCenterPos.getX());
      this.centerZ = SectionPos.blockToSectionCoord(pCenterPos.getZ());
      int i = SectionPos.blockToSectionCoord(pOffsetPos.getX());
      int j = SectionPos.blockToSectionCoord(pOffsetPos.getZ());
      this.chunks = new ChunkAccess[i - this.centerX + 1][j - this.centerZ + 1];
      ChunkSource chunksource = pLevel.getChunkSource();
      this.allEmpty = true;

      for(int k = this.centerX; k <= i; ++k) {
         for(int l = this.centerZ; l <= j; ++l) {
            this.chunks[k - this.centerX][l - this.centerZ] = chunksource.getChunkNow(k, l);
         }
      }

      for(int i1 = SectionPos.blockToSectionCoord(pCenterPos.getX()); i1 <= SectionPos.blockToSectionCoord(pOffsetPos.getX()); ++i1) {
         for(int j1 = SectionPos.blockToSectionCoord(pCenterPos.getZ()); j1 <= SectionPos.blockToSectionCoord(pOffsetPos.getZ()); ++j1) {
            ChunkAccess chunkaccess = this.chunks[i1 - this.centerX][j1 - this.centerZ];
            if (chunkaccess != null && !chunkaccess.isYSpaceEmpty(pCenterPos.getY(), pOffsetPos.getY())) {
               this.allEmpty = false;
               return;
            }
         }
      }

   }

   private ChunkAccess getChunk(BlockPos pPos) {
      return this.getChunk(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()));
   }

   private ChunkAccess getChunk(int pX, int pZ) {
      int i = pX - this.centerX;
      int j = pZ - this.centerZ;
      if (i >= 0 && i < this.chunks.length && j >= 0 && j < this.chunks[i].length) {
         ChunkAccess chunkaccess = this.chunks[i][j];
         return (ChunkAccess)(chunkaccess != null ? chunkaccess : new EmptyLevelChunk(this.level, new ChunkPos(pX, pZ)));
      } else {
         return new EmptyLevelChunk(this.level, new ChunkPos(pX, pZ));
      }
   }

   public WorldBorder getWorldBorder() {
      return this.level.getWorldBorder();
   }

   public BlockGetter getChunkForCollisions(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pPos) {
      ChunkAccess chunkaccess = this.getChunk(pPos);
      return chunkaccess.getBlockEntity(pPos);
   }

   public BlockState getBlockState(BlockPos pPos) {
      if (this.isOutsideBuildHeight(pPos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         ChunkAccess chunkaccess = this.getChunk(pPos);
         return chunkaccess.getBlockState(pPos);
      }
   }

   public Stream<VoxelShape> getEntityCollisions(@Nullable Entity pEntity, AABB pCollisionBox, Predicate<Entity> pFilter) {
      return Stream.empty();
   }

   public Stream<VoxelShape> getCollisions(@Nullable Entity pEntity, AABB pCollisionBox, Predicate<Entity> pFilter) {
      return this.getBlockCollisions(pEntity, pCollisionBox);
   }

   public FluidState getFluidState(BlockPos pPos) {
      if (this.isOutsideBuildHeight(pPos)) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         ChunkAccess chunkaccess = this.getChunk(pPos);
         return chunkaccess.getFluidState(pPos);
      }
   }

   public int getMinBuildHeight() {
      return this.level.getMinBuildHeight();
   }

   public int getHeight() {
      return this.level.getHeight();
   }

   public ProfilerFiller getProfiler() {
      return this.level.getProfiler();
   }
}