package com.lvsa.nw4.tweaks.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import techreborn.init.ModFluids;

@Mixin(LivingEntity.class)
public class H2SO4Mixin {

  private int hurtCooldown = 0;

  @Inject(method = "tick", at = @At("HEAD"))
  private void onTick(CallbackInfo ci) {
    LivingEntity entity = (LivingEntity)(Object)this;
    World world = entity.getWorld();
    
    // Check both feet and eye positions (for tall mobs)
    BlockPos[] checkPositions = {
        entity.getBlockPos(),
        entity.getBlockPos().up((int)Math.ceil(entity.getHeight() - 1))
    };
    
    for (BlockPos pos : checkPositions) {
      FluidState fluid = world.getFluidState(pos);
      
      if (fluid.isOf(ModFluids.SULFURIC_ACID.getFluid()) ||
          fluid.isOf(ModFluids.SULFURIC_ACID.getFlowingFluid())) {
        if (--hurtCooldown <= 0) {
          // Apply effects
          entity.damage(world.getDamageSources().magic(), 2.0f);
          entity.addStatusEffect(new StatusEffectInstance(
            StatusEffects.WITHER, 
            120, // 6 seconds
            0,   // Amplifier
            false, // Ambient
            true   // Show particles
          ));
          
          // Play sizzle sound
          world.playSound(
            null, 
            pos, 
            SoundEvents.BLOCK_LAVA_EXTINGUISH, 
            SoundCategory.BLOCKS,
            0.8f, 
            0.8f
          );
          
          hurtCooldown = 10; // 1 second cooldown
        }
        break; // Only trigger once per tick
      } else {
        hurtCooldown = 0; // Reset when leaving acid
      }
    }
  }
}