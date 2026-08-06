package net.mistersecret312.fairerdeath;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class AttachmentTypeInit
{
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
			NeoForgeRegistries.Keys.ATTACHMENT_TYPES, FairerDeath.MODID);

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<InventoryStorageAttachment>> STORAGE =
			ATTACHMENT_TYPES.register("storage",
					() -> AttachmentType.builder(InventoryStorageAttachment::new)
										.serialize(new IAttachmentSerializer<CompoundTag, InventoryStorageAttachment>() {
											@Override
											public InventoryStorageAttachment read(IAttachmentHolder holder, CompoundTag tag,
																		   HolderLookup.Provider provider)
											{
												InventoryStorageAttachment capability = new InventoryStorageAttachment();
												capability.deserializeNBT(provider, tag);
												return capability;
											}

											@Override
											public @Nullable CompoundTag write(InventoryStorageAttachment attachment,
																			   HolderLookup.Provider provider)
											{
												return attachment.serializeNBT(provider);
											}
										})
								  .build());


	public static void register(IEventBus eventBus)
	{
		ATTACHMENT_TYPES.register(eventBus);
	}
}
