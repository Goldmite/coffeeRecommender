import { fail, redirect, type Actions } from '@sveltejs/kit';
import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { m } from '$lib/paraglide/messages.js';

export const actions = {
	default: async ({ request, fetch }) => {
		const formData = await request.formData();
		const name = formData.get('name');
		const email = formData.get('email');

		const res = await fetch(`${PUBLIC_API_BASE_URL}/users/signup`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(Object.fromEntries(formData)),
		});

		if (res.status === 400) {
			return fail(400, {
				error: true,
				email,
				name,
				msg: m.auth_err_bad_request(),
			});
		}
		if (res.status === 409) {
			return fail(409, {
				conflictError: true,
				name,
				msg: m.auth_err_email_taken(),
			});
		}
		if (!res.ok) {
			return fail(res.status, { error: m.err_unexpected() });
		}

		throw redirect(303, '/login');
	},
} satisfies Actions;
