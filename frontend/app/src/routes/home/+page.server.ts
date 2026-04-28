import type { Recommendations, ShopResponse } from '$lib/types/recommendation';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { error, type Actions } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ locals, cookies }) => {
	const token = cookies.get('jwt');
	if (!token || !locals.userId) {
		throw error(401, 'Not authenticated');
	}
	const needsOnboarding = locals.isNew;

	const response = await fetch(`${PUBLIC_API_BASE_URL}/shops`, {
		method: 'GET',
		headers: {
			Authorization: `Bearer ${token}`,
			'Content-Type': 'application/json',
		},
	});

	if (!response.ok) {
		throw error(response.status);
	}
	const shopList: ShopResponse[] = await response.json();

	return { shopList, needsOnboarding };
};

export const actions = {
	fetch: async ({ fetch, request, locals, cookies }) => {
		const token = cookies.get('jwt');
		if (!token || !locals.userId) {
			throw error(401, 'Not authenticated');
		}

		const formData = await request.formData();

		const shopIds = formData.getAll('shopIds');
		const featureFilter = {}; // TODO: fill in when feature filter present

		const requestFilters = {
			shopIds: shopIds,
			featureFilter: featureFilter,
		};

		const response = await fetch(
			`${PUBLIC_API_BASE_URL}/recommendation?userId=${locals.userId}&limit=10`,
			{
				method: 'POST',
				headers: {
					Authorization: `Bearer ${token}`,
					'Content-Type': 'application/json',
				},
				body: JSON.stringify(requestFilters),
			},
		);

		if (response.status === 401) {
			throw error(401, 'Session expired');
		}

		if (!response.ok) {
			throw error(response.status);
		}

		const recommendations: Recommendations = await response.json();

		return { recommendations, shopIds };
	},
	survey: async ({ request, fetch, locals, cookies }) => {
		const token = cookies.get('jwt');
		if (!token || !locals.userId) {
			throw error(401, 'Not authenticated');
		}

		const formData = await request.formData();
		formData.append('userId', locals.userId.toString());

		const response = await fetch(`${PUBLIC_API_BASE_URL}/users/preferences`, {
			method: 'POST',
			headers: {
				Authorization: `Bearer ${token}`,
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(Object.fromEntries(formData)),
		});
		if (!response.ok) {
			throw error(response.status);
		}
		cookies.set('onboarded', 'true', { path: '/', maxAge: 60 * 60 * 24 });

		return { success: true };
	},
} satisfies Actions;
