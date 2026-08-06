package net.mistersecret312.fairerdeath;

public enum Categories
{
	KEEP_INVENTORY,
	KEEP_HOTBAR,
	KEEP_ARMOR,
	KEEP_EXPERIENCE;

	public static boolean isKnown(String name)
	{
		for (Categories category : values())
		{
			if (category.name().equals(name))
				return true;
		}
		return false;
	}
}
