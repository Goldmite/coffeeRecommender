import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = async ({ locals }) => {
	return {
		user: locals.userId
			? { id: locals.userId, email: locals.userEmail, name: locals.username }
			: null,
	};
};
