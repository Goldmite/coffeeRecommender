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
		const flavorFilters = formData.getAll('flavors');

		const singleOriginOrBlendInput = formData.getAll('origin');

		const normalize = (val: FormDataEntryValue | null, max = 10, min = 1) => {
			if (val === null) return null;
			const num = Number(val);
			return parseFloat(((num - min) / (max - min)).toFixed(2));
		};
		// helper - 1.0 is default weight, null falls back to base default
		const getFlavorWeight = (category: string) => (flavorFilters.includes(category) ? 2.5 : null);
		const requestFilters = {
			shopIds: shopIds,
			featureFilter: {
				roastWeight: normalize(formData.get('roast'), 5),
				scaWeight: normalize(formData.get('sca'), 100, 80),
				acidityWeight: normalize(formData.get('acidity')),
				bodyWeight: normalize(formData.get('body')),
				aftertasteWeight: normalize(formData.get('aftertaste')),
				sweetnessWeight: normalize(formData.get('sweetness')),
				bitternessWeight: normalize(formData.get('bitterness')),
				singleOriginWeight: singleOriginOrBlendInput.length > 0 ? singleOriginOrBlendInput : null,
				fruityWeight: getFlavorWeight('FRUITY'),
				floralWeight: getFlavorWeight('FLORAL'),
				sweetWeight: getFlavorWeight('SWEET'),
				nuttyCocoaWeight: getFlavorWeight('NUTTY_COCOA'),
				spicesWeight: getFlavorWeight('SPICES'),
				sourWeight: getFlavorWeight('SOUR'),
				vegetalWeight: getFlavorWeight('VEGETAL'),
			},
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
