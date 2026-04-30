import type { Recommendations, ShopResponse } from '$lib/types/recommendation';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { error, type Actions } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { recordInteraction } from '$lib/server/interactions';

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
		let featureFilterReq = {};

		const areFeatureFiltersUsed = formData.get('filterEnabled') === 'true';
		if (areFeatureFiltersUsed) {
			const flavorFilters = formData.getAll('flavors');
			const originTypeInput = formData.getAll('origin-type'); // only fetches checked value
			const originTypeWeight = originTypeInput.includes('single-origin')
				? '5'
				: originTypeInput.includes('blend')
					? '0'
					: null;
			// helper - return null when default value (3)
			const getAttributeWeight = (value: FormDataEntryValue | null) =>
				value ? (value == '3' ? null : value) : null;
			// helper - 1.0 is default weight, null falls back to base default
			const getFlavorWeight = (category: string) => (flavorFilters.includes(category) ? '5' : null);
			featureFilterReq = {
				roastWeight: getAttributeWeight(formData.get('roast')),
				scaWeight: getAttributeWeight(formData.get('sca')),
				acidityWeight: getAttributeWeight(formData.get('acidity')),
				bodyWeight: getAttributeWeight(formData.get('body')),
				aftertasteWeight: getAttributeWeight(formData.get('aftertaste')),
				sweetnessWeight: getAttributeWeight(formData.get('sweetness')),
				bitternessWeight: getAttributeWeight(formData.get('bitterness')),
				singleOriginWeight: originTypeWeight,
				fruityWeight: getFlavorWeight('FRUITY'),
				floralWeight: getFlavorWeight('FLORAL'),
				sweetWeight: getFlavorWeight('SWEET'),
				nuttyCocoaWeight: getFlavorWeight('NUTTY_COCOA'),
				spicesWeight: getFlavorWeight('SPICES'),
				sourWeight: getFlavorWeight('SOUR'),
				vegetalWeight: getFlavorWeight('VEGETAL'),
			};
		}
		const requestFilters = {
			shopIds: shopIds,
			featureFilter: featureFilterReq,
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
	purchased: async ({ request, fetch, locals, cookies }) => {
		const formData = await request.formData();

		return await recordInteraction({
			fetch,
			token: cookies.get('jwt'),
			userId: locals.userId,
			formData,
		});
	},
} satisfies Actions;
