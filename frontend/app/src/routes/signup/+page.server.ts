import { fail, redirect, type Actions } from '@sveltejs/kit';
import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const actions = {
	default: async ({ request, fetch }) => {
		const formData = await request.formData();
		const name = formData.get('name');
		const email = formData.get('email');
		//const password = formData.get('password');

		const res = await fetch(`${PUBLIC_API_BASE_URL}/users/signup`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(Object.fromEntries(formData)),
		});

		console.log(res);

		if (res.status === 400) {
			const validationErrors = await res.json();
			return fail(400, { validationErrors, name, email });
		}
		if (res.status === 403) {
			return fail(403, { error: 'Access Forbidden.' });
		}
		if (res.status === 409) {
			return fail(409, { conflictError: 'User already exists.', field: 'email', name, email });
		}
		if (!res.ok) {
			return fail(res.status, { error: 'An unexpected error occurred.' });
		}

		throw redirect(303, '/login?signup_success=true');
	},
} satisfies Actions;
