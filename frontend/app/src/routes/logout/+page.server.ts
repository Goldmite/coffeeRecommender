import { redirect } from '@sveltejs/kit';
import type { Actions } from './$types';
import { localizeHref } from '$lib/paraglide/runtime';

export const actions: Actions = {
	default: async ({ cookies, locals }) => {
		cookies.delete('jwt', { path: '/' });
		cookies.delete('onboarded', { path: '/' });

		locals.userId = undefined;
		locals.userEmail = undefined;
		locals.username = undefined;
		locals.isNew = undefined;

		throw redirect(303, localizeHref('/login'));
	},
};
