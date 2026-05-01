import { fail, redirect, type Actions } from '@sveltejs/kit';
import type { AuthResponse } from '$lib/types/auth';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { m } from '$lib/paraglide/messages.js';
import { localizeHref } from '$lib/paraglide/runtime';

export const actions = {
	default: async ({ request, fetch, cookies }) => {
		const formData = await request.formData();
		const email = formData.get('email');

		const res = await fetch(`${PUBLIC_API_BASE_URL}/users/login`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(Object.fromEntries(formData)),
		});

		if (res.status === 400 || res.status === 401 || res.status === 403) {
			return fail(res.status, { email, authError: true, msg: m.auth_err_no_access() });
		}
		if (!res.ok) {
			return fail(res.status, { error: true, msg: m.err_unexpected() });
		}

		const data: AuthResponse = await res.json();

		cookies.set('jwt', data.token, {
			path: '/',
			httpOnly: true,
			sameSite: 'strict',
			secure: true,
			maxAge: 60 * 60 * 24,
		});
		throw redirect(303, localizeHref('/home'));
	},
} satisfies Actions;
