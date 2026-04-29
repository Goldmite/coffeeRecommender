import { m } from '$lib/paraglide/messages';
import type { ExperienceLevel, PrepMethod } from '$lib/types/recommendation';

export const formatEnum = (value: string, map: Record<string, string>) => {
	return map[value] || value; // Fallback to raw string if key not found
};

export const processMap: Record<string, string> = {
	WASHED: m.process_washed(),
	NATURAL: m.process_natural(),
	HONEY: m.process_honey(),
	OTHER: m.other(),
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

export const levelToRoast: Record<number, string> = {
	1: m.even_lighter(),
	2: m.lighter(),
	3: m.current(),
	4: m.darker(),
	5: m.even_darker(),
};

export const experienceMap: Record<ExperienceLevel, { value: number; label: string }> = {
	BEGINNER: { value: 1, label: m.xp_beginner() },
	INTERMEDIATE: { value: 2, label: m.xp_intermediate() },
	ADVANCED: { value: 3, label: m.xp_advanced() },
	EXPERT: { value: 4, label: m.xp_expert() },
};

export const prepMethodMap: Record<PrepMethod, { label: string }> = {
	ESPRESSO: {
		label: m.prep_espresso(),
	},
	POUROVER: {
		label: m.prep_pourover(), // V60 / Chemex
	},
	IMMERSION: {
		label: m.prep_immersion(), // French Press / Aeropress
	},
	COLD_BREW: {
		label: m.prep_cold_brew(),
	},
	OTHER: {
		label: m.other(),
	},
};

export const flavorCategoryMap: Record<string, string> = {
	FRUITY: m.flavor_fruity(),
	FLORAL: m.flavor_floral(),
	SWEET: m.flavor_sweet(),
	NUTTY_COCOA: m.flavor_nutty_cocoa(),
	SPICES: m.flavor_spices(),
	SOUR: m.flavor_sour(),
	VEGETAL: m.flavor_vegetal(),
};

export const intensityMap: Record<number, string> = {
	1: m.low(),
	2: m.lower(),
	3: m.standard(),
	4: m.higher(),
	5: m.high(),
};
