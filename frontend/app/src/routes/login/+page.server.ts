import { fail, redirect, type Actions } from '@sveltejs/kit';
import type { AuthResponse } from '$lib/types/auth';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const actions = {
	default: async ({ request, fetch, cookies }) => {
		const formData = await request.formData();
		const email = formData.get('email');

		const res = await fetch(`${PUBLIC_API_BASE_URL}/users/login`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(Object.fromEntries(formData)),
		});

		if (res.status === 400) {
			const validationErrors = await res.json();
			return fail(400, { validationErrors });
		}
		if (res.status === 401) {
			const authError = await res.json();
			return fail(401, { authError, email });
		}
		if (res.status === 403) {
			return fail(403, { error: 'Access Forbidden.' });
		}
		if (!res.ok) {
			return fail(res.status, { error: 'An unexpected error occurred.' });
		}

		const data: AuthResponse = await res.json();

		cookies.set('jwt', data.token, {
			path: '/',
			httpOnly: true,
			sameSite: 'strict',
			secure: true,
			maxAge: 60 * 60 * 24,
		});
		throw redirect(303, '/');
	},
} satisfies Actions;
