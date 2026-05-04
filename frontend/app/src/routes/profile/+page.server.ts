import { error, redirect } from '@sveltejs/kit';
import type { Actions } from './$types';
import { localizeHref } from '$lib/paraglide/runtime';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const actions = {
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
