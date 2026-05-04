import { error, redirect } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import { localizeHref } from '$lib/paraglide/runtime';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import type { PreferencesResponse } from '$lib/types/user';

export const load: PageServerLoad = async ({ fetch, locals, cookies }) => {
	const userId = locals.userId;
	const token = cookies.get('jwt');
	if (!token || !userId) {
		throw error(401, 'Not authenticated');
	}

	const response = await fetch(`${PUBLIC_API_BASE_URL}/users/${userId}/preferences`, {
		method: 'GET',
		headers: {
			Authorization: `Bearer ${token}`,
			'Content-Type': 'application/json',
		},
	});
	if (!response.ok) {
		throw error(response.status);
	}

	const preferences: PreferencesResponse = await response.json();

	return { pref: preferences };
};

export const actions = {
	preferences: async ({ request, fetch, locals, cookies }) => {
		const token = cookies.get('jwt');
		if (!token || !locals.userId) {
			throw error(401, 'Not authenticated');
		}

		const formData = await request.formData();
		formData.append('userId', locals.userId.toString());

		const response = await fetch(`${PUBLIC_API_BASE_URL}/users/preferences/preparation`, {
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

		throw redirect(303, localizeHref('/home'));
	},
	deleteAccount: async ({ fetch, locals, cookies }) => {
		const token = cookies.get('jwt');
		const userId = locals.userId;

		if (!token || !userId) {
			throw error(401, 'Not authenticated');
		}

		const response = await fetch(`${PUBLIC_API_BASE_URL}/users/${userId}`, {
			method: 'DELETE',
			headers: {
				Authorization: `Bearer ${token}`,
				'Content-Type': 'application/json',
			},
		});

		if (!response.ok) {
			throw error(response.status);
		}

		cookies.delete('jwt', { path: '/' });
		cookies.delete('onboarded', { path: '/' });
		locals.userId = undefined;
		locals.userEmail = undefined;
		locals.username = undefined;
		locals.isNew = undefined;

		throw redirect(303, localizeHref('/signup'));
	},
} satisfies Actions;
