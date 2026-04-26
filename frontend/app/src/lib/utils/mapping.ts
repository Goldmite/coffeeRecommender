import { m } from '$lib/paraglide/messages';

export const processMap: Record<string, string> = {
	WASHED: m.process_washed(),
	NATURAL: m.process_natural(),
	HONEY: m.process_honey(),
	OTHER: m.process_other(),
};

export const roastMap: Record<string, string> = {
	LIGHT: m.roast_light(),
	MEDIUM_LIGHT: m.roast_medium_light(),
	MEDIUM: m.roast_medium(),
	MEDIUM_DARK: m.roast_medium_dark(),
	DARK: m.roast_dark(),
};

export const roastToLevel: Record<string, number> = {
	LIGHT: 1,
	MEDIUM_LIGHT: 2,
	MEDIUM: 3,
	MEDIUM_DARK: 4,
	DARK: 5,
};

export const formatEnum = (value: string, map: Record<string, string>) => {
	return map[value] || value; // Fallback to raw string if key not found
};
