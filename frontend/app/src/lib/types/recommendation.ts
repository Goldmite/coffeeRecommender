export type Recommendations = RecommendationDto[];

export interface RecommendationDto {
	score: number;
	coffee: CoffeeBeanResponse;
}

export interface ShopResponse {
	id: number;
	name: string;
	url: string;
	is_active: boolean;
}

export interface CoffeeBeanResponse {
	id: number;
	name: string;
	productUrl: string;
	shop: ShopResponse;
	// features
	origins: string[];
	process: Processing;
	roastLevel: RoastLevel;
	// extra details
	description: string;
	altitude: number[];
	scaScore: number | null;
	acidity: number | null;
	body: number | null;
	aftertaste: number | null;
	sweetness: number | null;
	bitterness: number | null;
	flavorNotes: string[];
}

export type Processing = 'WASHED' | 'NATURAL' | 'HONEY' | 'OTHER' | string;

export type RoastLevel = 'LIGHT' | 'MEDIUM_LIGHT' | 'MEDIUM' | 'MEDIUM_DARK' | 'DARK' | string;

export type ExperienceLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT' | string;

export type PrepMethod = 'ESPRESSO' | 'POUROVER' | 'IMMERSION' | 'COLD_BREW' | 'OTHER' | string;
