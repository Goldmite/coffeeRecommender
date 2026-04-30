import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import type { PurchaseDto } from '$lib/types/recommendation';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ url, locals, cookies }) => {
	const token = cookies.get('jwt');
	if (!token || !locals.userId) {
		throw error(401, 'Not authenticated');
	}
	const page = url.searchParams.get('page') || '0';

	const response = await fetch(
		`${PUBLIC_API_BASE_URL}/coffee/purchased/user/${locals.userId}?page=${page}&size=10`,
		{
			method: 'GET',
			headers: {
				Authorization: `Bearer ${token}`,
				'Content-Type': 'application/json',
			},
		},
	);

	if (!response.ok) {
		throw error(response.status);
	}
	const data = await response.json();
	const purchases: PurchaseDto[] = data.content;

	return {
		purchases: purchases,
		totalPages: data.page.totalPages,
		currentPage: parseInt(page),
	};
};
