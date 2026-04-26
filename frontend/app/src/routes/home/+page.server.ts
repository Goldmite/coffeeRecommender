import type { Recommendations } from '$lib/types/recommendation';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { error, type Actions } from '@sveltejs/kit';

export const actions = {
	fetch: async ({ fetch, locals, cookies }) => {
		if (!locals.userId) {
			throw error(401, 'Unauthorized');
		}

		const token = cookies.get('jwt');
		if (!token) {
			throw error(401, 'Not authenticated');
		}

		const response = await fetch(
			`${PUBLIC_API_BASE_URL}/recommendation?userId=${locals.userId}&limit=5`,
			{
				method: 'GET',
				headers: {
					Authorization: `Bearer ${token}`,
					'Content-Type': 'application/json',
				},
			},
		);

		if (response.status === 401) {
			throw error(401, 'Session expired');
		}

		if (!response.ok) {
			throw error(response.status);
		}

		const recommendations: Recommendations = await response.json();

		return { recommendations };
	},
} satisfies Actions;
