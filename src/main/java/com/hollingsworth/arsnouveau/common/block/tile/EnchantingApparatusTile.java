package com.hollingsworth.arsnouveau.common.block.tile;

import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.IEnchantingRecipe;
import com.hollingsworth.arsnouveau.api.util.ManaUtil;
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import com.hollingsworth.arsnouveau.setup.BlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EnchantingApparatusTile extends AnimatedTile implements IInventory {
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InvWrapper(this));
    public ItemStack catalystItem = ItemStack.EMPTY;
    public ItemEntity entity;
    public long frames = 0;

    private int craftingLength = 100;
    public boolean isCrafting;

    public EnchantingApparatusTile() {
        super(BlockRegistry.ENCHANTING_APP_TILE);
        counter = 1;
    }


    @Override
    public void tick() {
        if(level.isClientSide)
            return;

        if(isCrafting){
            if(this.getRecipe(catalystItem, null) == null)
                this.isCrafting = false;
            counter += 1;
        }

        if(counter > craftingLength) {
            counter = 1;

            if (this.isCrafting) {
                IEnchantingRecipe recipe = this.getRecipe(catalystItem, null);
                List<ItemStack> pedestalItems = getPedestalItems();
                if (recipe != null) {
                    pedestalItems.forEach(i -> i = null);
                    this.catalystItem = recipe.getResult(pedestalItems, this.catalystItem, this);
                    clearItems();
                    ParticleUtil.spawnPoof((ServerWorld) level, worldPosition);
                }

                this.isCrafting = false;
            }
            updateBlock();
        }
    }


    public void clearItems(){
        BlockPos.betweenClosedStream(this.getBlockPos().offset(5, -3, 5), this.getBlockPos().offset(-5, 3, -5)).forEach(blockPos -> {
            if (level.getBlockEntity(blockPos) instanceof ArcanePedestalTile && ((ArcanePedestalTile) level.getBlockEntity(blockPos)).stack != null) {
                ArcanePedestalTile tile = ((ArcanePedestalTile) level.getBlockEntity(blockPos));
                tile.stack = tile.stack == null ? ItemStack.EMPTY : tile.stack.getContainerItem();
                BlockState state = level.getBlockState(blockPos);
                level.sendBlockUpdated(blockPos, state, state, 3);
            }
        });
    }

    // Used for rendering on the client
    public List<BlockPos> pedestalList(){
        ArrayList<BlockPos> posList = new ArrayList<>();
        BlockPos.betweenClosedStream(this.getBlockPos().offset(5, -3, 5), this.getBlockPos().offset(-5, 3, -5)).forEach(blockPos -> {
            if(level.getBlockEntity(blockPos) instanceof ArcanePedestalTile && ((ArcanePedestalTile) level.getBlockEntity(blockPos)).stack != null &&  !((ArcanePedestalTile) level.getBlockEntity(blockPos)).stack.isEmpty()) {
                posList.add(blockPos.immutable());
            }
        });
        return posList;
    }

    public List<ItemStack> getPedestalItems(){
        ArrayList<ItemStack> pedestalItems = new ArrayList<>();
        BlockPos.betweenClosedStream(this.getBlockPos().offset(5, -3, 5), this.getBlockPos().offset(-5, 3, -5)).forEach(blockPos -> {
            if(level.getBlockEntity(blockPos) instanceof ArcanePedestalTile && ((ArcanePedestalTile) level.getBlockEntity(blockPos)).stack != null && !((ArcanePedestalTile) level.getBlockEntity(blockPos)).stack.isEmpty()) {
                pedestalItems.add(((ArcanePedestalTile) level.getBlockEntity(blockPos)).stack);
            }
        });
        return pedestalItems;
    }

    public IEnchantingRecipe getRecipe(ItemStack stack, @Nullable PlayerEntity playerEntity){
        List<ItemStack> pedestalItems = getPedestalItems();
        return ArsNouveauAPI.getInstance().getEnchantingApparatusRecipes(level).stream().filter(r-> r.isMatch(pedestalItems, stack, this, playerEntity)).findFirst().orElse(null);
    }

    public boolean attemptCraft(ItemStack catalyst, @Nullable PlayerEntity playerEntity){
        if(isCrafting)
            return false;
        if (!craftingPossible(catalyst, playerEntity)) {
            return false;
        }
        IEnchantingRecipe recipe = this.getRecipe(catalyst, playerEntity);
        ManaUtil.takeManaNearbyWithParticles(worldPosition, level, 10, recipe.manaCost());
        this.isCrafting = true;
        updateBlock();
        return true;
    }

    public boolean craftingPossible(ItemStack stack, PlayerEntity playerEntity){
        if(isCrafting || stack.isEmpty())
            return false;
        IEnchantingRecipe recipe = this.getRecipe(stack, playerEntity);

        return recipe != null && (!recipe.consumesMana() || (recipe.consumesMana() && ManaUtil.hasManaNearby(worldPosition, level, 10, recipe.manaCost())));
    }

    public void updateBlock(){
        if(counter == 0)
            counter = 1;
        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, 2);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        catalystItem = ItemStack.of((CompoundNBT)compound.get("itemStack"));
        isCrafting = compound.getBoolean("is_crafting");
        counter = compound.getInt("counter");
        super.load(state, compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        if(catalystItem != null) {
            CompoundNBT reagentTag = new CompoundNBT();
            catalystItem.save(reagentTag);
            compound.put("itemStack", reagentTag);
        }
        compound.putBoolean("is_crafting", isCrafting);
        compound.putInt("counter", counter);

        return super.save(compound);
    }
    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 3, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("counter", this.counter);
        tag.putBoolean("is_crafting", this.isCrafting);
        return this.save(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(level.getBlockState(worldPosition), pkt.getTag());
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return catalystItem.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return catalystItem;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(isCrafting || stack.isEmpty())
            return false;
        return catalystItem.isEmpty() && craftingPossible(stack,null);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if(isCrafting)
            return ItemStack.EMPTY;
        ItemStack stack = catalystItem.copy();
        catalystItem.shrink(count);
        updateBlock();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if(isCrafting)
            return ItemStack.EMPTY;
        return catalystItem;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if(isCrafting)
            return;
        this.catalystItem = stack;
        updateBlock();
        attemptCraft(this.catalystItem, null);
    }



    @Override
    public boolean stillValid(PlayerEntity player) {
        return false;
    }

    @Override
    public void clearContent() {
        this.catalystItem = ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        itemHandler.invalidate();
        super.invalidateCaps();
    }
}
