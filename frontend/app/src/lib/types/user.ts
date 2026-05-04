import type { ExperienceLevel, PrepMethod } from './recommendation';

export interface PreferencesResponse {
	userId: number;
	experienceLevel: ExperienceLevel;
	prepMethod: PrepMethod;
}
